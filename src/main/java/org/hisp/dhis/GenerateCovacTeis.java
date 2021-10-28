package org.hisp.dhis;

import com.github.javafaker.Faker;
import org.apache.commons.collections.set.ListOrderedSet;
import org.hisp.dhis.actions.AuthenticatedApiActions;
import org.hisp.dhis.actions.IdGenerator;
import org.hisp.dhis.cache.Program;
import org.hisp.dhis.cache.TrackedEntityAttribute;
import org.hisp.dhis.cache.User;
import org.hisp.dhis.cache.UserCredentials;
import org.hisp.dhis.dxf2.events.enrollment.Enrollment;
import org.hisp.dhis.dxf2.events.enrollment.EnrollmentStatus;
import org.hisp.dhis.dxf2.events.event.DataValue;
import org.hisp.dhis.dxf2.events.event.Event;
import org.hisp.dhis.dxf2.events.event.Events;
import org.hisp.dhis.dxf2.events.trackedentity.Attribute;
import org.hisp.dhis.dxf2.events.trackedentity.TrackedEntityInstance;
import org.hisp.dhis.dxf2.events.trackedentity.TrackedEntityInstances;
import org.hisp.dhis.event.EventStatus;
import org.hisp.dhis.random.*;
import org.hisp.dhis.request.QueryParamsBuilder;
import org.hisp.dhis.response.dto.ApiResponse;
import org.hisp.dhis.tasks.DhisAbstractTask;
import org.hisp.dhis.tasks.tracker.GenerateAndReserveTrackedEntityAttributeValuesTask;
import org.hisp.dhis.utils.DataRandomizer;
import org.jfree.data.time.Day;
import org.joda.time.Days;

import javax.xml.crypto.Data;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * @author Gintare Vilkelyte <vilkelyte.gintare@gmail.com>
 */
public class GenerateCovacTeis extends DhisAbstractTask
{
    private Program program;
    protected GenerateCovacTeis( int weight )
    {
        super( weight );
        this.program = entitiesCache.getTrackerPrograms().stream().filter( p -> p.getId().equals( "yDuAzyqYABS" ) ).findFirst().orElse( null );
    }

    @Override
    public String getName()
    {
        return "Gen teis";
    }

    @Override
    public String getType()
    {
        return "http";
    }

    @Override
    public void execute()
        throws Exception
    {
        RandomizerContext ctx = RandomizerContext.EMPTY_CONTEXT();
        ctx.setSkipTeiInEvent( true );
        ctx.setGenerateIds( true );
        ctx.setSkipTeiInEnrollment( true );
        String ou = DataRandomizer.randomElementFromList( program.getOrganisationUnits() );

        UserCredentials userCredentials = new UserCredentials("admin_clone", "Test1212?");
        ctx.setProgram( program );
        ctx.setOrgUnitUid( ou );
        Instant enrollmentDate = Faker.instance().date().past( 210, TimeUnit.DAYS ).toInstant();

        TrackedEntityInstances instances = new TrackedEntityInstances();

        for ( int i = 0; i < 40 ; i++ )
        {
            TrackedEntityInstance instance =  new TrackedEntityInstanceRandomizer().createWithoutEnrollment( entitiesCache,ctx );

            Attribute att = instance.getAttributes().stream().filter( p -> p.getAttribute().equalsIgnoreCase( "CklPZdOd6H1" ))
                .findFirst().orElse( null );

            String sex = att.getValue();

            Enrollment enrollment = new EnrollmentRandomizer().createWithoutEvents( entitiesCache, ctx );
            enrollment.setEnrollmentDate( Date.from( enrollmentDate ) );
            enrollment.setIncidentDate( Date.from( enrollmentDate ) );
            enrollment.setStatus( EnrollmentStatus.ACTIVE );
            enrollment.setTrackedEntityInstance( instance.getTrackedEntityInstance() );

            String vaccineManu = DataRandomizer.randomElementFromList( Arrays.asList( "ASTRAZENECA",
                "ASTRAZENECA",
                "ASTRAZENECA",
                "GAMALEYA",
                "SINOPHARM",
                "BIONTECHPFIZER",
                "MODERNA",
                "GAMALEYA",
                "SINOPHARM" ) );

            Set<DataValue> sharedDataValues = new ListOrderedSet();
            sharedDataValues.addAll( getVaccineValues( vaccineManu) );
            sharedDataValues.addAll( getUnderlyingConds() );
            // patient had covid
            sharedDataValues.add( buildDv( "LOU9t0aR0z7", DataRandomizer.randomElementFromList( Arrays.asList( "YES", "NO", "NO", "UNKNOWN","UNKNOWN" )  )));
            Instant nextEventDate = enrollmentDate;

            for ( int j = 0; j < 2 ; j++ )
            {
                if (nextEventDate.isAfter( Instant.now() )) {
                    Event event = new EventRandomizer().createWithoutDataValues( entitiesCache, ctx );
                    event.setStatus( EventStatus.SCHEDULE);
                    event.setEventDate( null );
                    event.setDueDate( nextEventDate.toString() );
                    event.setEnrollment( enrollment.getEnrollment() );
                    event.setTrackedEntityInstance( instance.getTrackedEntityInstance() );

                    enrollment.getEvents().add( event );
                    //events.getEvents().add(event);
                    continue;
                }


                Event event = new EventRandomizer().createWithoutDataValues( entitiesCache, ctx );
                event.setEnrollment( enrollment.getEnrollment() );
                event.setTrackedEntityInstance( instance.getTrackedEntityInstance() );
                event.setEventDate( nextEventDate.toString() );
                event.setStatus( EventStatus.COMPLETED);
                nextEventDate = nextEventDate.plus( 21, ChronoUnit.DAYS );

                Set<DataValue> values = new ListOrderedSet();
                values.addAll( sharedDataValues );
                values.addAll(  getDataValues( sex, (j == 1),vaccineManu ));
                if (j== 0) {
                    values.add( buildDv( "FFWcps4MfuH", nextEventDate.toString() ) );

                }

                else {
                    enrollment.setStatus( EnrollmentStatus.COMPLETED );
                }

                values.add( buildDv( "PamkjF1JUnE", "2" ) );

                event.setDataValues( values );
                enrollment.getEvents().add( event );

                //events.getEvents().add(event);
            }

            instance.setEnrollments( Arrays.asList(enrollment) );
            instances.getTrackedEntityInstances().add( instance );
        }

        generateAttributes( instances.getTrackedEntityInstances(), userCredentials);

        performTaskAndRecord( () -> {
            ApiResponse response = new AuthenticatedApiActions( "/api/trackedEntityInstances", userCredentials )
                .post( instances );

            return response;
        }, response -> response.extractString( "status" ).equalsIgnoreCase( "ERROR" ) ? false : true );

    }

    private Set<DataValue> getDataValues( String sex, boolean lastEvent, String manufactor ) {
        Set<DataValue> values = new ListOrderedSet();

        if ( sex.equalsIgnoreCase( "female" )) {
            String pregnancy = DataRandomizer.randomElementFromList( Arrays.asList( "NOT_PREGNANT", "COVACPREGNANT", "NOT_PREGNANT","NOT_PREGNANT", "COVACLACTATING" ) );

            values.add( buildDv( "BfNZcj99yz4" , pregnancy ) );

            if ( pregnancy == "COVACPREGNANT") {
                values.add( buildDv(  "CBAs12YL4g7",DataRandomizer.randomElementFromList( Arrays.asList( "1TRIMESTER",
                    "2TRIMESTER",
                    "3TRIMESTER" ) ) )  );
            }
        }


        values.add( buildDv( "YTQulAldGOs", Instant.now().toString() ) );

        if ( lastEvent ) {
            if ( DataRandomizer.randomBoolean()) {
                values.add( buildDv( "dWoveSw6b79", DataRandomizer.randomElementFromList( Arrays.asList( "true", "false", "false", "false","false" ) ) ) );
            }

            values.add( buildDv( "LUIsbsm3okG", "DOSE2" ) );
            values.add( buildDv( "DSOWCIdQ8Tr", "true" ));
        }

        else {
            values.add( buildDv( "LUIsbsm3okG", "DOSE1" ) );
        }

        values.add( buildDv( "m9PgIDAJGlF", DataRandomizer.randomElementFromList( Arrays.asList( "true", "false", "false", "false" ) ) ));

        return values;
    }

    private Set<DataValue> getVaccineValues( String vaccType) {
        Set<DataValue> vaccineValues = new ListOrderedSet();

        vaccineValues.add( buildDv( "bbnyNYD1wgS", vaccType ) );

        switch ( vaccType ){
        case "ASTRAZENECA":
            vaccineValues.add( buildDv( "Yp1F4txx8tm", DataRandomizer.randomElementFromList( Arrays.asList( "4120Z001", "4120Z002", "4120Z003" ) ) ) );
            vaccineValues.add( buildDv( "rpkH9ZPGJcX", DataRandomizer.randomElementFromList( Arrays.asList( "ASTRAZENECA", "ASTRAZENECASKBIO" ) )  ) );
            break;
        default:
            vaccineValues.add( buildDv( "rpkH9ZPGJcX", vaccType ) );
            break;

        }

        vaccineValues.add( buildDv( "rWYryQb3ohn", "BRAND"+ vaccType ) );

        return vaccineValues;

    }

    private DataValue buildDv( String de, String value) {
        DataValue dataValue = new DataValue();
        dataValue.setDataElement( de );
        dataValue.setValue( value );


        return dataValue;

    }
    private Set<DataValue> getUnderlyingConds() {
        Set<DataValue> conditions = new ListOrderedSet();

        String random = DataRandomizer.randomElementFromList( Arrays.asList( "YES", "NO", "NO", "UNKNOWN" ) );
        conditions.add( buildDv(  "bCtWZGjSWM8", random ) );

        if ( random.equals( "YES" )) {
            List<String> conds = Arrays.asList( "LNHAYF3qdZl","C0Bony47eKp", "TT1h0vGu5da", "MuZ9dMVXpuM", "xVxLMku5DMX", "VCetMtYu1DY","gW4pd818Sw8" );

            int numberOfConds = DataRandomizer.randomIntInRange( 1, 3 );

            for ( int i = 0; i < numberOfConds ; i++ )
            {
                if (DataRandomizer.randomBoolean()) {
                    conditions.add( buildDv( "dpyQUtizp7s",Faker.instance().medical().diseaseName()  ) );
                    break;
                }

                conditions.add( buildDv(  DataRandomizer.randomElementFromList( conds ), "true" ) );
            }
        }

        return conditions;
    }

    private void generateAttributes(  List<TrackedEntityInstance> teis, UserCredentials userCredentials )
        throws Exception
    {
        for ( TrackedEntityAttribute att : program.getAttributes() )
        {
            if ( att.isGenerated() )
            {
                new GenerateAndReserveTrackedEntityAttributeValuesTask( 1, att.getTrackedEntityAttribute(),
                    userCredentials, teis.size() ).executeAndAddAttributes( teis );
            }
        }
    }
}

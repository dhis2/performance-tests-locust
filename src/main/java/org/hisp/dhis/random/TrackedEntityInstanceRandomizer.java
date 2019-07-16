package org.hisp.dhis.random;

import com.github.javafaker.Faker;
import org.hisp.dhis.cache.DataElement;
import org.hisp.dhis.cache.EntitiesCache;
import org.hisp.dhis.cache.Program;
import org.hisp.dhis.cache.ProgramStage;
import org.hisp.dhis.common.ValueType;
import org.hisp.dhis.dxf2.events.enrollment.Enrollment;
import org.hisp.dhis.dxf2.events.enrollment.EnrollmentStatus;
import org.hisp.dhis.dxf2.events.event.DataValue;
import org.hisp.dhis.dxf2.events.event.Event;
import org.hisp.dhis.dxf2.events.trackedentity.TrackedEntityInstance;
import org.hisp.dhis.dxf2.events.trackedentity.TrackedEntityInstances;
import org.hisp.dhis.event.EventStatus;
import org.hisp.dhis.organisationunit.FeatureType;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class TrackedEntityInstanceRandomizer
{

    private int maxEvent = 5;

    private int minEVent = 1;

    private DateFormat df = new SimpleDateFormat( "yyyy-MM-dd" );

    private Faker faker = Faker.instance();

    public TrackedEntityInstance create( EntitiesCache cache )
    {
        Program program = cache.getProgram( faker.number().numberBetween( 0, cache.getPrograms().size() - 1 ) );
        ProgramStage programStage = getRandomProgramStageFromProgram( program );
        String ou = program.getOrgUnit( faker.number().numberBetween( 0, program.getOrgUnits().size() - 1 ) );

        TrackedEntityInstance tei = new TrackedEntityInstance();
        tei.setTrackedEntityType( cache.getTeiType( "Person" ).getUid() );
        tei.setInactive( false );
        tei.setDeleted( false );
        tei.setFeatureType( FeatureType.NONE );
        tei.setOrgUnit( ou );

        Enrollment enrollment = new Enrollment();
        enrollment.setProgram( program.getUid() );
        enrollment.setOrgUnit( ou );
        enrollment.setEnrollmentDate( new Date() );
        enrollment.setIncidentDate( new Date() );
        enrollment.setStatus( EnrollmentStatus.ACTIVE );
        enrollment.setFollowup( false );
        enrollment.setDeleted( false );

        int eventsSize = faker.number().numberBetween( minEVent, maxEvent );
        enrollment.setEvents( IntStream.rangeClosed( 1, eventsSize ).mapToObj( i -> {

            Event event = new Event();
            event.setDueDate( df.format(new Date()) );
            event.setProgram( program.getUid() );
            event.setProgramStage( programStage.getUid() );
            event.setOrgUnit( ou );
            event.setStatus( EventStatus.ACTIVE );
            event.setEventDate( df.format(new Date()) );
            event.setFollowup( false );
            event.setDeleted( false );
            event.setAttributeOptionCombo( "" ); // TODO
            event.setDataValues( createDataValues( programStage, 1, 8 ) );
            return event;
        } ).collect( Collectors.toList() ) );
        return tei;
    }

    public TrackedEntityInstances create( EntitiesCache cache, int size )
    {
        TrackedEntityInstances teis = new TrackedEntityInstances();
        teis.setTrackedEntityInstances(
            IntStream.rangeClosed( 1, size ).mapToObj( i -> create( cache ) ).collect( Collectors.toList() ) );
        return teis;

    }

    private Set<DataValue> createDataValues( ProgramStage programStage, int min, int max )
    {
        Set<DataValue> dataValues = new HashSet<>();
        int maxSize = max > programStage.getDataElements().size() ? programStage.getDataElements().size() : max;
        int dataElementsSize = faker.number().numberBetween( min - 1, maxSize - 1 );

        while ( dataValues.size() < dataElementsSize )
        {
            dataValues.add( withRandomValue( programStage.getDataElements()
                .get( faker.number().numberBetween( 0, programStage.getDataElements().size() - 1 ) ) ) );
        }

        return dataValues;
    }

    private DataValue withRandomValue( DataElement dataElement )
    {
        DataValue dataValue = new DataValue();
        dataValue.setDataElement( dataElement.getUid() );
        dataValue.setProvidedElsewhere( false );
        String val = null;
        if ( dataElement.getOptionSet() != null && !dataElement.getOptionSet().isEmpty() )
        {
            val = dataElement.getOptionSet().get( faker.number().numberBetween( 0, dataElement.getOptionSet().size() - 1 ) );
        }
        else
        {

            ValueType valueType = dataElement.getValueType();
            if ( valueType.isBoolean() )
            {
                if ( valueType.equals( ValueType.BOOLEAN ) )
                {
                    val = String.valueOf( faker.bool().bool() );
                }
                else
                {
                    // TRUE_ONLY
                    val = "true";
                }
            }

            else if ( valueType.isDate() )
            {
                val = new SimpleDateFormat( "yyyy-MM-dd" ).format( faker.date().birthday() );
            }

            else if ( valueType.isNumeric() )
            {
                val = String.valueOf( faker.number().numberBetween( 1, 10000 ) );
            }
            else if ( valueType.isDecimal() )
            {
                val = String.valueOf( faker.number().randomDouble( 6, 0, 1000 ) );
            }
            else if ( valueType.isText() )
            {
                val = faker.lorem().characters( 10 );
            }
            else if ( valueType.isOrganisationUnit() )
            {
                val = ""; // TODO
            }
            else if ( valueType.isGeo() )
            {
                String longtitute = faker.address().longitude();
                String latitude = faker.address().latitude();
                val = latitude + ", " + longtitute;
            }
        }
        dataValue.setValue( val );
        return dataValue;
    }

    private ProgramStage getRandomProgramStageFromProgram( Program program )
    {
        return program.getStages().get( faker.number().numberBetween( 0, program.getStages().size() - 1 ) );
    }
}

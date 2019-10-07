package org.hisp.dhis.random;

import static net.andreinc.mockneat.unit.time.LocalDates.localDates;
import static net.andreinc.mockneat.unit.types.Bools.bools;
import static net.andreinc.mockneat.unit.types.Doubles.doubles;
import static net.andreinc.mockneat.unit.types.Ints.ints;
import static org.hisp.dhis.utils.RandomUtils.createRandomPoint;
import static org.hisp.dhis.utils.RandomUtils.randomizeSequence;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.hisp.dhis.cache.DataElement;
import org.hisp.dhis.cache.EntitiesCache;
import org.hisp.dhis.cache.Program;
import org.hisp.dhis.cache.ProgramStage;
import org.hisp.dhis.common.ValueType;
import org.hisp.dhis.dxf2.events.enrollment.Enrollment;
import org.hisp.dhis.dxf2.events.enrollment.EnrollmentStatus;
import org.hisp.dhis.dxf2.events.event.DataValue;
import org.hisp.dhis.dxf2.events.event.Event;
import org.hisp.dhis.dxf2.events.trackedentity.Attribute;
import org.hisp.dhis.dxf2.events.trackedentity.TrackedEntityInstance;
import org.hisp.dhis.dxf2.events.trackedentity.TrackedEntityInstances;
import org.hisp.dhis.event.EventStatus;
import org.hisp.dhis.organisationunit.FeatureType;

import com.vividsolutions.jts.geom.Point;

import net.andreinc.mockneat.types.enums.StringType;
import net.andreinc.mockneat.unit.objects.From;
import net.andreinc.mockneat.unit.text.Strings;
import net.andreinc.mockneat.unit.types.Ints;

public class TrackedEntityInstanceRandomizer
{
    private int maxEvent = 5;

    private int minEVent = 1;

    private DateFormat df = new SimpleDateFormat( "yyyy-MM-dd" );

    public TrackedEntityInstance create( EntitiesCache cache )
    {
        Program program = getRandomProgram( cache );
        ProgramStage programStage = getRandomProgramStageFromProgram( program );
        String ou = getRandomOrgUnitFromProgram( program );

        TrackedEntityInstance tei = new TrackedEntityInstance();
        tei.setTrackedEntityType( cache.getTeiType( "Person" ).getUid() );
        tei.setInactive( false );
        tei.setDeleted( false );
        tei.setFeatureType( FeatureType.NONE );
        tei.setOrgUnit( ou );
        tei.setAttributes( getRandomAttributesList( program ) );

        Enrollment enrollment = new Enrollment();
        enrollment.setProgram( program.getUid() );
        enrollment.setOrgUnit( ou );
        enrollment.setEnrollmentDate( new Date() );
        enrollment.setIncidentDate( new Date() );
        enrollment.setStatus( EnrollmentStatus.ACTIVE );
        enrollment.setFollowup( false );
        enrollment.setDeleted( false );

        int eventsSize = Ints.ints().range( minEVent, maxEvent ).get();
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

        tei.setEnrollments( Collections.singletonList( enrollment ) );

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
        int numberOfDataValuesToCreate = Ints.ints().range( min, max ).get();
        List<Integer> indexes = randomizeSequence( programStage.getDataElements().size(), numberOfDataValuesToCreate );
        
        for ( Integer index : indexes )
        {
            dataValues.add( withRandomValue( programStage.getDataElements().get(index)));
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
            val = From.from( dataElement.getOptionSet() ).get();
        }
        else {

            val = rndValueFrom( dataElement.getValueType() );
        }
        dataValue.setValue( val );
        return dataValue;
    }

    private Program getRandomProgram(EntitiesCache cache)
    {
        return From.from( cache.getPrograms() ).get();
    }

    private ProgramStage getRandomProgramStageFromProgram( Program program )
    {
        return From.from( program.getStages() ).get();
    }

    private String getRandomOrgUnitFromProgram( Program program )
    {
        return From.from( program.getOrgUnits() ).get();
    }

    private List<Attribute> getRandomAttributesList( Program program )
    {
        return program.getAttributes().stream().map( att -> new Attribute( att.getTrackedEntityAttributeUid(),
            att.getValueType(), rndValueFrom( att.getValueType() ) ) ).collect( Collectors.toList() );
    }

    private String rndValueFrom( ValueType valueType )
    {
        String val = null;

        if ( valueType.isBoolean() )
        {
            if ( valueType.equals( ValueType.BOOLEAN ) )
            {
                val = String.valueOf( bools().get() );
            }
            else
            {
                // TRUE_ONLY
                val = "true";
            }
        }

        else if ( valueType.isDate() )
        {
            val = localDates().display( DateTimeFormatter.ISO_LOCAL_DATE ).get();
        }

        else if ( valueType.isNumeric() )
        {
            val = String.valueOf( ints().range( 1, 10000 ).get() );
        }
        else if ( valueType.isDecimal() )
        {
            val = String.valueOf( doubles().range( 100.0, 1000.0 ).get() );
        }
        else if ( valueType.isText() )
        {
            val = Strings.strings().type( StringType.LETTERS ).get();
        }
        else if ( valueType.isOrganisationUnit() )
        {
            val = ""; // TODO
        }
        else if ( valueType.isGeo() )
        {
            Point p = createRandomPoint();
            val = p.getY() + ", " + p.getY();
        }
        return val;
    }


}

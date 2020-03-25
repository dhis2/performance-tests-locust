package org.hisp.dhis.random;

import net.andreinc.mockneat.types.enums.StringType;
import net.andreinc.mockneat.unit.objects.From;
import net.andreinc.mockneat.unit.text.Strings;
import net.andreinc.mockneat.unit.types.Ints;
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
import org.hisp.dhis.textpattern.*;
import org.hisp.dhis.utils.DataRandomizer;
import org.springframework.util.StringUtils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static net.andreinc.mockneat.unit.time.LocalDates.localDates;
import static net.andreinc.mockneat.unit.types.Bools.bools;
import static net.andreinc.mockneat.unit.types.Doubles.doubles;
import static net.andreinc.mockneat.unit.types.Ints.ints;

public class TrackedEntityInstanceRandomizer
{
    private int maxEvent = 5;

    private int minEVent = 1;

    private DateFormat df = new SimpleDateFormat( "yyyy-MM-dd" );

    public TrackedEntityInstance create( EntitiesCache cache )
    {
        Program program = getRandomProgram( cache );
        String ou = getRandomOrgUnitFromProgram( program );

        TrackedEntityInstance tei = new TrackedEntityInstance();
        tei.setTrackedEntityType( program.getEntityType() );
        tei.setInactive( false );
        tei.setDeleted( false );
        tei.setFeatureType( FeatureType.NONE );
        tei.setOrgUnit( ou );
        tei.setAttributes( getRandomAttributesList( program ) );

        Enrollment enrollment = createEnrollment( program, ou );
        tei.setEnrollments( Collections.singletonList( enrollment ) );

        return tei;
    }

    public Enrollment createEnrollment(EntitiesCache cache) {
        Program program = getRandomProgram( cache );
        String ou = getRandomOrgUnitFromProgram( program );

        return createEnrollment( program, ou );
    }

    public Enrollment createEnrollment(Program program, String ou) {
        ProgramStage programStage = getRandomProgramStageFromProgram( program );

        Enrollment enrollment = new Enrollment();
        enrollment.setProgram( program.getUid() );
        enrollment.setOrgUnit( ou );
        enrollment.setEnrollmentDate( new Date(  ) );
        enrollment.setIncidentDate( new Date() );
        enrollment.setStatus( EnrollmentStatus.ACTIVE );
        enrollment.setFollowup( false );
        enrollment.setDeleted( false );

        int eventsSize = Ints.ints().range( minEVent, maxEvent ).get();
        enrollment.setEvents( IntStream.rangeClosed( 1, eventsSize ).mapToObj( i -> {

            Event event = new Event();
            event.setDueDate( df.format( new Date() ) );
            event.setProgram( program.getUid() );
            event.setProgramStage( programStage.getUid() );
            event.setOrgUnit( ou );
            event.setStatus( EventStatus.ACTIVE );
            event.setEventDate( df.format( new Date() ) );
            event.setFollowup( false );
            event.setDeleted( false );
            event.setAttributeOptionCombo( "" ); // TODO
            event.setDataValues( createDataValues( programStage, 1, 8 ) );
            return event;
        } ).collect( Collectors.toList() ) );

        return enrollment;
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
        List<Integer> indexes = DataRandomizer.randomSequence( programStage.getDataElements().size(), numberOfDataValuesToCreate );

        for ( Integer index : indexes )
        {
            dataValues.add( withRandomValue( programStage.getDataElements().get( index ) ) );
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
        else
        {

            val = rndValueFrom( dataElement.getValueType() );
        }
        dataValue.setValue( val );
        return dataValue;
    }

    private Program getRandomProgram( EntitiesCache cache )
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
        return program.getAttributes().stream().map( att -> {
            if ( !StringUtils.isEmpty( att.getPattern() ) )
            {
                try
                {
                    String patternValue = withPattern( TextPatternParser.parse( att.getPattern() ) );
                    if ( att.getValueType().isNumeric() && patternValue.startsWith( "0" ) )
                    {
                        // Numeric type should not start with a 0
                        patternValue = patternValue.replaceAll( "0", Ints.ints().range( 1, 9 ).get().toString() );
                    }
                    return new Attribute( att.getTrackedEntityAttributeUid(), att.getValueType(), patternValue );
                }
                catch ( TextPatternParser.TextPatternParsingException e )
                {
                    e.printStackTrace();
                    return null;
                }
            }
            else
            {
                if ( att.getOptions() == null )
                {
                    return new Attribute( att.getTrackedEntityAttributeUid(), att.getValueType(),
                        rndValueFrom( att.getValueType() ) );
                }
                return null;
//                else
//                {
//                    return new Attribute( att.getTrackedEntityAttributeUid(), att.getValueType(),
//                        From.from( att.getOptions() ).get() );
//                }
            }
        } ).filter( Objects::nonNull ).collect( Collectors.toList() );
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
            val = String.valueOf( ints().range( 1, 100000 ).get() );
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
//            Point p = createRandomPoint();
//            val = p.getY() + ", " + p.getY();
            val = ""; // TODO
        }
        return val;
    }

    private String withPattern( TextPattern textPattern )
    {
        return generateValue( textPattern );
    }

    private String generateValue( TextPattern textPattern )
    {
        TextPatternSegment segment = getGeneratedSegment( textPattern );

        if ( segment.getMethod().equals( TextPatternMethod.SEQUENTIAL ) )
        {
            return String.format( "%0" + segment.getParameter().length() + "d", Ints.ints().get() );
        }
        else if ( segment.getMethod().equals( TextPatternMethod.RANDOM ) )
        {
            return TextPatternMethodUtils.generateRandom( new Random(), segment.getParameter() ) ;
        }
        else
        {
            return "";
        }
    }

    private TextPatternSegment getGeneratedSegment( TextPattern textPattern )
    {
        return textPattern.getSegments().stream().filter( ( tp ) -> tp.getMethod().isGenerated() ).findFirst()
            .orElse( null );
    }
}

package org.hisp.dhis.random;

import net.andreinc.mockneat.types.enums.StringType;
import net.andreinc.mockneat.unit.objects.From;
import net.andreinc.mockneat.unit.text.Strings;
import net.andreinc.mockneat.unit.types.Ints;
import org.hisp.dhis.cache.CategoryOptionCombo;
import org.hisp.dhis.cache.DataElement;
import org.hisp.dhis.cache.EntitiesCache;
import org.hisp.dhis.cache.Program;
import org.hisp.dhis.cache.ProgramStage;
import org.hisp.dhis.common.CodeGenerator;
import org.hisp.dhis.common.ValueType;
import org.hisp.dhis.event.EventStatus;
import org.hisp.dhis.textpattern.TextPattern;
import org.hisp.dhis.textpattern.TextPatternMethod;
import org.hisp.dhis.textpattern.TextPatternMethodUtils;
import org.hisp.dhis.textpattern.TextPatternSegment;
import org.hisp.dhis.tracker.bundle.TrackerBundleParams;
import org.hisp.dhis.tracker.domain.DataValue;
import org.hisp.dhis.tracker.domain.Enrollment;
import org.hisp.dhis.tracker.domain.Event;
import org.hisp.dhis.tracker.domain.TrackedEntity;
import org.hisp.dhis.utils.DataRandomizer;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import static net.andreinc.mockneat.unit.time.LocalDates.localDates;
import static net.andreinc.mockneat.unit.types.Bools.bools;
import static net.andreinc.mockneat.unit.types.Doubles.doubles;
import static net.andreinc.mockneat.unit.types.Ints.ints;

public class EventRandomizer
{
    private DateFormat df = new SimpleDateFormat( "yyyy-MM-dd" );

    private Event makeEvent( String teiUid, String enrollId, Program program, String ou, ProgramStage programStage,
        CategoryOptionCombo defaultCategoryCombo )
    {

        String[] teiEnrol = teiUid.split( ";" );

        Event event = new Event();
        event.setEvent( CodeGenerator.generateUid() );
        event.setTrackedEntity( teiUid );
        event.setEnrollment( enrollId );
        event.setCompletedAt( df.format( new Date() ) );
        event.setOccurredAt( df.format( new Date() ) );
        event.setProgram( program.getUid() );
        event.setProgramStage( programStage.getUid() );
        event.setOrgUnit( ou );
        event.setStatus( EventStatus.ACTIVE );
        event.setCreatedAt( df.format( new Date() ) );
        event.setFollowUp( false );
        event.setDeleted( false );
        event.setAttributeOptionCombo( defaultCategoryCombo.getUid() );
            event.setDataValues( createDataValues( programStage, 1, 8 ) );
        return event;
    }

    public TrackerBundleParams createBundle( Map<String, String> teiEnMap, Program program,
        CategoryOptionCombo defaultCategoryCombo, EntitiesCache cache )
        throws Exception
    {
        ProgramStage programStage = getRandomProgramStageFromProgram( program );
        String ou = getRandomOrgUnitFromProgram( program );
        List<Event> list = new ArrayList<>();
        Set<Map.Entry<String, String>> entries = teiEnMap.entrySet();
        for ( Map.Entry<String, String> entry : entries )
        {
            list.add( makeEvent( entry.getKey(), entry.getValue(), program, ou, programStage ,defaultCategoryCombo) );
        }

        TrackerBundleParams params = new TrackerBundleParams();
        params.setEvents( list );
        return params;
    }

    private Set<DataValue> createDataValues( ProgramStage programStage, int min, int max )
    {
        Set<DataValue> dataValues = new HashSet<>();
        int numberOfDataValuesToCreate = Ints.ints().range( min, max ).get();
        List<Integer> indexes = DataRandomizer
            .randomSequence( programStage.getDataElements().size(), numberOfDataValuesToCreate );

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

    private String getRandomTeiUid( List<String> teiUids )
    {
        return From.from( teiUids ).get();
    }

    private Program getRandomProgram( EntitiesCache cache )
    {
        List<Program> programs = cache.getPrograms();
        return From.from( programs ).get();
    }

    private ProgramStage getRandomProgramStageFromProgram( Program program )
        throws Exception
    {
        List<ProgramStage> stages = program.getStages();
        for ( ProgramStage stage : stages )
        {
            if ( stage.isRepeatable() )
            {
                return stage;
            }
        }
        throw new Exception( "Could not find a repeatable stage!" );
    }

    private String getRandomOrgUnitFromProgram( Program program )
    {
        List<String> orgUnits = program.getOrgUnits();
        return From.from( orgUnits ).get();
    }

//    private List<Attribute> getRandomAttributesList( Program program )
//    {
//        return program.getAttributes().stream().map( att -> {
//            if ( !StringUtils.isEmpty( att.getPattern() ) )
//            {
//                try
//                {
//                    String patternValue = withPattern( TextPatternParser.parse( att.getPattern() ) );
//                    if ( att.getValueType().isNumeric() && patternValue.startsWith( "0" ) )
//                    {
//                        // Numeric type should not start with a 0
//                        patternValue = patternValue.replaceAll( "0", Ints.ints().range( 1, 9 ).get().toString() );
//                    }
//                    //String attribute, String code, String createdAt, String updatedAt, String storedBy, ValueType valueType, String value
//                    return new Attribute( att.getTrackedEntityAttributeUid(), att.getValueType(), patternValue );
//                }
//                catch ( TextPatternParser.TextPatternParsingException e )
//                {
//                    e.printStackTrace();
//                    return null;
//                }
//            }
//            else
//            {
//                if ( att.getOptions() == null )
//                {
//                    return new Attribute( att.getTrackedEntityAttributeUid(), att.getValueType(), rndValueFrom( att.getValueType() ) );
//                }
//                return null;
////                else
////                {
////                    return new Attribute( att.getTrackedEntityAttributeUid(), att.getValueType(),
////                        From.from( att.getOptions() ).get() );
////                }
//            }
//        } ).filter( Objects::nonNull ).collect( Collectors.toList() );
//    }

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
            return TextPatternMethodUtils.generateRandom( new Random(), segment.getParameter() );
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

    private class TeiWrapper
    {
        private TrackedEntity tei;

        private Enrollment enrollment;

        private Event event;

        public TeiWrapper( TrackedEntity tei, Enrollment enrollment, Event event )
        {
            this.tei = tei;
            this.enrollment = enrollment;
            this.event = event;

        }
    }
}

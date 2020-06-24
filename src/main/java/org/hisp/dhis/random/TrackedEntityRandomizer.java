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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import static net.andreinc.mockneat.unit.time.LocalDates.localDates;
import static net.andreinc.mockneat.unit.types.Bools.bools;
import static net.andreinc.mockneat.unit.types.Doubles.doubles;
import static net.andreinc.mockneat.unit.types.Ints.ints;

public class TrackedEntityRandomizer
{
    private int maxEvent = 5;

    private int minEVent = 1;

    private DateFormat df = new SimpleDateFormat( "yyyy-MM-dd" );

    public TrackedEntity create( String id, Program program, EntitiesCache cache )
    {
        String ou = getRandomOrgUnitFromProgram( program );

        TrackedEntity tei = new TrackedEntity();
        tei.setTrackedEntity( id );
        tei.setTrackedEntityType( program.getEntityType() );
        tei.setInactive( false );
        tei.setDeleted( false );
        tei.setOrgUnit( ou );

        return tei;
    }

    public TrackerBundleParams createBundle( Map<String, String> idMap, Program program, EntitiesCache cache )
    {
        List<TrackedEntity> list = new ArrayList<>();
        Set<Map.Entry<String, String>> entries = idMap.entrySet();
        for ( Map.Entry<String, String> entry : entries )
        {
            list.add( create( entry.getKey(), program, cache ) );
        }

        TrackerBundleParams params = new TrackerBundleParams();
        params.setTrackedEntities( list );
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

    private Program getRandomProgram( EntitiesCache cache )
    {
        List<Program> programs = cache.getPrograms();
        return From.from( programs ).get();
    }

    private ProgramStage getRandomProgramStageFromProgram( Program program )
    {
        List<ProgramStage> stages = program.getStages();
        return From.from( stages ).get();
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

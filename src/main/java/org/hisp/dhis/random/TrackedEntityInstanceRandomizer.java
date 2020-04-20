package org.hisp.dhis.random;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.stream.Collectors;

import org.hisp.dhis.cache.EntitiesCache;
import org.hisp.dhis.cache.Program;
import org.hisp.dhis.dxf2.events.enrollment.Enrollment;
import org.hisp.dhis.dxf2.events.trackedentity.Attribute;
import org.hisp.dhis.dxf2.events.trackedentity.TrackedEntityInstance;
import org.hisp.dhis.dxf2.events.trackedentity.TrackedEntityInstances;
import org.hisp.dhis.organisationunit.FeatureType;
import org.hisp.dhis.textpattern.TextPattern;
import org.hisp.dhis.textpattern.TextPatternMethod;
import org.hisp.dhis.textpattern.TextPatternMethodUtils;
import org.hisp.dhis.textpattern.TextPatternParser;
import org.hisp.dhis.textpattern.TextPatternSegment;
import org.hisp.dhis.utils.DataRandomizer;
import org.springframework.util.StringUtils;

/**
 * Generates a random Tracked Entity Instance graph.
 *
 * TEI
 *  |_ENROLLMENT
 *        |__EVENT 1
 *        |__EVENT 2
 *        |_...
 *
 * - For each generated TEI, a list of attributes with random values is generated
 * - For each generate Event, a random number of data values is generated
 * - For each TEI, a random number of events (between 1 and 5) is generated (unless the randomly picked program stage
 * is non-repeatable, then only one event is generated)
 *
 */
public class TrackedEntityInstanceRandomizer
    extends
    AbstractTrackerEntityRandomizer<TrackedEntityInstance>
{
    private int maxEvent = 5;
    private int minEVent = 1;

    private EventRandomizer eventRandomizer;
    private EnrollmentRandomizer enrollmentRandomizer;

    public TrackedEntityInstanceRandomizer( int maxEvent, int minEVent )
    {
        this.maxEvent = maxEvent;
        this.minEVent = minEVent;

        eventRandomizer = new EventRandomizer();
        enrollmentRandomizer = new EnrollmentRandomizer( minEVent, maxEvent);
    }

    public TrackedEntityInstanceRandomizer()
    {
        eventRandomizer = new EventRandomizer();
        enrollmentRandomizer = new EnrollmentRandomizer( minEVent, maxEvent);
    }

    @Override
    public TrackedEntityInstance create( EntitiesCache cache, RandomizerContext ctx )
    {
        Program program = getProgramFromContextOrRnd( ctx, cache );

        String ou = getRandomOrgUnitFromProgram( program );
        ctx.setOrgUnitUid( ou );
        ctx.setSkipTeiInEvent( true );
        TrackedEntityInstance tei = new TrackedEntityInstance();

        tei.setTrackedEntityType( program.getEntityType() );
        tei.setInactive( false );
        tei.setDeleted( false );
        tei.setFeatureType( FeatureType.NONE );
        tei.setOrgUnit( ou );
        tei.setAttributes( getRandomAttributesList( program ) );

        Enrollment enrollment = enrollmentRandomizer.create( cache, ctx );
        tei.setEnrollments( Collections.singletonList( enrollment ) );

        return tei;
    }

    public TrackedEntityInstances create( EntitiesCache cache, int size )
    {
        List<TrackedEntityInstance> rndTeis = new ArrayList<>();
        TrackedEntityInstances teis = new TrackedEntityInstances();
        for ( int i = 0; i < size - 1 ; i++ )
        {
            TrackedEntityInstance trackedEntityInstance = createTrackedEntityInstance( cache );
            if ( trackedEntityInstance != null )
            {
                rndTeis.add( trackedEntityInstance );
            }
        }
        teis.setTrackedEntityInstances( rndTeis );
        return teis;

    }
    
    private TrackedEntityInstance createTrackedEntityInstance( EntitiesCache cache )
    {
        try
        {
            return create( cache, new RandomizerContext() );
        }
        catch ( Exception e )
        {
            e.printStackTrace();
        }
        return null;
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
                        patternValue = patternValue.replaceAll( "0",
                            String.valueOf( DataRandomizer.randomIntInRange( 1, 9 ) ) );
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
            }
        } ).filter( Objects::nonNull ).collect( Collectors.toList() );
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
            return String.format( "%0" + segment.getParameter().length() + "d", DataRandomizer.randomInt() );
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
}

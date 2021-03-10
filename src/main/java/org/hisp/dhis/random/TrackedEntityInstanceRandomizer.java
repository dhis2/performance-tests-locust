package org.hisp.dhis.random;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.stream.Collectors;

import com.github.javafaker.Faker;
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
        TrackedEntityInstance tei = createWithoutEnrollment( cache, ctx );

        ctx.setSkipTeiInEvent( true );
        ctx.setSkipTeiInEnrollment( true );
        Enrollment enrollment = enrollmentRandomizer.create( cache, ctx );
        tei.setEnrollments( Collections.singletonList( enrollment ) );

        return tei;
    }

    public TrackedEntityInstance createWithoutEnrollment( EntitiesCache cache, RandomizerContext ctx ) {
        Program program = getProgramFromContextOrRnd( ctx, cache );
        ctx.setSkipTeiInEvent( true );

        TrackedEntityInstance tei = new TrackedEntityInstance();

        tei.setTrackedEntityType( program.getEntityType() );
        tei.setInactive( false );
        tei.setDeleted( false );
        tei.setFeatureType( FeatureType.NONE );
        tei.setOrgUnit( getOrgUnitFromContextOrRndFromProgram(ctx, program ) );
        tei.setAttributes( getRandomAttributesList( program ) );

        return tei;
    }

    public TrackedEntityInstances create( EntitiesCache cache, RandomizerContext context, int min, int max) {
        int numberToCreate = DataRandomizer.randomIntInRange( min, max );

        return create(cache, context, numberToCreate);
    }

    public TrackedEntityInstances create( EntitiesCache cache, RandomizerContext context, int size )
    {
        List<TrackedEntityInstance> rndTeis = new ArrayList<>();
        for ( int i = 0; i < size  ; i++ )
        {
            TrackedEntityInstance trackedEntityInstance = create( cache, context );
            if ( trackedEntityInstance != null )
            {
                rndTeis.add( trackedEntityInstance );
            }
        }

        TrackedEntityInstances teis = new TrackedEntityInstances();
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

    public List<Attribute> getRandomAttributesList( Program program )
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
                    return new Attribute( att.getTrackedEntityAttribute(), att.getValueType(), patternValue );
                }
                catch ( TextPatternParser.TextPatternParsingException e )
                {
                    e.printStackTrace();
                    return null;
                }
            }
            else
            {
                if ( att.getOptions() == null || att.getOptions().isEmpty())
                {
                    if ( att.getDisplayName().toLowerCase().contains( "firstname" ) ||  att.getDisplayName().toLowerCase().contains( "given name" )) {
                        return new Attribute( att.getTrackedEntityAttribute(), att.getValueType(),
                            Faker.instance().name().firstName());
                    }

                    if ( att.getDisplayName().toLowerCase().contains( "surname" ) ||  att.getDisplayName().toLowerCase().contains( "given name" )) {
                        return new Attribute( att.getTrackedEntityAttribute(), att.getValueType(),
                            Faker.instance().name().lastName());
                    }

                    return new Attribute( att.getTrackedEntityAttribute(), att.getValueType(),
                        rndValueFrom( att.getValueType() ) );
                }

                else {
                    return null;
                    //return new Attribute( att.getTrackedEntityAttribute(), att.getValueType(), DataRandomizer.randomElementFromList( att.getOptions() ));
                }

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

        String value = "";
        if ( segment.getMethod().equals( TextPatternMethod.SEQUENTIAL ) )
        {
            value = String.format( "%0" + segment.getParameter().length() + "d", DataRandomizer.randomInt() );
        }
        else if ( segment.getMethod().equals( TextPatternMethod.RANDOM ) )
        {
            value = TextPatternMethodUtils.generateRandom( new Random(), segment.getParameter() );
        }
        else
        {
            value = "";
        }

        if (getValueSegment( textPattern ) != null) {
            value = getValueSegment( textPattern ).getParameter() + value;
        }

        return value;
    }

    private TextPatternSegment getGeneratedSegment( TextPattern textPattern )
    {
        return textPattern.getSegments().stream().filter( ( tp ) -> tp.getMethod().isGenerated() ).findFirst()
            .orElse( null );
    }

    private TextPatternSegment getValueSegment( TextPattern textPattern ) {
        return textPattern.getSegments().stream().filter( ( tp ) -> !tp.getMethod().isGenerated() ).findFirst()
            .orElse( null );
    }
}

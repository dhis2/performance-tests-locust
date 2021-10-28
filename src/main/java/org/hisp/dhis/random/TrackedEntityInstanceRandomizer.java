package org.hisp.dhis.random;

import com.github.javafaker.Faker;
import org.hisp.dhis.cache.EntitiesCache;
import org.hisp.dhis.cache.Program;
import org.hisp.dhis.dxf2.events.enrollment.Enrollment;
import org.hisp.dhis.dxf2.events.trackedentity.Attribute;
import org.hisp.dhis.dxf2.events.trackedentity.TrackedEntityInstance;
import org.hisp.dhis.dxf2.events.trackedentity.TrackedEntityInstances;
import org.hisp.dhis.organisationunit.FeatureType;
import org.hisp.dhis.textpattern.*;
import org.hisp.dhis.utils.DataRandomizer;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Generates a random Tracked Entity Instance graph.
 * <p>
 * TEI
 * |_ENROLLMENT
 * |__EVENT 1
 * |__EVENT 2
 * |_...
 * <p>
 * - For each generated TEI, a list of attributes with random values is generated
 * - For each generate Event, a random number of data values is generated
 * - For each TEI, a random number of events (between 1 and 5) is generated (unless the randomly picked program stage
 * is non-repeatable, then only one event is generated)
 */
public class TrackedEntityInstanceRandomizer
    extends
    AbstractTrackerEntityRandomizer<TrackedEntityInstance>
{
    private int maxEvent = 5;

    private int minEVent = 1;

    private EnrollmentRandomizer enrollmentRandomizer;

    public TrackedEntityInstanceRandomizer( int maxEvent, int minEVent )
    {
        this.maxEvent = maxEvent;
        this.minEVent = minEVent;

        enrollmentRandomizer = new EnrollmentRandomizer( minEVent, maxEvent );
    }

    public TrackedEntityInstanceRandomizer()
    {
        enrollmentRandomizer = new EnrollmentRandomizer( minEVent, maxEvent );
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

    public TrackedEntityInstance createWithoutEnrollment( EntitiesCache cache, RandomizerContext ctx )
    {
        Program program = getProgramFromContextOrRnd( ctx, cache );
        ctx.setSkipTeiInEvent( true );
        ctx.setProgram( program );

        TrackedEntityInstance tei = new TrackedEntityInstance();

        tei.setTrackedEntityType( program.getTrackedEntityType() );
        tei.setInactive( false );
        tei.setDeleted( false );
        tei.setFeatureType( FeatureType.NONE );
        tei.setOrgUnit( getOrgUnitFromContextOrRndFromProgram( ctx, program ) );
        tei.setAttributes( new TrackedEntityAttributeRandomizer().create( ctx, false, ctx.isProgramAttributesInEnrollment() ) );

        return tei;
    }

    public TrackedEntityInstances create( EntitiesCache cache, RandomizerContext context, int min, int max )
    {
        int numberToCreate = DataRandomizer.randomIntInRange( min, max );

        return create( cache, context, numberToCreate );
    }

    public TrackedEntityInstances create( EntitiesCache cache, RandomizerContext context, int size )
    {
        List<TrackedEntityInstance> rndTeis = new ArrayList<>();
        for ( int i = 0; i < size; i++ )
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
}

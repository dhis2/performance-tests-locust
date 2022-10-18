package org.hisp.dhis.random;

import org.hisp.dhis.cache.EntitiesCache;
import org.hisp.dhis.cache.Program;
import org.hisp.dhis.dxf2.events.enrollment.Enrollment;
import org.hisp.dhis.dxf2.events.trackedentity.TrackedEntityInstance;
import org.hisp.dhis.dxf2.events.trackedentity.TrackedEntityInstances;
import org.hisp.dhis.organisationunit.FeatureType;
import org.hisp.dhis.utils.Randomizer;
import org.hisp.dhis.utils.UidGenerator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
    private int maxEvent;

    private int minEVent;

    private final EnrollmentRandomizer enrollmentRandomizer;

    public TrackedEntityInstanceRandomizer( Randomizer rnd, int maxEvent, int minEVent )
    {
        super( rnd );
        this.maxEvent = maxEvent;
        this.minEVent = minEVent;

        enrollmentRandomizer = new EnrollmentRandomizer( rnd, minEVent, maxEvent );
    }

    public TrackedEntityInstanceRandomizer( Randomizer rnd )
    {
        this(rnd, 1, 5 );
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
        if ( ctx.isGenerateIds() ) {
            tei.setTrackedEntityInstance( UidGenerator.generateUid() );
        }
        tei.setStoredBy( "performance-test" );
        tei.setTrackedEntityType( program.getTrackedEntityType() );
        tei.setInactive( false );
        tei.setDeleted( false );
        tei.setFeatureType( FeatureType.NONE );
        tei.setOrgUnit( getOrgUnitFromContextOrRndFromProgram( ctx, program ) );
        tei.setAttributes( new TrackedEntityAttributeRandomizer(rnd).create( ctx, false, ctx.isProgramAttributesInEnrollment() ) );

        return tei;
    }

    public TrackedEntityInstances create( EntitiesCache cache, RandomizerContext context, int min, int max )
    {
        int numberToCreate = rnd.randomIntInRange( min, max );

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

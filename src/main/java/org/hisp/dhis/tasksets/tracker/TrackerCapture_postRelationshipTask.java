package org.hisp.dhis.tasksets.tracker;

import org.hisp.dhis.actions.AuthenticatedApiActions;
import org.hisp.dhis.cache.Program;
import org.hisp.dhis.cache.TrackedEntityAttribute;
import org.hisp.dhis.cache.User;
import org.hisp.dhis.cache.UserCredentials;
import org.hisp.dhis.dxf2.events.trackedentity.TrackedEntityInstances;
import org.hisp.dhis.random.RandomizerContext;
import org.hisp.dhis.random.RelationshipRandomizer;
import org.hisp.dhis.random.TrackedEntityInstanceRandomizer;
import org.hisp.dhis.response.dto.ApiResponse;
import org.hisp.dhis.tasks.DhisAbstractTask;
import org.hisp.dhis.tasks.tracker.GenerateAndReserveTrackedEntityAttributeValuesTask;
import org.hisp.dhis.tasks.tracker.tei.AddTeiTask;
import org.hisp.dhis.tasksets.DhisAbstractTaskSet;
import org.hisp.dhis.utils.Randomizer;

import javax.print.attribute.standard.MediaSize;
import java.util.List;

/**
 * @author Gintare Vilkelyte <vilkelyte.gintare@gmail.com>
 */
public class TrackerCapture_postRelationshipTask
    extends DhisAbstractTaskSet
{

    private static final String NAME = "/api/relationships";

    private RelationshipRandomizer relationshipRandomizer;

    public TrackerCapture_postRelationshipTask( int weight )
    {
        super( NAME, weight );
    }

    @Override
    public String getName()
    {
        return NAME;
    }

    @Override
    public String getType()
    {
        return "POST";
    }

    @Override
    public void execute()
        throws Exception
    {
        Randomizer rnd = getNextRandomizer( getName() );
        // todo this test won't work on SL because the TEIs passed there might not have a TET that matches relationship types.
        User user = getUser(rnd);

        RandomizerContext context = new RandomizerContext();
        context.setOrgUnitUid( rnd.randomElementFromList( user.getOrganisationUnits() ) );
        context.setProgram( rnd.randomElementFromList( entitiesCache.getTrackerPrograms() ) );

        AuthenticatedApiActions actions = new AuthenticatedApiActions( NAME, user.getUserCredentials() );
        List<String> uids = createTeis( context, rnd);

        if ( uids == null )
        {
            return;
        }

        org.hisp.dhis.dxf2.events.trackedentity.Relationship relationship = relationshipRandomizer
            .create( entitiesCache, uids.get( 0 ), uids.get( 1 ) );

        performTaskAndRecord( () -> actions.post( relationship ) );

        waitBetweenTasks();
    }

    private List<String> createTeis(RandomizerContext context, Randomizer rnd)
        throws Exception
    {

        TrackedEntityInstances trackedEntityInstances = new TrackedEntityInstanceRandomizer(rnd).create(
            entitiesCache, context, 2
        );

        generateAttributes( context.getProgram(), trackedEntityInstances, user.getUserCredentials(), rnd);

        ApiResponse body = new AddTeiTask( 1, trackedEntityInstances, user.getUserCredentials(), rnd )
            .executeAndGetResponse();

        return body.extractUids();
    }

    private void generateAttributes(Program program, TrackedEntityInstances teis, UserCredentials userCredentials, Randomizer rnd)
        throws Exception
    {
        for ( TrackedEntityAttribute att : program.getGeneratedAttributes() )
        {
            new GenerateAndReserveTrackedEntityAttributeValuesTask( 1, att.getTrackedEntityAttribute(),
                userCredentials, teis.getTrackedEntityInstances().size(), rnd )
                .executeAndAddAttributes( teis.getTrackedEntityInstances() );

        }

    }

}

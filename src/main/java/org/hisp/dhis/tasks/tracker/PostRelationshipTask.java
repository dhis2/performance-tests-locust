package org.hisp.dhis.tasks.tracker;

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
import org.hisp.dhis.tasks.tracker.tei.AddTeiTask;
import org.hisp.dhis.utils.DataRandomizer;

import java.util.List;

/**
 * @author Gintare Vilkelyte <vilkelyte.gintare@gmail.com>
 */
public class PostRelationshipTask
    extends DhisAbstractTask
{
    private String endpoint = "/api/relationships";

    private RelationshipRandomizer relationshipRandomizer;

    public PostRelationshipTask( int weight )
    {
        super( weight );
        this.relationshipRandomizer = new RelationshipRandomizer();
    }

    @Override
    public String getName()
    {
        return endpoint;
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
        User user = getUser();

        RandomizerContext context = new RandomizerContext();
        context.setOrgUnitUid( DataRandomizer.randomElementFromList( user.getOrganisationUnits() ) );
        context.setProgram( DataRandomizer.randomElementFromList( entitiesCache.getTrackerPrograms() ) );

        AuthenticatedApiActions actions = new AuthenticatedApiActions( endpoint, user.getUserCredentials() );
        List<String> uids = createTeis( context );

        if ( uids == null )
        {
            return;
        }

        org.hisp.dhis.dxf2.events.trackedentity.Relationship relationship = relationshipRandomizer
            .create( entitiesCache, uids.get( 0 ), uids.get( 1 ) );

        performTaskAndRecord( () -> actions.post( relationship ) );

        waitBetweenTasks();
    }

    private List<String> createTeis( RandomizerContext context )
        throws Exception
    {

        TrackedEntityInstances trackedEntityInstances = new TrackedEntityInstanceRandomizer().create(
            entitiesCache, context, 2
        );

        generateAttributes( context.getProgram(), trackedEntityInstances, user.getUserCredentials() );

        ApiResponse body = new AddTeiTask( 1, trackedEntityInstances, user.getUserCredentials() )
            .executeAndGetResponse();

        return body.extractUids();
    }

    private void generateAttributes( Program program, TrackedEntityInstances teis, UserCredentials userCredentials )
        throws Exception
    {
        for ( TrackedEntityAttribute att : program.getGeneratedAttributes() )
        {
            new GenerateAndReserveTrackedEntityAttributeValuesTask( 1, att.getTrackedEntityAttribute(),
                userCredentials, teis.getTrackedEntityInstances().size() )
                .executeAndAddAttributes( teis.getTrackedEntityInstances() );

        }

    }

}

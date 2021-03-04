package org.hisp.dhis.tasks.tracker;

import org.hisp.dhis.actions.AuthenticatedApiActions;
import org.hisp.dhis.cache.EntitiesCache;
import org.hisp.dhis.cache.Program;
import org.hisp.dhis.cache.UserCredentials;
import org.hisp.dhis.dxf2.events.trackedentity.Attribute;
import org.hisp.dhis.dxf2.events.trackedentity.TrackedEntityInstance;
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

    public PostRelationshipTask( int weight, EntitiesCache cache )
    {
        this.weight = weight;
        this.entitiesCache = cache;
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
        user = getUser();

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
            .create( entitiesCache, context, uids.get( 0 ), uids.get( 1 ) );

        performTaskAndRecord( () -> actions.post( relationship ) );

        waitBetweenTasks();
    }

    private List<String> createTeis( RandomizerContext context )
        throws Exception
    {

        TrackedEntityInstances trackedEntityInstances = new TrackedEntityInstanceRandomizer().create(
            entitiesCache, context, 2
        );

        trackedEntityInstances.getTrackedEntityInstances().forEach( p-> generateAttributes( context.getProgram(), p, user.getUserCredentials()) );

        ApiResponse body = new AddTeiTask( 1, entitiesCache, trackedEntityInstances, user.getUserCredentials() )
            .executeAndGetResponse();

        List<String> uids = body.extractUids();

        return uids;
    }

    private void generateAttributes( Program program, TrackedEntityInstance tei, UserCredentials userCredentials )
    {

        program.getAttributes().stream().filter( p ->
            p.isGenerated()
        ).forEach( att -> {
                ApiResponse response = new GenerateTrackedEntityAttributeValueTask( 1, att.getTrackedEntityAttribute(),
                    userCredentials ).executeAndGetResponse();

                String value = response.extractString( "value" );

                Attribute attribute = tei.getAttributes().stream()
                    .filter( teiAtr -> teiAtr.getAttribute().equals( att.getTrackedEntityAttribute() ) )
                    .findFirst().orElse( new Attribute() );

                attribute.setValue( value );
            }
        );

    }

}

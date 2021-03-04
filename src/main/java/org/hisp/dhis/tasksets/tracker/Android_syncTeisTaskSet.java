package org.hisp.dhis.tasksets.tracker;

import org.hisp.dhis.actions.AuthenticatedApiActions;
import org.hisp.dhis.cache.EntitiesCache;
import org.hisp.dhis.cache.Program;
import org.hisp.dhis.cache.User;
import org.hisp.dhis.cache.UserCredentials;
import org.hisp.dhis.dxf2.events.trackedentity.Attribute;
import org.hisp.dhis.dxf2.events.trackedentity.TrackedEntityInstance;
import org.hisp.dhis.dxf2.events.trackedentity.TrackedEntityInstances;
import org.hisp.dhis.random.RandomizerContext;
import org.hisp.dhis.random.TrackedEntityInstanceRandomizer;
import org.hisp.dhis.random.UserRandomizer;
import org.hisp.dhis.request.QueryParamsBuilder;
import org.hisp.dhis.response.dto.ApiResponse;
import org.hisp.dhis.tasks.DhisAbstractTask;
import org.hisp.dhis.tasks.tracker.GenerateAndReserveTrackedEntityAttributeValuesTask;
import org.hisp.dhis.utils.DataRandomizer;

import java.util.List;

/**
 * @author Gintare Vilkelyte <vilkelyte.gintare@gmail.com>
 */
public class Android_syncTeisTaskSet
    extends DhisAbstractTask
{
    private int minPayload = 3;
    private int maxPayload = 10;
    public Android_syncTeisTaskSet( int weight, EntitiesCache cache )
    {
        this.weight = weight;
        this.entitiesCache = cache;
    }
    public Android_syncTeisTaskSet( int weight, EntitiesCache cache, int payloadSize) {
        this(weight, cache);
        this.minPayload = payloadSize;
        this.maxPayload = payloadSize;
    }

    @Override
    public String getName()
    {
        return String.format( "Android: sync teis (%d, %d)", minPayload, maxPayload);
    }

    @Override
    public String getType()
    {
        return "http";
    }

    @Override
    public void execute()
        throws Exception
    {
        User user = new UserRandomizer().getRandomUser( entitiesCache );
        String ou = new UserRandomizer().getRandomUserOrgUnit( user );
        Program program = DataRandomizer.randomElementFromList( entitiesCache.getTrackerPrograms() );

        RandomizerContext context = new RandomizerContext();
        context.setProgram( program );
        context.setOrgUnitUid( ou );

        TrackedEntityInstances teis = new TrackedEntityInstanceRandomizer().create( this.entitiesCache, context, minPayload, maxPayload );

        generateAttributes( program, teis.getTrackedEntityInstances(), user.getUserCredentials() );

        performTaskAndRecord( () -> new AuthenticatedApiActions( "/api/trackedEntityInstances", user.getUserCredentials() )
            .post( teis, new QueryParamsBuilder().add( "strategy=SYNC" ) ) );

        waitBetweenTasks();
    }

    private void generateAttributes( Program program, List<TrackedEntityInstance> teis, UserCredentials userCredentials )
    {
        program.getAttributes().stream().filter( p ->
            p.isGenerated()
        ).forEach( att -> {
            ApiResponse response = new GenerateAndReserveTrackedEntityAttributeValuesTask( 1, att.getTrackedEntityAttribute(),
                userCredentials, teis.size() ).executeAndGetResponse();
            List<String> values = response.extractList( "value" );

            for ( int i = 0; i < teis.size(); i++ )
            {
                Attribute attribute = teis.get( i ).getAttributes().stream()
                    .filter( teiAtr -> teiAtr.getAttribute().equals( att.getTrackedEntityAttribute() ) )
                    .findFirst().orElse( null );

                attribute.setValue( values.get( i ) );
            }
        } );
    }
}

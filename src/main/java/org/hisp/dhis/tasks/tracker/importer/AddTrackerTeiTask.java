package org.hisp.dhis.tasks.tracker.importer;

import org.hisp.dhis.actions.AuthenticatedApiActions;
import org.hisp.dhis.cache.EntitiesCache;
import org.hisp.dhis.cache.UserCredentials;
import org.hisp.dhis.dxf2.events.trackedentity.TrackedEntityInstances;
import org.hisp.dhis.models.TrackedEntities;
import org.hisp.dhis.random.RandomizerContext;
import org.hisp.dhis.random.TrackedEntityInstanceRandomizer;
import org.hisp.dhis.request.QueryParamsBuilder;
import org.hisp.dhis.response.dto.ApiResponse;
import org.hisp.dhis.response.dto.TrackerApiResponse;
import org.hisp.dhis.tasks.DhisAbstractTask;
import org.hisp.dhis.tracker.domain.mapper.TrackedEntityMapperImpl;

import java.util.stream.Collectors;

/**
 * @author Gintare Vilkelyte <vilkelyte.gintare@gmail.com>
 */
public class AddTrackerTeiTask
    extends DhisAbstractTask
{
    private String endpoint = "/api/tracker";

    private TrackedEntities trackedEntityInstanceBody;

    private TrackerApiResponse response;

    public AddTrackerTeiTask( int weight, EntitiesCache entitiesCache )
    {
        this.weight = weight;
        this.entitiesCache = entitiesCache;
    }

    public AddTrackerTeiTask( int weight, EntitiesCache cache, TrackedEntities trackedEntityInstance,
        UserCredentials userCredentials )
    {
        this( weight, cache );
        trackedEntityInstanceBody = trackedEntityInstance;
        this.userCredentials = userCredentials;

    }

    public String getName()
    {
        return this.endpoint;
    }

    @Override
    public String getType()
    {
        return "POST";
    }

    public void execute()
    {
        if ( trackedEntityInstanceBody == null )
        {
            TrackedEntityInstances ins = new TrackedEntityInstanceRandomizer()
                .create( this.entitiesCache, RandomizerContext.EMPTY_CONTEXT(), 5 );

            trackedEntityInstanceBody = TrackedEntities.builder().trackedEntities(
                ins.getTrackedEntityInstances().stream().map( p -> new TrackedEntityMapperImpl().from( p ) ).collect( Collectors
                    .toList() ) ).build();
        }

        long time = System.currentTimeMillis();

        boolean hasFailed = false;
        try
        {
            ApiResponse response = new AuthenticatedApiActions( this.endpoint, getUserCredentials() ).post( trackedEntityInstanceBody, new QueryParamsBuilder().add( "async=false" ) );
            this.response = new TrackerApiResponse( response );
        }

        catch ( Exception e )
        {
            recordFailure( System.currentTimeMillis() - time, e.getMessage() );
            hasFailed = true;
        }

        if ( !hasFailed )
        {
            if ( response.statusCode() == 200)
            {
                recordSuccess( response.getRaw() );
            }
            else
            {
                recordFailure( response.getRaw() );
            }
        }
    }

    public TrackerApiResponse executeAndGetResponse()
    {
        this.execute();
        return this.response;
    }
}

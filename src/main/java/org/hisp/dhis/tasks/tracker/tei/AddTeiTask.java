package org.hisp.dhis.tasks.tracker.tei;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import org.hisp.dhis.actions.AuthenticatedApiActions;
import org.hisp.dhis.actions.RestApiActions;
import org.hisp.dhis.cache.EntitiesCache;
import org.hisp.dhis.cache.User;
import org.hisp.dhis.cache.UserCredentials;
import org.hisp.dhis.dxf2.events.trackedentity.TrackedEntityInstance;
import org.hisp.dhis.dxf2.events.trackedentity.TrackedEntityInstances;
import org.hisp.dhis.random.RandomizerContext;
import org.hisp.dhis.random.TrackedEntityInstanceRandomizer;
import org.hisp.dhis.random.UserRandomizer;
import org.hisp.dhis.response.dto.ApiResponse;
import org.hisp.dhis.tasks.DhisAbstractTask;

public class AddTeiTask
    extends
    DhisAbstractTask
{
    private String endpoint = "/api/trackedEntityInstances";
    private TrackedEntityInstances trackedEntityInstanceBody;
    private ApiResponse response;

    public AddTeiTask( int weight, EntitiesCache entitiesCache )
    {
        this.weight = weight;
        this.entitiesCache = entitiesCache;
    }

    public AddTeiTask( int weight, EntitiesCache cache, TrackedEntityInstances trackedEntityInstance, UserCredentials userCredentials ) {
        this(weight, cache);
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
        if (trackedEntityInstanceBody == null) {
            trackedEntityInstanceBody = new TrackedEntityInstanceRandomizer().create( this.entitiesCache, RandomizerContext.EMPTY_CONTEXT(), 5 );
        }

        long time = System.currentTimeMillis();

        boolean hasFailed = false;
        try
        {
            this.response = new AuthenticatedApiActions( this.endpoint, getUserCredentials() ).post( trackedEntityInstanceBody );
        }

        catch ( Exception e )
        {
            recordFailure( System.currentTimeMillis() - time, e.getMessage() );
            hasFailed = true;
        }

        if ( !hasFailed )
        {
            if ( response.statusCode() == 200 )
            {
                recordSuccess( response.getRaw() );
            }
            else
            {
                recordFailure( response.getRaw() );
            }
        }
    }

    public ApiResponse executeAndGetResponse() {
        this.execute();
        return this.response;
    }
}

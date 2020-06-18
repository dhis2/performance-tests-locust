package org.hisp.dhis.tasks.tracker.tei;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.hisp.dhis.actions.RestApiActions;
import org.hisp.dhis.cache.EntitiesCache;
import org.hisp.dhis.dxf2.events.trackedentity.TrackedEntityInstances;
import org.hisp.dhis.random.TrackedEntityInstanceRandomizer;
import org.hisp.dhis.response.dto.ApiResponse;
import org.hisp.dhis.tasks.DhisAbstractTask;

public class AddTeiTask
    extends
    DhisAbstractTask
{
    private EntitiesCache cache;

    private String endpoint = "/api/trackedEntityInstances";

    public AddTeiTask( int weight, EntitiesCache entitiesCache )
    {
        this.weight = weight;
        this.cache = entitiesCache;
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
        TrackedEntityInstances trackedEntityInstances = new TrackedEntityInstanceRandomizer().create( this.cache, 5 );

        long time = System.currentTimeMillis();

        ApiResponse response = null;
        boolean hasFailed = false;
        try
        {
            response = new RestApiActions( this.endpoint ).post( trackedEntityInstances );
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
}

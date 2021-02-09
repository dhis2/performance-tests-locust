package org.hisp.dhis.tasks.tracker.importer;

import com.google.gson.JsonObject;
import org.hisp.dhis.actions.AuthenticatedApiActions;
import org.hisp.dhis.cache.UserCredentials;
import org.hisp.dhis.response.dto.ApiResponse;
import org.hisp.dhis.tasks.DhisAbstractTask;

/**
 * @author Gintare Vilkelyte <vilkelyte.gintare@gmail.com>
 */
public class QueryTrackerEventsTask
    extends
    DhisAbstractTask
{
    private String endpoint = "/api/tracker/events";

    private String query;

    private JsonObject responseBody;

    public QueryTrackerEventsTask( String query )
    {
        this.query = query;
    }

    public QueryTrackerEventsTask( String query, UserCredentials userCredentials ) {
        this.query = query;
        this.userCredentials = userCredentials;
    }

    public int getWeight()
    {
        return 1;
    }

    public String getName()
    {
        return endpoint;
    }

    @Override
    public String getType()
    {
        return "GET";
    }

    public void execute()
    {
        ApiResponse response = new AuthenticatedApiActions( this.endpoint, getUserCredentials()).get(this.query);

        this.responseBody = response.getBody();

        if ( response.statusCode() == 200 )
        {
            recordSuccess( response.getRaw() );
            return;
        }

        recordFailure( response.getRaw() );
    }

    public JsonObject executeAndGetBody()
    {
        execute();
        return responseBody;
    }
}


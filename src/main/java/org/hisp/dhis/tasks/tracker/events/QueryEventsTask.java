package org.hisp.dhis.tasks.tracker.events;

import com.google.gson.JsonObject;
import org.hisp.dhis.actions.AuthenticatedApiActions;
import org.hisp.dhis.cache.UserCredentials;
import org.hisp.dhis.response.dto.ApiResponse;
import org.hisp.dhis.tasks.DhisAbstractTask;

/**
 * @author Gintare Vilkelyte <vilkelyte.gintare@gmail.com>
 */
public class QueryEventsTask
    extends
    DhisAbstractTask
{
    private String endpoint = "/api/events";

    private String query;

    private JsonObject responseBody;

    private boolean saveResponse = false;

    public QueryEventsTask( String query )
    {
        this.query = query;
    }

    public QueryEventsTask( String query, UserCredentials userCredentials ) {
        this.query = query;
        this.userCredentials = userCredentials;
    }

    public int getWeight()
    {
        return 1;
    }

    public String getName()
    {
        return "/events";
    }

    @Override
    public String getType()
    {
        return "GET";
    }

    public void execute()
    {
        ApiResponse response = new AuthenticatedApiActions( this.endpoint, getUserCredentials()).get(this.query);

        if ( saveResponse ) {
            this.responseBody = response.getBody();
        }

        record( response.getRaw() );
    }

    public JsonObject executeAndGetBody()
    {
        this.saveResponse = true;
        execute();
        return responseBody;
    }
}

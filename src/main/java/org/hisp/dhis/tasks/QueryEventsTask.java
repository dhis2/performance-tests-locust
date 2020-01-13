package org.hisp.dhis.tasks;

import com.google.gson.JsonObject;
import org.hisp.dhis.actions.RestApiActions;
import org.hisp.dhis.response.dto.ApiResponse;

/**
 * @author Gintare Vilkelyte <vilkelyte.gintare@gmail.com>
 */
public class QueryEventsTask
    extends
    DhisAbstractTask
{
    private String query = "/api/events/";

    private JsonObject responseBody;

    public QueryEventsTask( String query )
    {
        this.query += query;
    }

    public int getWeight()
    {
        return 1;
    }

    public String getName()
    {
        return "Get events " + this.query;
    }

    public void execute()
    {
        RestApiActions apiActions = new RestApiActions( this.query );
        ApiResponse response = apiActions.get();

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

package org.hisp.dhis.tasks.tracker.events;

import com.google.gson.JsonObject;
import org.hisp.dhis.actions.RestApiActions;
import org.hisp.dhis.response.dto.ApiResponse;
import org.hisp.dhis.tasks.DhisAbstractTask;

/**
 * @author Gintare Vilkelyte <vilkelyte.gintare@gmail.com>
 */
public class QueryEventsTask
    extends
    DhisAbstractTask
{
    private RestApiActions apiActions = new RestApiActions( "/api/events" );

    private String query;

    private JsonObject responseBody;

    public QueryEventsTask( String query )
    {
        this.query = query;
    }

    public int getWeight()
    {
        return 1;
    }

    public String getName()
    {
        return "/events" + this.query;
    }

    @Override
    public String getType()
    {
        return "GET";
    }

    public void execute()
    {
        ApiResponse response = apiActions.get(this.query);

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

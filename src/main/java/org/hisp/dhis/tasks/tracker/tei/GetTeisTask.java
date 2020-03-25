package org.hisp.dhis.tasks.tracker.tei;

import com.google.gson.JsonObject;
import org.hisp.dhis.actions.RestApiActions;
import org.hisp.dhis.request.QueryParamsBuilder;
import org.hisp.dhis.response.dto.ApiResponse;
import org.hisp.dhis.tasks.DhisAbstractTask;

/**
 * @author Gintare Vilkelyte <vilkelyte.gintare@gmail.com>
 */
public class GetTeiTask extends DhisAbstractTask
{
    private JsonObject responseBody;

    private RestApiActions teiActions = new RestApiActions( "/trackedEntityInstances" );

    public GetTeiTask( int weight ) {
        this.weight = weight;
    }

    @Override
    public String getName()
    {
        return "GET /trackedEntityInstances";
    }

    @Override
    public void execute()
        throws Exception
    {
        ApiResponse response = teiActions.get( "", new QueryParamsBuilder().add(  "ou", "DiszpKrYNg8") );

        this.responseBody = response.getBody();

        if ( response.statusCode() == 200 )
        {
            recordSuccess( response.getRaw());
        }
        else
        {
            recordFailure( response.getRaw());
        }
    }

    public JsonObject executeAndGetBody()
        throws Exception
    {
        this.execute();

        return this.responseBody;
    }
}

package org.hisp.dhis.tasks.tracker.tei;

import com.google.gson.JsonObject;
import org.hisp.dhis.actions.RestApiActions;
import org.hisp.dhis.response.dto.ApiResponse;
import org.hisp.dhis.tasks.DhisAbstractTask;

/**
 * @author Gintare Vilkelyte <vilkelyte.gintare@gmail.com>
 */
public class GetTeiTask
    extends DhisAbstractTask
{
    private JsonObject responseBody;
    private String tei;
    private RestApiActions teiActions = new RestApiActions( "/api/trackedEntityInstances" );

    public GetTeiTask( String teiId) {
        this.tei = teiId;
    }

    @Override
    public String getName()
    {
        return "/trackedEntityInstances/$id";
    }

    @Override
    public String getType()
    {
        return "GET";
    }

    @Override
    public void execute()
    {
        ApiResponse response = teiActions.get( tei );

        this.responseBody = response.getBody();

        if ( response.statusCode() == 200 ) {
            this.recordSuccess( response.getRaw() );
            return;
        }

        this.recordFailure( response.getRaw() );

    }

    public JsonObject executeAndGetBody()
    {
        this.execute();
        return responseBody;
    }
}

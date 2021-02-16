package org.hisp.dhis.tasks.tracker.events;

import com.google.gson.JsonObject;
import org.hisp.dhis.actions.AuthenticatedApiActions;
import org.hisp.dhis.actions.RestApiActions;
import org.hisp.dhis.response.dto.ApiResponse;
import org.hisp.dhis.tasks.DhisAbstractTask;

import static com.google.api.client.http.HttpStatusCodes.STATUS_CODE_OK;

/**
 * @author Gintare Vilkelyte <vilkelyte.gintare@gmail.com>
 */
public class PostEventsTask
    extends
    DhisAbstractTask
{
    private JsonObject body;

    private String endpoint = "/api/events";

    public PostEventsTask( JsonObject body )
    {
        this.body = body;
    }

    public int getWeight()
    {
        return 1;
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
        RestApiActions apiActions = new AuthenticatedApiActions(
            this.endpoint , getUserCredentials());

        record( apiActions.post( body ).getRaw() );
    }
}

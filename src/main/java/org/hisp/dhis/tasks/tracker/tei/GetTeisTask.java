package org.hisp.dhis.tasks.tracker.tei;

import com.google.gson.JsonObject;
import org.hisp.dhis.actions.RestApiActions;
import org.hisp.dhis.request.QueryParamsBuilder;
import org.hisp.dhis.response.dto.ApiResponse;
import org.hisp.dhis.tasks.DhisAbstractTask;

/**
 * @author Gintare Vilkelyte <vilkelyte.gintare@gmail.com>
 */
public class GetTeisTask
    extends DhisAbstractTask
{
    private JsonObject responseBody;

    private RestApiActions teiActions = new RestApiActions( "/api/trackedEntityInstances" );

    private boolean savePayload = false;

    public GetTeisTask( ) {
        this.weight = 1;
    }

    public GetTeisTask( int weight ) {
        this.weight = weight;
    }

    @Override
    public String getName()
    {
        return "/trackedEntityInstances";
    }

    @Override
    public String getType()
    {
        return "GET";
    }

    @Override
    public void execute()
        throws Exception
    {
        ApiResponse response = teiActions.get( "", new QueryParamsBuilder().add(  "ou", "DiszpKrYNg8") );

        if ( savePayload ) {
            this.responseBody = response.getBody();
        }

        record( response.getRaw() );
    }

    public JsonObject executeAndGetBody()
        throws Exception
    {
        this.savePayload = true;
        this.execute();

        return this.responseBody;
    }
}

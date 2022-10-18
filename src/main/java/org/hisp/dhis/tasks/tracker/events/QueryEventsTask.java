package org.hisp.dhis.tasks.tracker.events;

import com.google.gson.JsonObject;
import org.hisp.dhis.actions.AuthenticatedApiActions;
import org.hisp.dhis.cache.UserCredentials;
import org.hisp.dhis.response.dto.ApiResponse;
import org.hisp.dhis.tasks.DhisAbstractTask;
import org.hisp.dhis.utils.Randomizer;

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

    public QueryEventsTask( String query, UserCredentials userCredentials, Randomizer randomizer )
    {
        super( 1,randomizer );
        this.query = query;
        this.userCredentials = userCredentials;
    }

    public String getName()
    {
        return "/api/events";
    }

    @Override
    public String getType()
    {
        return "GET";
    }

    public void execute()
    {
        Randomizer rnd = getNextRandomizer( getName() );
        ApiResponse response = new AuthenticatedApiActions( this.endpoint, getUserCredentials(rnd) ).get( this.query );

        if ( saveResponse )
        {
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

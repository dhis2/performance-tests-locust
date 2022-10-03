package org.hisp.dhis.tasks.tracker.importer;

import org.hisp.dhis.actions.AuthenticatedApiActions;
import org.hisp.dhis.cache.UserCredentials;
import org.hisp.dhis.response.dto.ApiResponse;
import org.hisp.dhis.tasks.DhisAbstractTask;

/**
 * @author Gintare Vilkelyte <vilkelyte.gintare@gmail.com>
 */
public class QueryTrackerTeisTask
    extends DhisAbstractTask
{
    private String endpoint = "/api/tracker/trackedEntities";

    private String query = "?ou=DiszpKrYNg8&attribute=TfdH5KvFmMy&filter=TfdH5KvFmMy:GE:Karoline";

    private ApiResponse response;

    private boolean savePayload = false;

    public QueryTrackerTeisTask( int weight, String query, UserCredentials userCredentials )
    {
        super( weight );
        this.query = query;
        this.userCredentials = userCredentials;
    }

    @Override
    public String getName()
    {
        return endpoint;
    }

    @Override
    public String getType()
    {
        return "GET";
    }

    @Override
    public void execute()
    {
        ApiResponse response = new AuthenticatedApiActions( this.endpoint, getUserCredentials() ).get( this.query );

        if ( savePayload )
        {
            this.response = response;
        }

        record( response.getRaw() );
    }

    public ApiResponse executeAndGetResponse()
    {
        savePayload = true;
        this.execute();
        return this.response;
    }
}

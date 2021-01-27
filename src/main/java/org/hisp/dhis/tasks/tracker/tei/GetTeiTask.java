package org.hisp.dhis.tasks.tracker.tei;

import com.google.gson.JsonObject;
import org.hisp.dhis.actions.AuthenticatedApiActions;
import org.hisp.dhis.actions.RestApiActions;
import org.hisp.dhis.cache.UserCredentials;
import org.hisp.dhis.response.dto.ApiResponse;
import org.hisp.dhis.tasks.DhisAbstractTask;

/**
 * @author Gintare Vilkelyte <vilkelyte.gintare@gmail.com>
 */
public class GetTeiTask
    extends DhisAbstractTask
{
    private String endpoint = "/api/trackedEntityInstances";
    private ApiResponse response;
    private String tei;

    public GetTeiTask( String teiId ) {
        this.tei = teiId;
    }

    public GetTeiTask ( String teiId, UserCredentials userCredentials ) {
        this.tei = teiId;
        this.userCredentials = userCredentials;
    }

    @Override
    public String getName()
    {
        return endpoint + "/id";
    }

    @Override
    public String getType()
    {
        return "GET";
    }

    @Override
    public void execute()
    {
        this.response = new AuthenticatedApiActions( endpoint, getUserCredentials() ).get( tei );

        if ( response.statusCode() == 200 ) {
            this.recordSuccess( response.getRaw() );
            return;
        }

        this.recordFailure( response.getRaw() );

    }

    public ApiResponse executeAndGetResponse()
    {
        this.execute();
        return response;
    }
}

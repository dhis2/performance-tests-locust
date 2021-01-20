package org.hisp.dhis.tasks.tracker;

import org.hisp.dhis.actions.AuthenticatedApiActions;
import org.hisp.dhis.cache.UserCredentials;
import org.hisp.dhis.response.dto.ApiResponse;
import org.hisp.dhis.tasks.DhisAbstractTask;

/**
 * @author Gintare Vilkelyte <vilkelyte.gintare@gmail.com>
 */
public class GenerateTrackedEntityAttributeValueTask
    extends DhisAbstractTask
{
    private String teiAttributeId;
    private UserCredentials userCredentials;
    private String endpoint = "/api/trackedEntityAttributes/id/generate";
    private ApiResponse response;

    public GenerateTrackedEntityAttributeValueTask(int weight, String trackedEntityAttributeId, UserCredentials userCredentials ) {
        this.weight = weight;
        this.teiAttributeId = trackedEntityAttributeId;
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
        return "POST";
    }

    @Override
    public void execute()
    {
        AuthenticatedApiActions apiActions = new AuthenticatedApiActions( "", userCredentials );

        response = apiActions.get(  endpoint.replace( "id", teiAttributeId ) );

        if (response.statusCode() == 200) {
            recordSuccess( response.getRaw() );
            return;
        }

        recordFailure( response.getRaw() );

    }

    public ApiResponse executeAndGetResponse()
    {
        this.execute();
        return this.response;
    }
}

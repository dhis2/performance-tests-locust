package org.hisp.dhis.tasks.tracker.importer;

import org.hisp.dhis.actions.AuthenticatedApiActions;
import org.hisp.dhis.cache.UserCredentials;
import org.hisp.dhis.response.dto.ApiResponse;
import org.hisp.dhis.tasks.DhisAbstractTask;
import org.hisp.dhis.utils.Randomizer;

/**
 * @author Gintare Vilkelyte <vilkelyte.gintare@gmail.com>
 */
public class GetImportJobTask
    extends DhisAbstractTask
{
    private String endpoint = "/api/tracker/jobs/";

    private String jobId;

    private ApiResponse response;

    public GetImportJobTask(int weight, UserCredentials userCredentials, String jobId,
                            Randomizer randomizer)
    {
        super( weight, randomizer );
        this.userCredentials = userCredentials;
        this.jobId = jobId;
    }

    @Override
    public String getName()
    {
        return endpoint + "id";
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
        performTaskAndRecord( () -> {
            response = new AuthenticatedApiActions( endpoint + jobId, userCredentials ).get();
            return response;
        } );
    }

    public ApiResponse executeAndGetResponse()
        throws Exception
    {
        execute();
        return response;
    }
}

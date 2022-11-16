package org.hisp.dhis.tasks.tracker.importer;

import org.hisp.dhis.actions.AuthenticatedApiActions;
import org.hisp.dhis.cache.UserCredentials;
import org.hisp.dhis.response.dto.ApiResponse;
import org.hisp.dhis.tasks.DhisAbstractTask;
import org.hisp.dhis.utils.Randomizer;

/**
 * @author Gintare Vilkelyte <vilkelyte.gintare@gmail.com>
 */
public class GetTrackerTeiTask
    extends DhisAbstractTask
{
    private String endpoint = "/api/tracker/trackedEntities";

    private ApiResponse response;

    private String tei;

    public GetTrackerTeiTask(String teiId, UserCredentials userCredentials, Randomizer randomizer)
    {
        super( 1, randomizer );
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
        throws Exception
    {
        this.response = performTaskAndRecord( () -> new AuthenticatedApiActions( endpoint, this.userCredentials ).get( tei ) );
    }

    public ApiResponse executeAndGetResponse()
        throws Exception
    {
        this.execute();
        return response;
    }
}


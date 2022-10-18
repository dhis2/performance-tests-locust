package org.hisp.dhis.tasks.tracker.tei;

import org.hisp.dhis.actions.AuthenticatedApiActions;
import org.hisp.dhis.cache.UserCredentials;
import org.hisp.dhis.response.dto.ApiResponse;
import org.hisp.dhis.tasks.DhisAbstractTask;
import org.hisp.dhis.utils.Randomizer;

/**
 * @author Gintare Vilkelyte <vilkelyte.gintare@gmail.com>
 */
public class GetTeiTask
    extends DhisAbstractTask
{
    private String endpoint = "/api/trackedEntityInstances";

    private ApiResponse response;

    private String tei;

    public GetTeiTask( String teiId, UserCredentials userCredentials, Randomizer randomizer )
    {
        super( 1,randomizer);
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
        Randomizer rnd = getNextRandomizer( getName() );
        this.response = new AuthenticatedApiActions( endpoint, getUserCredentials(rnd) ).get( tei );

        record( response.getRaw() );
    }

    public ApiResponse executeAndGetResponse()
    {
        this.execute();
        return response;
    }
}

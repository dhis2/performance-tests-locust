package org.hisp.dhis.tasks.tracker.tei;

import org.hisp.dhis.actions.AuthenticatedApiActions;
import org.hisp.dhis.cache.UserCredentials;
import org.hisp.dhis.response.dto.ApiResponse;
import org.hisp.dhis.tasks.DhisAbstractTask;
import org.hisp.dhis.utils.Randomizer;

/**
 * @author Marc Pratllusa <marc@dhis2.org>
 */
public class GetEntitiesTask
        extends
        DhisAbstractTask
{
    private static final String ENDPOINT = "/api/tracker/trackedEntities";

    private String identifier = "";

    private final String query;

    private ApiResponse response;

    public GetEntitiesTask(int weight, String query, UserCredentials userCredentials,
                           String customIdentifier, Randomizer randomizer )
    {
        super( weight,randomizer );
        this.query = query;
        this.userCredentials = userCredentials;
        this.identifier = String.format( " ( %s )", customIdentifier );
    }

    @Override
    public String getName()
    {
        return ENDPOINT + this.identifier;
    }

    @Override
    public String getType()
    {
        return "GET";
    }

    @Override
    public void execute()
    {
        this.response = new AuthenticatedApiActions( this.ENDPOINT, this.userCredentials ).get( this.query );

        record( response.getRaw() );
    }

    public ApiResponse executeAndGetResponse()
    {
        this.execute();
        return this.response;
    }
}

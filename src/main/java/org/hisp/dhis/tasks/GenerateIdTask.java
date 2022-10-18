package org.hisp.dhis.tasks;

import org.hisp.dhis.actions.AuthenticatedApiActions;
import org.hisp.dhis.cache.UserCredentials;
import org.hisp.dhis.response.dto.ApiResponse;
import org.hisp.dhis.utils.Randomizer;

import java.util.List;

/**
 * @author Gintare Vilkelyte <vilkelyte.gintare@gmail.com>
 */
public class GenerateIdTask
    extends DhisAbstractTask
{
    private int size = 1;

    private ApiResponse response;

    public GenerateIdTask(UserCredentials userCredentials, Randomizer randomizer)
    {
        super(1, randomizer);
        this.userCredentials = userCredentials;
    }

    public GenerateIdTask( UserCredentials userCredentials, int size, Randomizer randomizer )
    {
        this(userCredentials, randomizer);
        this.size = size;
    }

    @Override
    public String getName()
    {
        return "/system/id";
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
        response = performTaskAndRecord( ()-> new AuthenticatedApiActions( "/api/system", this.userCredentials ).get("/id?limit=" + size) );
    }

    public List<String> executeAndGetResponse()
        throws Exception
    {
        this.execute();
        return response.extractList( "codes" );
    }
}

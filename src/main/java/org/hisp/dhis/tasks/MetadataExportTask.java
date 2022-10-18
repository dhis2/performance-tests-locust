package org.hisp.dhis.tasks;

import com.google.gson.JsonObject;
import org.hisp.dhis.actions.RestApiActions;
import org.hisp.dhis.response.dto.ApiResponse;
import org.hisp.dhis.utils.Randomizer;

/**
 * @author Gintare Vilkelyte <vilkelyte.gintare@gmail.com>
 */
public class MetadataExportTask
    extends DhisAbstractTask
{
    private JsonObject responseBody;

    private String endpoint = "/api/metadata";

    protected MetadataExportTask( Randomizer randomizer )
    {
        super( 1, randomizer );
    }

    public String getName()
    {
        return "Export all metadata";
    }

    @Override
    public String getType()
    {
        return "GET";
    }

    public void execute()
        throws Exception
    {
        RestApiActions apiActions = new RestApiActions( endpoint );

        ApiResponse response = apiActions.get();

        this.responseBody = response.getBody();

        if ( response.statusCode() != 200 )
        {
            recordFailure( response.getRaw() );
            return;
        }

        recordSuccess( response.getRaw() );
    }

    public JsonObject executeAndGetBody()
        throws Exception
    {
        this.execute();

        return responseBody;
    }
}

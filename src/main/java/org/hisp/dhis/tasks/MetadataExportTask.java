package org.hisp.dhis.tasks;

import com.google.gson.JsonObject;
import io.restassured.response.Response;
import org.hisp.dhis.RestAssured;

/**
 * @author Gintare Vilkelyte <vilkelyte.gintare@gmail.com>
 */
public class MetadataExportTask
    extends DhisAbstractTask
{
    private JsonObject responseBody;

    public int getWeight()
    {
        return 1;
    }

    public String getName()
    {
        return "Export all metadata";
    }

    public void execute()
        throws Exception
    {
        Response response = RestAssured.getRestAssured()
            .given()
            .get( "api/metadata" )
            .thenReturn();

        this.responseBody = response.body().as( JsonObject.class );

        if ( response.statusCode() != 200 )
        {
            recordFailure( response );
            return;
        }

        recordSuccess( response );
    }

    public JsonObject executeAndGetBody()
        throws Exception
    {
        this.execute();

        return responseBody;
    }
}

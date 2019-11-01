package org.hisp.dhis.tasks;

import static io.restassured.RestAssured.given;

import com.google.gson.JsonObject;

/**
 * @author Gintare Vilkelyte <vilkelyte.gintare@gmail.com>
 */
public class MetadataExportTask
    extends
    DhisAbstractTask
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
    {
        this.responseBody = executeQuery( () -> given().get( "api/metadata" ).thenReturn() ).as( JsonObject.class );
    }

    public JsonObject executeAndGetBody()
        throws Exception
    {
        this.execute();

        return responseBody;
    }
}

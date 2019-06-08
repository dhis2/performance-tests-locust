package org.hisp.dhis.tasks;

import com.google.gson.JsonObject;
import io.restassured.response.Response;

import static io.restassured.RestAssured.given;

/**
 * @author Gintare Vilkelyte <vilkelyte.gintare@gmail.com>
 */
public class QueryEventsTask
    extends
    DhisAbstractTask
{
    private String query = "/api/events/";

    private JsonObject responseBody;

    public QueryEventsTask( String query )
    {
        this.query += query;
    }

    public int getWeight()
    {
        return 1;
    }

    public String getName()
    {
        return "Get events " + this.query;
    }

    public void execute()
    {
        Response response = given().get( query );

        this.responseBody = response.body().as( JsonObject.class );

        if ( response.statusCode() == 200 )
        {
            recordSuccess( response );
            return;
        }

        recordFailure( response );
    }

    public JsonObject executeAndGetBody()
        throws Exception
    {
        execute();
        return responseBody;
    }
}

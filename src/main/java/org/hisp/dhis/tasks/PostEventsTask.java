package org.hisp.dhis.tasks;

import com.google.gson.JsonObject;
import io.restassured.http.ContentType;
import io.restassured.response.Response;

import static io.restassured.RestAssured.given;
import static io.restassured.RestAssured.post;
import static io.restassured.RestAssured.when;

/**
 * @author Gintare Vilkelyte <vilkelyte.gintare@gmail.com>
 */
public class PostEventsTask
    extends
    DhisAbstractTask
{
    private JsonObject body;

    public PostEventsTask( JsonObject body )
    {
        this.body = body;
    }

    public int getWeight()
    {
        return 1;
    }

    public String getName()
    {
        return "Post events";
    }

    public void execute()
    {
        Response response = given().contentType( ContentType.JSON ).body( body ).when().post( "/api/events" )
            .thenReturn();

        if ( response.statusCode() == 200 )
        {
            recordSuccess( response );
            return;
        }

        recordFailure( response );

    }
}

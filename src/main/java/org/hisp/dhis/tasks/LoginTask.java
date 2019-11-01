package org.hisp.dhis.tasks;

import static io.restassured.RestAssured.*;

import io.restassured.http.ContentType;
import io.restassured.response.Response;

/**
 * @author Gintare Vilkelyte <vilkelyte.gintare@gmail.com>
 */
public class LoginTask
    extends
    DhisAbstractTask
{
    public int getWeight()
    {
        return 1;
    }

    public String getName()
    {
        return "Authenticate";
    }

    public void execute()
    {
        authentication = preemptive().basic( "system", "System123" );

        Response apiResponse = given().contentType( ContentType.TEXT ).when()
            .get( "api/me" ).thenReturn();

        if ( apiResponse.statusCode() == 200 )
        {
            recordSuccess( apiResponse );
            return;
        }

        recordFailure( apiResponse );
    }
}

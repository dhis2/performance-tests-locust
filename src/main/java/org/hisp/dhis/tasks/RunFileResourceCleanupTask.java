package org.hisp.dhis.tasks;

import io.restassured.http.ContentType;
import io.restassured.response.Response;

import static io.restassured.RestAssured.given;

public class RunFileResourceCleanupTask
    extends DhisAbstractTask
{

    // Based on SL database
    private final String JOB_UID = "pd6O228pqr0";

    @Override
    public String getName()
    {
        return "GET /jobConfiguration/{uid}/execute";
    }

    @Override
    public void execute()
        throws Exception
    {
        long time = System.currentTimeMillis();

        Response response = null;
        boolean hasFailed = false;

        try
        {
            response = given().contentType( ContentType.JSON ).when()
                .get( "/api/jobConfigurations/" + JOB_UID + "/execute" ).thenReturn();
        }
        catch ( Exception e )
        {
            recordFailure( System.currentTimeMillis() - time, e.getMessage() );
            hasFailed = true;
        }

        if ( !hasFailed )
        {
            if ( response.statusCode() == 200 )
            {
                recordSuccess( response );
            }
            else
            {
                recordFailure( response );
            }
        }
    }
}

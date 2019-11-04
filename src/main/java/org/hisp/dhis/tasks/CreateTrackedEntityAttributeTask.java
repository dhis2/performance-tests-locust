package org.hisp.dhis.tasks;

import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.hisp.dhis.cache.GeneratedTrackedEntityAttribute;

import java.util.UUID;

import static io.restassured.RestAssured.given;

public class CreateTrackedEntityAttributeTask
    extends
    DhisAbstractTask
{
    private String id;

    public int getWeight()
    {
        return 1;
    }

    public String getName()
    {
        return "POST /api/trackedEntityAttribute";
    }

    public void execute()
    {
        long time = System.currentTimeMillis();

        Response response = null;
        boolean hasFailed = false;

        try
        {

            GeneratedTrackedEntityAttribute generatedAttribute = new GeneratedTrackedEntityAttribute();
            generatedAttribute.setName( generatedAttribute.getName() + UUID.randomUUID() );
            generatedAttribute.setShortName( ""+UUID.randomUUID() );
            response = given().contentType( ContentType.JSON ).body( generatedAttribute ).when()
                .post( "/api/trackedEntityAttributes" ).thenReturn();
            if ( response.getStatusCode() == 201 ) {
                id = response.jsonPath().get( "response.uid");
            }
        }
        catch ( Exception e )
        {
            recordFailure( System.currentTimeMillis() - time, e.getMessage() );
            hasFailed = true;
        }

        if ( !hasFailed )
        {
            if ( response.statusCode() == 201 )
            {
                recordSuccess( response );
            }
            else
            {
                recordFailure( response );
            }
        }
    }

    public String executeAndGetId()
        throws Exception
    {
        this.execute();

        return id;
    }
}

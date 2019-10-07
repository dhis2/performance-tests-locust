package org.hisp.dhis.tasks;

import static io.restassured.RestAssured.given;

import org.hisp.dhis.cache.EntitiesCache;
import org.hisp.dhis.dxf2.events.trackedentity.TrackedEntityInstances;
import org.hisp.dhis.random.TrackedEntityInstanceRandomizer;

import io.restassured.http.ContentType;
import io.restassured.response.Response;

public class AddTeiTask
    extends
    DhisAbstractTask
{
    private EntitiesCache cache;

    public AddTeiTask( int weight, EntitiesCache entitiesCache )
    {
        this.weight = weight;
        this.cache = entitiesCache;
    }

    public int getWeight()
    {
        return this.weight;
    }

    public String getName()
    {
        return "POST /api/trackedEntityInstances";
    }

    public void execute()
    {
        TrackedEntityInstances trackedEntityInstances = new TrackedEntityInstanceRandomizer().create( this.cache, 5 );

        long time = System.currentTimeMillis();

        Response response = null;
        boolean hasFailed = false;
        try
        {
            response = given().contentType( ContentType.JSON ).body( trackedEntityInstances ).when()
                .post( "/api/trackedEntityInstances" ).thenReturn();
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

package org.hisp.dhis.tasks;

import com.github.myzhan.locust4j.Locust;
import io.restassured.http.ContentType;
import io.restassured.response.Response;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static io.restassured.RestAssured.given;

public class ReserveTrackedEntityAttributeValuesTask
    extends
    DhisAbstractTask
{
    public ReserveTrackedEntityAttributeValuesTask( int weight )
    {
        this.weight = weight;
    }

    public int getWeight()
    {
        return this.weight;
    }

    public String getName()
    {
        return "POST /api/trackedEntityAttribute/{id}/generateAndReserve";
    }

    public void execute()
    {
        long time = System.currentTimeMillis();

        List<Response> setupResponses = null;

        Response response = null;

        boolean hasFailed = false;

        try
        {
            String attributeId = new CreateTrackedEntityAttributeTask().executeAndGetId();

            setupResponses = IntStream.range( 0, 10 ).mapToObj( r -> given()
                    .contentType( ContentType.JSON )
                    .queryParam( "numberToReserve", 800 )
                    .when()
                    .get( "/api/trackedEntityAttributes/" + attributeId + "/generateAndReserve" )
                    .thenReturn())
            .collect( Collectors.toList());

            response = given()
                .contentType( ContentType.JSON )
                .queryParam( "numberToReserve", 800 )
                .when()
                .get( "/api/trackedEntityAttributes/" + attributeId + "/generateAndReserve" )
                .thenReturn();

        }
        catch ( Exception e )
        {
            recordFailure( System.currentTimeMillis() - time, e.getMessage() );
            hasFailed = true;
        }

        if ( !hasFailed )
        {

            if (setupResponses.stream().allMatch( r -> r.statusCode() == 200 )) {
                record( response );
            } else {
                Response failureResponse = setupResponses.stream().filter( r -> r.statusCode() != 200 ).findFirst().get();
                Locust.getInstance().recordFailure( "http", getName() + " SETUP",
                    System.currentTimeMillis() - time, failureResponse.getBody().print() );
            }
        }
    }

    private void record( Response response )
    {
        if(response.statusCode() == 200 ) {
            recordSuccess( response );
        } else {
            recordFailure( response );
        }
    }
}

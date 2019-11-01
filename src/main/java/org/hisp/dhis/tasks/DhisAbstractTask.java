package org.hisp.dhis.tasks;

import static io.restassured.RestAssured.given;

import java.util.function.Supplier;

import io.restassured.specification.RequestSpecification;
import org.hisp.dhis.tasks.httpUtils.QueryParams;

import com.github.myzhan.locust4j.AbstractTask;
import com.github.myzhan.locust4j.Locust;

import io.restassured.http.ContentType;
import io.restassured.response.Response;

/**
 * @author Gintare Vilkelyte <vilkelyte.gintare@gmail.com>
 */
public abstract class DhisAbstractTask
    extends
    AbstractTask
{
    protected int weight;

    public int getWeight()
    {
        return this.weight;
    }

    public abstract String getName();

    public abstract void execute()
        throws Exception;

    public void recordSuccess( Response response )
    {
        Locust.getInstance().recordSuccess( "http", getName(), response.getTime(),
            response.getBody().asByteArray().length );
    }

    public void recordSuccess( long time, long length )
    {
        Locust.getInstance().recordSuccess( "http", getName(), time, length );
    }

    public void recordFailure( long time, String message )
    {
        Locust.getInstance().recordFailure( "http", getName(), time, message );
    }

    public void recordFailure( Response response )
    {
        Locust.getInstance().recordFailure( "http", getName(), response.getTime(), response.getBody().print() );

    }

    /**
     * This method allows to wrap any RestAssured query into a common exception
     * handling and success/failure recording
     *
     * <pre>
     * {@code
     *  executeQuery( () -> given().contentType( ContentType.JSON ).body( trackedEntityInstances ).when()
     *             .post( "/api/trackedEntityInstances" ).thenReturn() );
     *
     * }
     * </pre>
     * 
     * @param query a RestAssure query
     */
    public Response executeQuery( Supplier<Response> query )
    {

        long time = System.currentTimeMillis();
        Response response = null;
        boolean hasFailed = false;
        try
        {
            response = query.get();
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
        return response;
    }

    /**
     * Executes a HTTP POST with a payload
     * 
     * @param uri a target uri (e.g. /api/me)
     * @param body a Java object that will be serialized to Json
     * @return a Response object
     */
    public Response post( String uri, Object body )
    {
        return given()
            .contentType( ContentType.JSON )
            .body( body )
            .when()
            .post( uri )
            .thenReturn();
    }

    /**
     * Executes a HTTP POST with a payload and query parameters
     *
     * @param uri a target uri (e.g. /api/me)
     * @param queryParams a Java object that will be serialized to Json
     * @param body a Response object
     * @return a Response object
     */
    public Response post( String uri, QueryParams queryParams, Object body )
    {

        return given()
            .contentType( ContentType.JSON )
            .body( body ).when()
            .post( uri + "?" + queryParams.getParams() ).thenReturn();

    }
    
    public Response get( String uri )
    {
        return given()
        .contentType( ContentType.JSON )
        .when()
        .get( uri )
        .thenReturn();
    }

    public Response get( String uri, QueryParams queryParams )
    {
        return given()
            .contentType( ContentType.JSON )
            .when()
            .get( uri + "?" + queryParams.getParams() )
            .thenReturn();
    }

}

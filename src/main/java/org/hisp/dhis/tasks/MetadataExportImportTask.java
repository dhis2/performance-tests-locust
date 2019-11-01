package org.hisp.dhis.tasks;

import static io.restassured.RestAssured.given;

import org.hisp.dhis.tasks.httpUtils.QueryParams;

import com.google.gson.JsonObject;

import io.restassured.http.ContentType;
import io.restassured.response.Response;

/**
 * @author Gintare Vilkelyte <vilkelyte.gintare@gmail.com>
 */
public class MetadataExportImportTask
    extends
    DhisAbstractTask
{
    public int getWeight()
    {
        return 1;
    }

    public String getName()
    {
        return "Metadata export/import";
    }

    public void execute()
        throws Exception
    {
        new LoginTask().execute();

        JsonObject metadata = new MetadataExportTask().executeAndGetBody();

        long time = System.currentTimeMillis();

        QueryParams queryParam = QueryParams.create()
            .add( "async", "true" )
            .add( "importMode", "COMMIT" )
            .add( "identifier", "UID" )
            .add( "importReportMode", "ERRORS" )
            .add( "preheatMode", "REFERENCE" )
            .add( "atomicMode", "ALL" )
            .add( "mergeMode", "MERGE" )
            .add( "flushMode", "AUTO" )
            .add( "skipSharing", "false" )
            .add( "skipValidation", "false" )
            .add( "async", "true" )
            .add( "inclusionStrategy", "NON_NULL" )
            .add( "importStrategy", "CREATE_AND_UPDATE" );

        Response response = executeQuery(() -> post("/api/metadata", queryParam, metadata));

        if ( response.statusCode() != 200 )
        {
            recordFailure( response );
            return;
        }

        String url = response.jsonPath().getString( "response.relativeNotifierEndpoint" );

        response = isCompleted( url );

        while ( !response.jsonPath().getList( "completed" ).contains( true ) )
        {
            Thread.sleep( 100 );
            response = isCompleted( url );
        }

        time = System.currentTimeMillis() - time;

        if ( response.statusCode() == 200 )
        {
            recordSuccess( time, response.body().asByteArray().length );
            return;
        }

        recordFailure( time, response.body().print() );
    }

    private Response isCompleted( String url )
    {
        return given().contentType( ContentType.JSON ).get( url ).thenReturn();
    }
}

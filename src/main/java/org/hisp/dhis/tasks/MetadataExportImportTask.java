package org.hisp.dhis.tasks;

import com.google.gson.JsonObject;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.hisp.dhis.RestAssured;

/**
 * @author Gintare Vilkelyte <vilkelyte.gintare@gmail.com>
 */
public class MetadataExportImportTask
    extends DhisAbstractTask
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

        Response response = RestAssured.getRestAssured()
            .given()
            .contentType( ContentType.JSON )
            .body( metadata )
            .post(
                "api/metadata.json?async=true&importMode=COMMIT&identifier=UID&importReportMode=ERRORS&preheatMode=REFERENCE&importStrategy=CREATE_AND_UPDATE&atomicMode=ALL&mergeMode=MERGE&flushMode=AUTO&skipSharing=false&skipValidation=false&async=true&inclusionStrategy=NON_NULL" )
            .thenReturn();

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

        return RestAssured.getRestAssured()
            .given()
            .contentType( ContentType.JSON )
            .get( url )
            .thenReturn();
    }
}

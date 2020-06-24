package org.hisp.dhis.tasks.tracker;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.hisp.dhis.SuperclassExclusionStrategy;
import org.hisp.dhis.actions.RestApiActions;
import org.hisp.dhis.request.QueryParamsBuilder;
import org.hisp.dhis.response.dto.ApiResponse;
import org.hisp.dhis.tasks.DhisAbstractTask;
import org.hisp.dhis.tasks.LoginTask;

import java.io.FileNotFoundException;
import java.io.FileReader;

/**
 * @author Morten Svanæs <msvanaes@dhis2.org>
 */
public class MetadataImportTask extends DhisAbstractTask
{
    private final String metadataFilename;

    public MetadataImportTask( String metadataFilename )
    {
        this.metadataFilename = metadataFilename;
    }

    public String getName()
    {
        return "Metadata import";
    }

    public void execute()
        throws Exception
    {
        new LoginTask().execute();

        postMetadata();
    }

    private void postMetadata()
        throws Exception
    {
        Gson gson = new GsonBuilder()
            .setDateFormat( "yyyy-MM-dd" )
            .addDeserializationExclusionStrategy( new SuperclassExclusionStrategy() )
            .addSerializationExclusionStrategy( new SuperclassExclusionStrategy() )
            .create();

        JsonParser parser = new JsonParser();
        JsonElement jsonElement = parser.parse( new FileReader( this.metadataFilename ) );
        JsonObject jsonObject = jsonElement.getAsJsonObject();

        long time = System.currentTimeMillis();

        QueryParamsBuilder queryParamsBuilder = new QueryParamsBuilder()
            .add( "async", "true" )
            .add( "importMode", "COMMIT" )
            .add( "identifier", "UID" )
            .add( "importReportMode", "ERROR" )
            .add( "preheatMode", "REFERENCE" )
            .add( "importStrategy", "CREATE_AND_UPDATE" )
            .add( "atomicMode", "ALL" )
            .add( "mergeMode", "MERGE" )
            .add( "flushMode", "AUTO" )
            .add( "skipSharing", "false" )
            .add( "skipValidation", "false" )
            .add( "inclusionStrategy", "NON_NULL" );

        ApiResponse response = new RestApiActions( "/api/metadata" ).post( jsonObject );
        if ( response.statusCode() != 200 )
        {
            recordFailure( response.getRaw() );
            throw new Exception("Failed to import metadata");
        }

        String body = response.getRaw().getBody().print();
        String total = response.extractString( "stats.total" );
        String created = response.extractString( "stats.created" );
        String updated = response.extractString( "stats.updated" );

        if ( Integer.parseInt( total ) == 0 )
        {
            time = System.currentTimeMillis() - time;
            recordFailure( time, response.getRaw().body().print() );
            return;
        }

        time = System.currentTimeMillis() - time;

        recordSuccess( time, response.getRaw().body().asByteArray().length );
    }

    private ApiResponse isCompleted( String uid )
    {
        return new RestApiActions( String.format( "api/tracker/jobs/%s", uid ) ).get();
    }

    private ApiResponse getReport( String uid )
    {
        return new RestApiActions( String.format( "api/tracker/jobs/%s/report", uid ) ).get();
    }

}

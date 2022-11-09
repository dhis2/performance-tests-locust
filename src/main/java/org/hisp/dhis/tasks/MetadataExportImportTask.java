package org.hisp.dhis.tasks;

import com.google.gson.JsonObject;
import org.hisp.dhis.actions.RestApiActions;
import org.hisp.dhis.request.QueryParamsBuilder;
import org.hisp.dhis.response.dto.ApiResponse;
import org.hisp.dhis.utils.Randomizer;

/**
 * @author Gintare Vilkelyte <vilkelyte.gintare@gmail.com>
 */
public class MetadataExportImportTask
    extends DhisAbstractTask
{
    public MetadataExportImportTask(int weight, Randomizer randomizer)
    {
        super( weight, randomizer );
    }

    public String getName()
    {
        return "Metadata export/import";
    }

    @Override
    public String getType()
    {
        return "https";
    }

    public void execute()
        throws Exception
    {
        Randomizer rnd = getNextRandomizer( getName() );
        JsonObject metadata = new MetadataExportTask( rnd ).executeAndGetBody();

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

        ApiResponse response = new RestApiActions( "api/metadata" )
            .post( metadata, queryParamsBuilder );

        if ( response.statusCode() != 200 )
        {
            recordFailure( response.getRaw() );
            return;
        }

        String url = response.extractString( "response.relativeNotifierEndpoint" );

        response = isCompleted( url );

        while ( !response.extractList( "completed" ).contains( true ) )
        {
            Thread.sleep( 100 );
            response = isCompleted( url );
        }

        time = System.currentTimeMillis() - time;

        if ( response.statusCode() == 200 )
        {
            recordSuccess( time, response.getRaw().body().asByteArray().length );
            return;
        }

        recordFailure( time, response.getRaw().body().print() );
    }

    private ApiResponse isCompleted( String url )
    {
        return new RestApiActions( url ).get();
    }
}

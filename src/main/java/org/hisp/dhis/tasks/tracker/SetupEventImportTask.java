package org.hisp.dhis.tasks.tracker;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.hisp.dhis.SuperclassExclusionStrategy;
import org.hisp.dhis.actions.RestApiActions;
import org.hisp.dhis.cache.EntitiesCache;
import org.hisp.dhis.cache.Program;
import org.hisp.dhis.random.EnrollmentRandomizer;
import org.hisp.dhis.random.TrackedEntityRandomizer;
import org.hisp.dhis.request.QueryParamsBuilder;
import org.hisp.dhis.response.dto.ApiResponse;
import org.hisp.dhis.tasks.DhisAbstractTask;
import org.hisp.dhis.tasks.LoginTask;
import org.hisp.dhis.tracker.bundle.TrackerBundleParams;

import java.util.Map;

/**
 * @author Gintare Vilkelyte <vilkelyte.gintare@gmail.com>
 * @author Morten Svanæs <msvanaes@dhis2.org>
 */
public class SetupEventImportTask
    extends DhisAbstractTask
{
    private final Map<String, String> idMap;

    private final Program program;

    private EntitiesCache cache;

    public SetupEventImportTask( Map<String, String> idMap, Program program, EntitiesCache entitiesCache )
    {
        this.idMap = idMap;
        this.cache = entitiesCache;
        this.program = program;
    }

    public String getName()
    {
        return "Tracker import";
    }

    public void execute()
        throws Exception
    {
        new LoginTask().execute();


        TrackerBundleParams teiParams = new TrackedEntityRandomizer()
            .createBundle( this.idMap, program, this.cache );

        TrackerBundleParams enrollParams = new EnrollmentRandomizer()
            .createBundle( this.idMap, program, this.cache );


        doPost( teiParams );
        doPost( enrollParams );
    }

    private void doPost( TrackerBundleParams trackerBundleParams )
        throws Exception
    {
        Gson gson = new GsonBuilder().setDateFormat( "yyyy-MM-dd" )
            .addDeserializationExclusionStrategy( new SuperclassExclusionStrategy() )
            .addSerializationExclusionStrategy( new SuperclassExclusionStrategy() )
            .create();

//        String json = gson.toJson( trackerBundleParams );

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

        ApiResponse response = new RestApiActions( "api/tracker/sync" ).post( trackerBundleParams );
        if ( response.statusCode() != 200 )
        {
            recordFailure( response.getRaw() );
            throw new Exception( "Failed to import!" );
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

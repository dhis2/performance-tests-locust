package org.hisp.dhis.tasks.tracker;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.hisp.dhis.SuperclassExclusionStrategy;
import org.hisp.dhis.actions.RestApiActions;
import org.hisp.dhis.cache.CategoryOptionCombo;
import org.hisp.dhis.cache.EntitiesCache;
import org.hisp.dhis.cache.Program;
import org.hisp.dhis.random.EventRandomizer;
import org.hisp.dhis.request.QueryParamsBuilder;
import org.hisp.dhis.response.dto.ApiResponse;
import org.hisp.dhis.tasks.DhisAbstractTask;
import org.hisp.dhis.tasks.LoginTask;
import org.hisp.dhis.tracker.bundle.TrackerBundleParams;

import java.util.Map;

/**
 * @author Gintare Vilkelyte <vilkelyte.gintare@gmail.com>
 * @author Morten Svanæs <msvanaes@dhis.org>
 */
public class EventImporSyncTask
    extends DhisAbstractTask
{
    private final CategoryOptionCombo defaultCategoryCombo;

    private Program program;

    private int weight;

    private EntitiesCache cache;

    private Map<String, String> teiEnMap;

    public EventImporSyncTask( Map<String, String> teiEnMap, Program program,
        CategoryOptionCombo defaultCategoryCombo, EntitiesCache entitiesCache )
    {
        this.cache = entitiesCache;
        this.teiEnMap = teiEnMap;
        this.program = program;
        this.defaultCategoryCombo = defaultCategoryCombo;
    }

    public int getWeight()
    {
        return this.weight;
    }

    public String getName()
    {
        return "Tracker import";
    }

    public void execute()
        throws Exception
    {
        new LoginTask().execute();

        TrackerBundleParams trackerBundleParams = new EventRandomizer()
            .createBundle( teiEnMap, program, this.defaultCategoryCombo, this.cache );

        String json = getJson( trackerBundleParams );

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
        String body = response.getRaw().getBody().print();
        if ( response.statusCode() != 200 )
        {
            recordFailure( response.getRaw() );
            return;
        }

        String created = response.extractString( "stats.created" );
        if ( Integer.parseInt( created ) != trackerBundleParams.getEvents().size() )
        {
            time = System.currentTimeMillis() - time;
            recordFailure( time, response.getRaw().body().print() );
            return;
        }

        time = System.currentTimeMillis() - time;

        recordSuccess( time, response.getRaw().body().asByteArray().length );
    }

    private String getJson( TrackerBundleParams trackerBundleParams )
    {
        Gson gson = new GsonBuilder().setDateFormat( "yyyy-MM-dd" )
            .addDeserializationExclusionStrategy( new SuperclassExclusionStrategy() )
            .addSerializationExclusionStrategy( new SuperclassExclusionStrategy() )
            .create();

        return gson.toJson( trackerBundleParams );
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

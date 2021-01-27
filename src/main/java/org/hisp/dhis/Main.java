package org.hisp.dhis;

import com.github.myzhan.locust4j.Locust;
import com.google.gson.GsonBuilder;
import io.restassured.RestAssured;
import io.restassured.config.DecoderConfig;
import io.restassured.config.EncoderConfig;
import io.restassured.config.ObjectMapperConfig;
import io.restassured.mapper.ObjectMapperType;
import org.hisp.dhis.cache.EntitiesCache;
import org.hisp.dhis.locust.LocustConfig;
import org.hisp.dhis.locust.LocustSlave;
import org.hisp.dhis.tasks.LoginTask;
import org.hisp.dhis.tasks.aggregate.AddDataValueTask;
import org.hisp.dhis.tasksets.aggregate.Android_syncDataValuesTaskSet;
import org.hisp.dhis.tasksets.tracker.Android_syncTeisTaskSet;
import org.hisp.dhis.tasksets.tracker.Capture_addEventTaskSet;
import org.hisp.dhis.tasksets.tracker.TrackerCapture_addTeiTaskSet;
import org.hisp.dhis.tasksets.tracker.TrackerCapture_searchForTeiTaskSet;

import java.io.IOException;

import static io.restassured.config.RestAssuredConfig.config;
import static org.aeonbits.owner.ConfigFactory.create;
import static org.hisp.dhis.utils.CacheUtils.*;

/**
 * @author Gintare Vilkelyte <vilkelyte.gintare@gmail.com>
 */
public class Main
{
    private static final LocustConfig cfg = create( LocustConfig.class );

    public static void main( String[] args )
        throws IOException
    {
        RestAssured.config = config().
            decoderConfig(
                new DecoderConfig( "UTF-8" )
            ).encoderConfig(
            new EncoderConfig( "UTF-8", "UTF-8" )
        ).objectMapperConfig(
            new ObjectMapperConfig()
                .defaultObjectMapperType( ObjectMapperType.GSON )
                .gsonObjectMapperFactory( ( type, s ) -> new GsonBuilder().setDateFormat( "yyyy-MM-dd" ).create() )

        );

        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();

        RestAssured.baseURI = cfg.targetUri();
        EntitiesCache cache;

        new LoginTask( cfg.adminUsername(), cfg.adminPassword() ).execute();

        if ( !cacheExists() )
        {
            System.out.println( "cache not found. Hold on while a new cache is created." );
            cache = createAndSerializeCache(cfg);
        }
        else
        {
            try
            {
                cache = deserializeCache();
            }
            catch ( Exception e )
            {
                System.out.println( "Error deserializing cache. Recreating cache file..." + e );
                cache = createAndSerializeCache(cfg);
            }
        }

        System.out.println( "cache loaded from " + getCachePath() );

        Locust locust = LocustSlave.newInstance().init();

        /*locust.run(
            new Android_syncTeisTaskSet( 1, cache ),
            new AddDataValueTask( 1, cache ),
            new Android_syncDataValuesTaskSet( 1, cache ),
            new TrackerCapture_addTeiTaskSet( 1, cache ),
            new Capture_addEventTaskSet( 1, cache ) );*/

        locust.dryRun(
            new Android_syncTeisTaskSet( 1,cache )
        );
        /*locust.dryRun(
                new QueryFilterTeiTask( 3 ),
                new GetHeavyAnalyticsTask( 1, cfg.analyticsApiVersion() ),
                new GetHeavyAnalyticsRandomTask( 1, cfg.analyticsApiVersion(), cache ),
                new AddTeiTask( 5, cache ),
                new FilterTeiTask( 5 ),
                new CreateTrackedEntityAttributeTask( 5 ),
                //new MetadataExportImportTask( 1 ),
                new ReserveTrackedEntityAttributeValuesTask( 1 ),
                new GetAndUpdateEventsTask( 2, "?orgUnit=DiszpKrYNg8" ),
                new GetAndUpdateTeiTask( 2, cache ),
                new AddEventsTask(3, cache)

        );*/

    }
}

package org.hisp.dhis;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.myzhan.locust4j.AbstractTask;
import com.github.myzhan.locust4j.Locust;
import com.github.myzhan.locust4j.ratelimit.RampUpRateLimiter;
import com.github.myzhan.locust4j.ratelimit.StableRateLimiter;
import com.google.gson.*;
import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.config.DecoderConfig;
import io.restassured.config.EncoderConfig;
import io.restassured.config.ObjectMapperConfig;
import io.restassured.filter.cookie.CookieFilter;
import io.restassured.filter.session.SessionFilter;
import io.restassured.http.ContentType;
import io.restassured.mapper.ObjectMapperType;
import io.restassured.path.json.mapper.factory.DefaultJackson2ObjectMapperFactory;
import io.restassured.specification.RequestSpecification;
import org.hisp.dhis.cache.EntitiesCache;
import org.hisp.dhis.commons.config.JacksonObjectMapperConfig;
import org.hisp.dhis.locust.LocustConfig;
import org.hisp.dhis.locust.LocustSlave;
import org.hisp.dhis.tasks.LoginTask;
import org.hisp.dhis.tasks.aggregate.AddDataValueTask;
import org.hisp.dhis.tasks.analytics.GetAnalyticsTask;
import org.hisp.dhis.tasks.analytics.LoadDashboardTask;
import org.hisp.dhis.tasks.tracker.PostRelationshipTask;
import org.hisp.dhis.tasks.tracker.importer.AddTrackerDataTask;
import org.hisp.dhis.tasks.tracker.importer.AddTrackerEventsTask;
import org.hisp.dhis.tasks.tracker.importer.AddTrackerTeiTask;
import org.hisp.dhis.tasksets.aggregate.Android_syncDataValuesTaskSet;
import org.hisp.dhis.tasksets.tracker.Android_syncTeisTaskSet;
import org.hisp.dhis.tasksets.tracker.Capture_addEventTaskSet;
import org.hisp.dhis.tasksets.tracker.TrackerCapture_addTeiTaskSet;
import org.hisp.dhis.tasksets.tracker.TrackerCapture_searchForTeiTaskSet;
import org.hisp.dhis.tasksets.tracker.importer.Android_importer_syncTeisTaskSet;
import org.hisp.dhis.tasksets.tracker.importer.Capture_importer_addEventTaskSet;
import org.hisp.dhis.tasksets.tracker.importer.TrackerCapture_importer_addTeiTaskSet;
import org.hisp.dhis.tasksets.tracker.importer.TrackerCapture_importer_searchForTeiTaskSet;
import org.hisp.dhis.utils.AuthFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import java.io.IOException;
import java.lang.reflect.Type;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

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
                .gsonObjectMapperFactory( ( type, s ) ->
                    new GsonBuilder().setDateFormat( "yyyy-MM-dd" )
                        .registerTypeAdapter( Instant.class,
                            (JsonSerializer<Instant>) ( src, typeOfSrc, context ) -> new JsonPrimitive( src.toString() ) ).create() )
        );

        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
        RestAssured.requestSpecification = defaultRequestSpecification();
        RestAssured.baseURI = cfg.targetUri();
        EntitiesCache cache;

        new LoginTask( cfg.adminUsername(), cfg.adminPassword() ).execute();

        if ( !cacheExists() )
        {
            System.out.println( "cache not found. Hold on while a new cache is created." );
            cache = createAndSerializeCache();
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
                cache = createAndSerializeCache();
            }
        }

        System.out.println( "cache loaded from " + getCachePath() );

        Locust locust = LocustSlave.newInstance().init();

        runNewAndOldImporterTests( locust, cache );
    }

    private static void runNewAndOldImporterTests( Locust locust, EntitiesCache cache )
    {locust.run(
        new Android_importer_syncTeisTaskSet( 1, cache ),
        new Capture_importer_addEventTaskSet( 1, cache ),
        //new TrackerCapture_importer_searchForTeiTaskSet( 1, cache ),
        new TrackerCapture_importer_addTeiTaskSet( 1, cache ),
        new TrackerCapture_addTeiTaskSet( 1, cache ),
        //new TrackerCapture_searchForTeiTaskSet( 1, cache ),
        new Capture_addEventTaskSet( 1, cache ),
        new Android_syncTeisTaskSet( 1, cache ),
        new PostRelationshipTask( 1, cache )
    );
    }

    private static List<AbstractTask> getOldImporterTests( EntitiesCache cache ) {
        return Arrays.asList(
            new TrackerCapture_addTeiTaskSet( 1, cache ),
            //new TrackerCapture_searchForTeiTaskSet( 1, cache ),
            new Capture_addEventTaskSet( 1, cache ),
            new Android_syncTeisTaskSet( 1, cache ),
            new PostRelationshipTask( 1, cache )
        );
    }

    public static void runCovaxTests(Locust locust, EntitiesCache cache ) {
        locust.run(
            new Android_syncTeisTaskSet( 2, cache ),
            new AddDataValueTask( 2, cache ),
            new Android_syncDataValuesTaskSet( 2, cache ),
            new TrackerCapture_addTeiTaskSet( 2, cache ),
            new Capture_addEventTaskSet( 2, cache ),
            new TrackerCapture_searchForTeiTaskSet( 2, cache ),
            new LoadDashboardTask( 1, cache ),
            new PostRelationshipTask( 2, cache )
        );
    }

    public static void runAndroidTests( Locust locust, EntitiesCache cache) {
        locust.run(
            new Android_syncTeisTaskSet( 2,cache, 1 ),
            new Android_syncTeisTaskSet( 2, cache, 10 ),
            new Android_syncTeisTaskSet( 2, cache, 20 )
        );
    }
    public static void runNewImporterTests( Locust locust, EntitiesCache cache) {
        locust.run(
            new Android_importer_syncTeisTaskSet( 1, cache ),
            new Capture_importer_addEventTaskSet( 1, cache ),
            new TrackerCapture_importer_searchForTeiTaskSet( 1, cache ),
            new TrackerCapture_importer_addTeiTaskSet( 1, cache )
        );
    }


    private static RequestSpecification defaultRequestSpecification()
    {
        RequestSpecBuilder requestSpecification = new RequestSpecBuilder();
        requestSpecification.addFilter( new AuthFilter() );

        return requestSpecification.build();
    }

    public static void runTest(Locust locust, EntitiesCache cache) {
        locust.dryRun( new PostRelationshipTask( 1, cache ) );
    }
}

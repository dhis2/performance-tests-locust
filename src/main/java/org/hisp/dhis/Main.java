package org.hisp.dhis;

import static io.restassured.config.RestAssuredConfig.config;
import static org.aeonbits.owner.ConfigFactory.create;
import static org.hisp.dhis.utils.CacheUtils.cacheExists;
import static org.hisp.dhis.utils.CacheUtils.deserializeCache;
import static org.hisp.dhis.utils.CacheUtils.getCachePath;
import static org.hisp.dhis.utils.CacheUtils.serializeCache;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import org.hisp.dhis.cache.EntitiesCache;
import org.hisp.dhis.locust.LocustConfig;
import org.hisp.dhis.locust.LocustSlave;
import org.hisp.dhis.tasks.AddTeiTask;
import org.hisp.dhis.tasks.GetHeavyAnalyticsRandomTask;
import org.hisp.dhis.tasks.GetHeavyAnalyticsTask;
import org.hisp.dhis.tasks.LoginTask;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.myzhan.locust4j.Locust;

import io.restassured.RestAssured;
import io.restassured.config.ObjectMapperConfig;

/**
 * @author Gintare Vilkelyte <vilkelyte.gintare@gmail.com>
 */
public class Main
{
    private static LocustConfig cfg = create( LocustConfig.class );

    public static void main( String[] args )
        throws IOException
    {

        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
        // Configure RestAssured mapper to convert any date into DHIS2 format

        RestAssured.config = config()
            .objectMapperConfig( new ObjectMapperConfig().jackson2ObjectMapperFactory( ( type, s ) -> {
                ObjectMapper om = new ObjectMapper().findAndRegisterModules();
                DateFormat df = new SimpleDateFormat( "yyyy-MM-dd" );
                om.setDateFormat( df );
                return om;
            } ) );

        RestAssured.baseURI = cfg.targetUri();
        EntitiesCache cache;

        new LoginTask().execute();

        if ( !cacheExists() )
        {
            System.out.println( "cache not found. Hold on while a new cache is created." );
            cache = new EntitiesCache();
            cache.loadAll();
            serializeCache( cache );
        }
        else
        {
            cache = deserializeCache();
        }
        System.out.println( "cache loaded from " + getCachePath() );

        LocustSlave locustSlave = LocustSlave.newInstance();

        Locust locust = locustSlave.init();

        locust.run(
                new AddTeiTask( 50, cache ),
                new GetHeavyAnalyticsTask( 30, cfg.analyticsApiVersion() ),
                new GetHeavyAnalyticsRandomTask( 30, cfg.analyticsApiVersion(), cache )
        );
    }
}

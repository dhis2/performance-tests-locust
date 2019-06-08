package org.hisp.dhis;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import org.aeonbits.owner.ConfigFactory;
import org.hisp.dhis.cache.EntitiesCache;
import org.hisp.dhis.locust.LocustConfig;
import org.hisp.dhis.locust.LocustSlave;
import org.hisp.dhis.random.TrackedEntityInstanceRandomizer;
import org.hisp.dhis.tasks.AddTeiTask;
import org.hisp.dhis.tasks.LoginTask;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.myzhan.locust4j.Locust;

import io.restassured.RestAssured;
import io.restassured.config.ObjectMapperConfig;
import io.restassured.config.RestAssuredConfig;
import org.hisp.dhis.utils.CacheUtils;

/**
 * @author Gintare Vilkelyte <vilkelyte.gintare@gmail.com>
 */
public class Main
{
    private static LocustConfig cfg = ConfigFactory.create( LocustConfig.class );

    public static void main( String[] args )
            throws IOException {
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
        // Configure RestAssured mapper to convert any date into DHIS2 format

        RestAssured.config = RestAssuredConfig.config()
            .objectMapperConfig( new ObjectMapperConfig().jackson2ObjectMapperFactory( ( type, s ) -> {
                ObjectMapper om = new ObjectMapper().findAndRegisterModules();
                DateFormat df = new SimpleDateFormat( "yyyy-MM-dd" );
                om.setDateFormat( df );
                return om;
            } ) );

        RestAssured.baseURI = cfg.targetUri();
        EntitiesCache cache;
        new LoginTask().execute();

        if ( !CacheUtils.cacheExists() )
        {
            cache = new EntitiesCache();
            cache.loadTeiTypeCache();
            cache.loadProgramCache();
            CacheUtils.serializeCache( cache );
        }
        else
        {
            cache = CacheUtils.deserializeCache();
        }
        System.out.println( "cache loaded" );

        LocustSlave locustSlave = LocustSlave.newInstance();

        Locust locust = locustSlave.init();
    
        locust.run( new AddTeiTask( 50, cache ) );



    }

}

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
import org.hisp.dhis.tasks.*;

import java.io.IOException;

import static io.restassured.config.RestAssuredConfig.config;
import static org.aeonbits.owner.ConfigFactory.create;
import static org.hisp.dhis.utils.CacheUtils.*;

/**
 * @author Gintare Vilkelyte <vilkelyte.gintare@gmail.com>
 */
public class Main
{
    private static LocustConfig cfg = create( LocustConfig.class );

    public static void main( String[] args )
        throws IOException
    {

        // Configure RestAssured mapper to convert any date into DHIS2 format

      /* RestAssured.config = new RestAssuredConfig(  )
            .objectMapperConfig( new ObjectMapperConfig().jackson2ObjectMapperFactory( ( type, s ) -> {
                ObjectMapper om = new ObjectMapper().findAndRegisterModules();

                DateFormat df = new SimpleDateFormat( "yyyy-MM-dd" );
                om.setDateFormat( df );
                return om;
            } ) );
*/
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
            new QueryFilterTeiTask( 3 ),
            new GetHeavyAnalyticsRandomTask( 1, cfg.analyticsApiVersion(), cache ),
            new GetHeavyAnalyticsTask( 1, cfg.analyticsApiVersion() ),
            new AddTeiTask( 5, cache ),
            new FilterTeiTask( 5 ),
            new CreateTrackedEntityAttributeTask( 5 ),
            new MetadataExportImportTask( 1 ),
            new ReserveTrackedEntityAttributeValuesTask( 1 )
            //new QueryFilterTeiTask( 4 )
            //new AddTeiTask( 1, cache )
        );
    }
}

package org.hisp.dhis;

import com.github.myzhan.locust4j.Locust;
import com.google.gson.GsonBuilder;
import io.restassured.RestAssured;
import io.restassured.config.DecoderConfig;
import io.restassured.config.EncoderConfig;
import io.restassured.config.ObjectMapperConfig;
import io.restassured.mapper.ObjectMapperType;
import org.apache.commons.lang3.tuple.Pair;
import org.hisp.dhis.cache.CategoryOptionCombo;
import org.hisp.dhis.cache.EntitiesCache;
import org.hisp.dhis.cache.Program;
import org.hisp.dhis.common.CodeGenerator;
import org.hisp.dhis.locust.LocustConfig;
import org.hisp.dhis.locust.LocustSlave;
import org.hisp.dhis.tasks.LoginTask;
import org.hisp.dhis.tasks.tracker.EventImporSyncTask;
import org.hisp.dhis.tasks.tracker.MetadataImportTask;
import org.hisp.dhis.tasks.tracker.SetupEventImportTask;

import java.io.IOException;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static io.restassured.config.RestAssuredConfig.config;
import static org.aeonbits.owner.ConfigFactory.create;
import static org.hisp.dhis.utils.CacheUtils.cacheExists;
import static org.hisp.dhis.utils.CacheUtils.deserializeCache;
import static org.hisp.dhis.utils.CacheUtils.getCachePath;
import static org.hisp.dhis.utils.CacheUtils.serializeCache;

/**
 * @author Gintare Vilkelyte <vilkelyte.gintare@gmail.com>
 */
public class Main
{
    private static LocustConfig cfg = create( LocustConfig.class );

    public static void main( String[] args )
        throws IOException
    {
        try
        {
            run();
        }
        catch ( Exception e )
        {
            e.printStackTrace();
            System.out.println( "error:  " + e.getMessage() );
        }
    }

    public static void run()
        throws Exception
    {

        RestAssured.config = config()
            .decoderConfig( new DecoderConfig( "UTF-8" ) )
            .encoderConfig( new EncoderConfig( "UTF-8", "UTF-8" ) )

            .objectMapperConfig( new ObjectMapperConfig()
                .defaultObjectMapperType( ObjectMapperType.GSON )
                .gsonObjectMapperFactory( ( type, s ) -> new GsonBuilder()
                    .addDeserializationExclusionStrategy( new SuperclassExclusionStrategy() )
                    .addSerializationExclusionStrategy( new SuperclassExclusionStrategy() )
                    .setDateFormat( "yyyy-MM-dd" )
                    .create() )
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

        //new MetadataImportTask( "metadata_8879.json" ).execute();

        Program program = cache.getValidProgramFromCache();
        CategoryOptionCombo defaultCategoryCombo = cache.loadDefaultCategoryOptionCombo();

        Map<String, String> idMap = makeRandomIdMap( 5 );

        new SetupEventImportTask( idMap, program, cache ).execute();

        //new EventImporSyncTask( idMap, program, defaultCategoryCombo, cache ).execute();

        LocustSlave locustSlave = LocustSlave.newInstance();
        Locust locust = locustSlave.init();

        locust.run(
            new EventImporSyncTask( idMap, program,defaultCategoryCombo, cache )
        );
    }

    private static Map<String, String> makeRandomIdMap( int size )
    {
        return IntStream.rangeClosed( 1, size )
            .mapToObj( value -> Pair.of( CodeGenerator.generateUid(), CodeGenerator.generateUid() ) ).
                collect( Collectors.toMap( Pair::getLeft, Pair::getRight ) );
    }
}

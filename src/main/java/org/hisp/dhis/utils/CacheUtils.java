package org.hisp.dhis.utils;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.serializers.CollectionSerializer;
import de.javakaffee.kryoserializers.UnmodifiableCollectionsSerializer;
import org.hisp.dhis.cache.*;
import org.hisp.dhis.common.ValueType;
import org.hisp.dhis.conf.TestConfig;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.logging.Logger;

public class CacheUtils
{
    private static Logger logger = Logger.getLogger( CacheUtils.class.getName() );

    private static String tmp = "/tmp/cache";

    private static String cacheFile = tmp  + "/locust-cache.dat";

    private static Kryo kryo;

    static
    {
        kryo = new Kryo();

        CollectionSerializer serializer = new CollectionSerializer();
        UnmodifiableCollectionsSerializer.registerSerializers( kryo );

        kryo.register( LinkedList.class, serializer );
        kryo.register( ArrayList.class, serializer );
        kryo.register( HashMap.class );
        kryo.register( OrganisationUnit.class, 199 );
        kryo.register( ValueType.class, 200 );
        kryo.register( EntitiesCache.class, 201 );
        kryo.register( DataElement.class, 202 );
        kryo.register( Program.class, 203 );
        kryo.register( ProgramStage.class, 204 );
        kryo.register( TeiType.class, 205 );
        kryo.register( DataSet.class, 206 );
        kryo.register( UserCredentials.class, 207 );
        kryo.register( User.class, 208 );
        kryo.register( TrackedEntityAttribute.class, 209 );
        kryo.register( Tei.class, 210 );
        kryo.register( Visualization.class, 211 );
        kryo.register( DashboardItem.class, 212 );
        kryo.register( Dashboard.class, 213 );
        kryo.register( RelationshipConstraint.class, 214 );
        kryo.register( RelationshipType.class, 215 );
        kryo.register( ProgramRuleAction.class, 216 );

    }

    private CacheUtils()
    {
    }

    private static void serializeCache( EntitiesCache cache )
        throws IOException
    {
        File file = new File( cacheFile );
        if ( !file.exists()) {
            file.getParentFile().mkdirs();
        }

        Output output = new Output( new FileOutputStream(file, false) );
        kryo.writeObject(output, cache);
        output.close();
    }

    private static EntitiesCache deserializeCache()
        throws FileNotFoundException
    {
        try (Input input = new Input( new FileInputStream( cacheFile ) ))
        {
            return kryo.readObject( input, EntitiesCache.class );
        }

    }

    public static EntitiesCache initCache( TestConfig cfg )
        throws IOException
    {
        EntitiesCache cache;

        if ( !cacheExists() || !cfg.reuseCache() )
        {
            logger.info( "Cache not found. Hold on while a new cache is created." );
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
                logger.warning( "Error deserializing cache. Recreating cache file..." + e );
                cache = createAndSerializeCache();
            }
        }

        logger.info( "Cache loaded from " + getCachePath() );

        return cache;
    }

    private static EntitiesCache createAndSerializeCache()
        throws IOException
    {
        EntitiesCache cache = EntitiesCache.getInstance();
        serializeCache( cache );
        return cache;
    }

    private static boolean cacheExists()
    {
        return new File( cacheFile ).exists();
    }

    private static String getCachePath()
    {
        return cacheFile;
    }
}

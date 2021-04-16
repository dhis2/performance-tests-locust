package org.hisp.dhis.utils;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.serializers.CollectionSerializer;
import de.javakaffee.kryoserializers.UnmodifiableCollectionsSerializer;
import org.hisp.dhis.TestConfig;
import org.hisp.dhis.cache.*;
import org.hisp.dhis.common.ValueType;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.logging.Logger;

public class CacheUtils
{
    private CacheUtils() { }
    private static Logger logger = Logger.getLogger( CacheUtils.class.getName() );

    private static String tmp = System.getProperty( "java.io.tmpdir" );

    private static String cacheFile = tmp + System.getProperty( "file.separator" ) + "locust-cache.dat";

    private static Kryo kryo;

    static
    {
        kryo = new Kryo();

        CollectionSerializer serializer = new CollectionSerializer();
        UnmodifiableCollectionsSerializer.registerSerializers( kryo );

        kryo.register( LinkedList.class, serializer );
        kryo.register( ArrayList.class, serializer );
        kryo.register( HashMap.class );
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

    }

    private static void serializeCache( EntitiesCache cache )
        throws IOException
    {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Output output = new Output( baos );
        kryo.writeObject( output, cache );
        output.flush();
        try (OutputStream outputStream = new FileOutputStream( cacheFile ))
        {
            baos.writeTo( outputStream );
        }
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

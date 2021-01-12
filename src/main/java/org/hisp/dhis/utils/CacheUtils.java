package org.hisp.dhis.utils;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.serializers.CollectionSerializer;
import de.javakaffee.kryoserializers.UnmodifiableCollectionsSerializer;
import org.hisp.dhis.cache.*;
import org.hisp.dhis.common.ValueType;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

public class CacheUtils
{
    private static String TMP = System.getProperty( "java.io.tmpdir" );

    public static String CACHE_FILE = TMP + System.getProperty( "file.separator" ) + "locust-cache.dat";

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
        kryo.register( ProgramAttribute.class, 209 );
        kryo.register( Tei.class, 210 );

    }

    public static void serializeCache( EntitiesCache cache )
        throws IOException
    {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Output output = new Output( baos );
        kryo.writeObject( output, cache );
        output.flush();
        try (OutputStream outputStream = new FileOutputStream( CACHE_FILE ))
        {
            baos.writeTo( outputStream );
        }
    }

    public static EntitiesCache deserializeCache()
        throws FileNotFoundException
    {
        try (Input input = new Input( new FileInputStream( CACHE_FILE ) ))
        {
            return kryo.readObject( input, EntitiesCache.class );
        }

    }

    public static EntitiesCache createAndSerializeCache()
            throws IOException
    {
        EntitiesCache cache = new EntitiesCache();
        cache.loadAll();
        serializeCache( cache );
        return cache;
    }

    public static boolean cacheExists()
    {
        return new File( CACHE_FILE ).exists();
    }

    public static String getCachePath()
    {
        return CACHE_FILE;
    }
}

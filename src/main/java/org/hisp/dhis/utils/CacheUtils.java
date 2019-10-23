package org.hisp.dhis.utils;

import java.io.*;
import java.util.ArrayList;

import org.hisp.dhis.cache.*;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.serializers.CollectionSerializer;

public class CacheUtils
{
    private static String TMP = System.getProperty( "java.io.tmpdir" );

    private static String CACHE_FILE = TMP + System.getProperty( "file.separator" ) + "locust-cache.dat";

    private static Kryo kryo;

    static
    {
        kryo = new Kryo();
        CollectionSerializer serializer = new CollectionSerializer();
        kryo.register( ArrayList.class, serializer );

        kryo.register( EntitiesCache.class, 201 );
        kryo.register( DataElement.class, 202 );
        kryo.register( Program.class, 203 );
        kryo.register( ProgramStage.class, 204 );
        kryo.register( TeiType.class, 205 );
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

    public static boolean cacheExists()
    {
        return new File( CACHE_FILE ).exists();
    }

    public static String getCachePath( ) {

        return CACHE_FILE;
    }
}

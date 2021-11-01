package org.hisp.dhis.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import java.util.ArrayList;

/**
 * @author Gintare Vilkelyte <vilkelyte.gintare@gmail.com>
 */
public class JsonParserUtils
{
    private static JsonParser parser = new JsonParser();

    public static JsonElement toJsonObject( Object object )
    {
        if ( object instanceof String )
        {
            return parser.parseString( (String) object ).getAsJsonObject();
        }

        Gson gson = new GsonBuilder().setDateFormat( "yyyy-MM-dd" ).create();

        if ( object instanceof ArrayList )
        {
            return parser.parseString( gson.toJson( object ) ).getAsJsonArray();

        }

        return parser.parseString( gson.toJson( object ) ).getAsJsonObject();
    }
}

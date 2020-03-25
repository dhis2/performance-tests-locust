package org.hisp.dhis.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * @author Gintare Vilkelyte <vilkelyte.gintare@gmail.com>
 */
public class JsonParserUtils
{
    private static JsonParser parser = new JsonParser();

    public static JsonObject toJsonObject( Object object )
    {
        if ( object instanceof String )
        {
            return parser.parse( (String) object ).getAsJsonObject();
        }

        Gson gson = new GsonBuilder().setDateFormat( "yyyy-MM-dd"  ).create();
        JsonObject jsonObject = parser.parse( gson.toJson( object ) ).getAsJsonObject();

        return jsonObject;
    }
}

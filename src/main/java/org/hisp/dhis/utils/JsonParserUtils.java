package org.hisp.dhis.utils;

import com.google.gson.*;

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
            return parser.parse( (String) object ).getAsJsonObject();
        }

        Gson gson = new GsonBuilder().setDateFormat( "yyyy-MM-dd" ).create();

        if ( object instanceof ArrayList )
        {
            return parser.parse( gson.toJson( object ) ).getAsJsonArray();

        }

        JsonObject jsonObject = parser.parse( gson.toJson( object ) ).getAsJsonObject();

        return jsonObject;
    }
}

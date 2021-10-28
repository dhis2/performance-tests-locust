package org.hisp.dhis.actions;

import io.restassured.specification.RequestSpecification;
import org.apache.logging.log4j.core.util.KeyValuePair;
import org.hisp.dhis.cache.UserCredentials;
import org.hisp.dhis.response.dto.ApiResponse;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

/**
 * @author Gintare Vilkelyte <vilkelyte.gintare@gmail.com>
 */
public class ApiActions  extends RestApiActions
{
    public ApiActions( String endpoint )
    {
        super( endpoint );
    }

    public ApiResponse postFormData( String url, String param, String data )
        throws URISyntaxException
    {
        return new ApiResponse( this.given().formParam( param, data ).when().post( new URI( url ) ) );
    }


    public ApiResponse postFormData( String url, List<KeyValuePair> params )
        throws URISyntaxException
    {
        RequestSpecification spec = this.given();
        for ( KeyValuePair param : params )
        {
            spec.formParam( param.getKey(), param.getValue() );

        }
        return new ApiResponse( spec.when().post( new URI( url ) ) );
    }
}

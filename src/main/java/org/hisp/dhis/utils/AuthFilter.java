package org.hisp.dhis.utils;

import io.restassured.authentication.BasicAuthScheme;
import io.restassured.authentication.PreemptiveBasicAuthScheme;
import io.restassured.filter.Filter;
import io.restassured.filter.FilterContext;
import io.restassured.response.Response;
import io.restassured.specification.FilterableRequestSpecification;
import io.restassured.specification.FilterableResponseSpecification;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Gintare Vilkelyte <vilkelyte.gintare@gmail.com>
 */
public class AuthFilter
    implements Filter
{
    ConcurrentHashMap<String, Cookie> auth = new ConcurrentHashMap<>();

    @Override
    public Response filter( FilterableRequestSpecification requestSpec, FilterableResponseSpecification responseSpec,
        FilterContext ctx )
    {
        String username = "";
        String password = "";
        requestSpec.removeCookies();

        if ( requestSpec.getAuthenticationScheme() instanceof BasicAuthScheme ||
            requestSpec.getAuthenticationScheme() instanceof PreemptiveBasicAuthScheme )
        {
            if ( requestSpec.getAuthenticationScheme() instanceof BasicAuthScheme )
            {
                username = ((BasicAuthScheme) requestSpec.getAuthenticationScheme()).getUserName();
                password = ((BasicAuthScheme) requestSpec.getAuthenticationScheme()).getPassword();
            }
            else
            {
                username = ((PreemptiveBasicAuthScheme) requestSpec.getAuthenticationScheme()).getUserName();
                password = ((PreemptiveBasicAuthScheme) requestSpec.getAuthenticationScheme()).getPassword();
            }

            if ( auth.get( username ) != null )
            {
                String finalUsername = username;

                Cookie cookie = auth.get(
                    finalUsername
                );

                requestSpec.cookie( cookie.getType(), cookie.getValue() );
            }

            else
            {
                requestSpec.auth().preemptive().basic( username, password );
            }
        }

        final Response response = ctx.next( requestSpec, responseSpec );

        if ( auth.get( username ) == null && getSessionCookie( response ) != null )
        {
            auth.put( username, getSessionCookie( response ) );
        }

        return response;
    }

    private Cookie getSessionCookie( Response response )
    {
        if ( response.getCookie( "JSESSIONID" ) != null )
        {
            return new Cookie( "JSESSIONID", response.getCookie( "JSESSIONID" ) );
        }
        else if ( response.getCookie( "SESSION" ) != null )
        {
            return new Cookie( "SESSION", response.getCookie( "SESSION" ) );
        }

        return null;
    }

    @AllArgsConstructor
    @Getter
    @Setter
    public class Cookie
    {
        private String type;

        private String value;
    }
}

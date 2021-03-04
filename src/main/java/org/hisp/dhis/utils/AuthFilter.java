package org.hisp.dhis.utils;

import io.restassured.authentication.BasicAuthScheme;
import io.restassured.authentication.PreemptiveBasicAuthScheme;
import io.restassured.filter.Filter;
import io.restassured.filter.FilterContext;
import io.restassured.response.Response;
import io.restassured.specification.FilterableRequestSpecification;
import io.restassured.specification.FilterableResponseSpecification;
import javafx.util.Pair;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Gintare Vilkelyte <vilkelyte.gintare@gmail.com>
 */
public class AuthFilter
    implements Filter
{
    List<Pair<String, Cookie>> auth = new ArrayList<>();

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

            if ( getPair( username ) != null )
            {
                String finalUsername = username;
                Cookie cookie = auth.stream().filter( p -> p.getKey().equalsIgnoreCase( finalUsername ) )
                    .findFirst().orElse( null ).getValue();

                requestSpec.cookie( cookie.getType(), cookie.getValue() );

            }

            else
            {
                requestSpec.auth().preemptive().basic( username, password );
            }
        }

        final Response response = ctx.next( requestSpec, responseSpec );

        if ( getPair( username ) == null && getSessionCookie( response ) != null )
        {
            auth.add( new Pair<>( username,
                getSessionCookie( response ) ) );
        }
        return response;
    }

    private Cookie getSessionCookie( Response response )
    {
        if ( response.getCookie( "JSESSIONID" ) != null )
        {
            return new Cookie( "JSESSIONID", response.getCookie( "JSESSIONID" ) );
        }
        else
        {
            return new Cookie( "SESSION", response.getCookie( "SESSION" ) );
        }
    }

    private Pair<String, Cookie> getPair( String username )
    {
        return auth.stream().filter( p -> p.getKey().equalsIgnoreCase( username ) ).findFirst().orElse(
            null
        );
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

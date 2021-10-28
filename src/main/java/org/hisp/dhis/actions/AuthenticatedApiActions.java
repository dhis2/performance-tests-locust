package org.hisp.dhis.actions;

import io.restassured.specification.RequestSpecification;
import org.hisp.dhis.cache.UserCredentials;
import org.hisp.dhis.response.dto.ApiResponse;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * @author Gintare Vilkelyte <vilkelyte.gintare@gmail.com>
 */
public class AuthenticatedApiActions
    extends ApiActions
{
    private UserCredentials userCredentials;

    public AuthenticatedApiActions( String endpoint, UserCredentials userCredentials )
    {
        super( endpoint );
        this.userCredentials = userCredentials;
    }

    @Override
    protected RequestSpecification given()
    {
        return super.given().auth()
            .basic( userCredentials.getUsername(), userCredentials.getPassword() );
    }
}

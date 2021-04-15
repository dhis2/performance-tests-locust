package org.hisp.dhis.cache;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * @author Gintare Vilkelyte <vilkelyte.gintare@gmail.com>
 */
@Getter
@Setter
@AllArgsConstructor
public class UserCredentials
{
    private String username;

    private String password;

    public UserCredentials()
    {
    }

}

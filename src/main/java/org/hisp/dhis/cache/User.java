package org.hisp.dhis.cache;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * @author Gintare Vilkelyte <vilkelyte.gintare@gmail.com>
 */
@Getter
@Setter
@AllArgsConstructor
public class User
{
    private String id;

    private UserCredentials userCredentials;

    private List<String> organisationUnits;

    public User()
    {

    }

    public User( UserCredentials userCredentials )
    {
        this.userCredentials = userCredentials;
    }

    public String getUsername()
    {
        return userCredentials.getUsername();
    }

    public String getPassword()
    {
        return userCredentials.getPassword();
    }
}

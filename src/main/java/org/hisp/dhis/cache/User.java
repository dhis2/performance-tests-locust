package org.hisp.dhis.cache;

import lombok.*;

import java.util.List;
import java.util.Objects;

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

    public String getUsername()
    {
        return userCredentials.getUsername();
    }

    public String getPassword() {
        return userCredentials.getPassword();
    }

    public User() {

    }
}

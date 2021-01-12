package org.hisp.dhis.cache;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Objects;

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

    public UserCredentials() {
    }

    public String getPassword() {
        if (password == null) {
            return "Test1212?";
        }

        return password;
    }

}

package org.hisp.dhis.random;

import org.hisp.dhis.cache.EntitiesCache;
import org.hisp.dhis.cache.Program;
import org.hisp.dhis.cache.User;
import org.hisp.dhis.utils.Randomizer;

import java.util.stream.Collectors;

/**
 * @author Gintare Vilkelyte <vilkelyte.gintare@gmail.com>
 */
public class UserRandomizer
{
    private Randomizer rnd;

    public UserRandomizer( Randomizer rnd )
    {
        this.rnd = rnd;
    }

    public User getRandomUser(EntitiesCache entitiesCache )
    {
        return rnd.randomElementFromList( entitiesCache.getUsers() );
    }

    public User getRandomUserNotAdmin( EntitiesCache cache )
    {
        return rnd.randomElementFromList(
            cache.getUsers().stream().filter( p -> !p.getUserCredentials().equals( cache.getDefaultUser().getUserCredentials() ) )
                .collect(
                    Collectors.toList() ) );
    }

    public String getRandomUserOrgUnit( User user )
    {
        return rnd.randomElementFromList( user.getOrganisationUnits() );
    }

    /**
     * If user is assigned to root OU only, program ou will be used.
     *
     * @param user
     * @param program
     * @return
     */
    public String getRandomUserOrProgramOrgUnit( User user, Program program )
    {
        if ( user.getOrganisationUnits().contains( EntitiesCache.getInstance().getRootOu().getId() ) )
        {
            return rnd.randomElementFromList( program.getOrganisationUnits() );
        }


        return getRandomUserOrgUnit( user );
    }
}

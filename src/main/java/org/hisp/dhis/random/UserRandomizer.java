package org.hisp.dhis.random;

import org.hisp.dhis.cache.EntitiesCache;
import org.hisp.dhis.cache.User;
import org.hisp.dhis.utils.DataRandomizer;

/**
 * @author Gintare Vilkelyte <vilkelyte.gintare@gmail.com>
 */
public class UserRandomizer
{
    public User getRandomUser( EntitiesCache entitiesCache ) {
        return DataRandomizer.randomElementFromList( entitiesCache.getUsers());
    }

    public String getRandomUserOrgUnit( User user ) {
        return DataRandomizer.randomElementFromList( user.getOrganisationUnits() );
    }
}

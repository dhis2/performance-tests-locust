package org.hisp.dhis.cache.builder;

import org.hisp.dhis.actions.RestApiActions;
import org.hisp.dhis.cache.EntitiesCache;
import org.hisp.dhis.cache.User;
import org.hisp.dhis.locust.LocustConfig;
import org.hisp.dhis.response.dto.ApiResponse;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.aeonbits.owner.ConfigFactory.create;

/**
 * @author Gintare Vilkelyte <vilkelyte.gintare@gmail.com>
 */
public class UserCacheBuilder
    implements CacheBuilder<User>
{
    private static final LocustConfig cfg = create( LocustConfig.class );

    @Override
    public void load( EntitiesCache cache )
    {
        List<User> users = getPayload(
            "/api/users?filter=organisationUnits.level:eq:5&filter=displayName:like:uio&fields=id,organisationUnits~pluck,userCredentials[username]&pageSize=" +
                cfg.cacheUserPoolSize() )
            .extractList( "users", User.class );

        if ( users.isEmpty() )
        {
            System.out.println( "No dummy users, only default user will be added to the cache" );
        }

        User defaultUser = getPayload( String.format(
            "/api/users?filter=userCredentials.username:eq:%s&fields=id,organisationUnits~pluck,userCredentials[username]",
            cfg.adminUsername() ) ).extractList( "users", User.class ).get( 0 );
        defaultUser.getUserCredentials().setPassword( cfg.adminPassword() );

        if ( cfg.useDefaultUser() )
        {
            users = new ArrayList<>(users); // extractList returns unmodifiable collection
            users.add( defaultUser );
        }

        cache.setDefaultUser( defaultUser );

        System.out.println( "Users loaded in cache. Size: " + users.size() );
        cache.setUsers( users );
    }

    private ApiResponse getPayload( String url )
    {
        return new RestApiActions( "" ).get( url );
    }
}

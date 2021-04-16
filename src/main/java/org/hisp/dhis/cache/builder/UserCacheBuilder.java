package org.hisp.dhis.cache.builder;

import org.hisp.dhis.TestConfig;
import org.hisp.dhis.actions.RestApiActions;
import org.hisp.dhis.cache.EntitiesCache;
import org.hisp.dhis.cache.User;
import org.hisp.dhis.response.dto.ApiResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import static org.aeonbits.owner.ConfigFactory.create;

/**
 * @author Gintare Vilkelyte <vilkelyte.gintare@gmail.com>
 */
public class UserCacheBuilder
    implements CacheBuilder<User>
{
    private static final TestConfig cfg = create( TestConfig.class );

    private Logger logger = Logger.getLogger( this.getClass().getName() );

    @Override
    public void load( EntitiesCache cache )
    {
        cache.setDefaultUser( getDefaultUser() );

        List<User> users = get();

        cache.setUsers( users );
        logger.info( "Users loaded in cache. Size: " + users.size() );
    }

    @Override
    public List<User> get()
    {
        List<User> users = getPayload(
            String.format(
                "/api/users?filter=organisationUnits.level:eq:5&fields=id,organisationUnits~pluck,userCredentials[username]&filter=displayName:like:%s&pageSize=%d",
                cfg.cacheUsersIdentifier(),
                cfg.cacheUserPoolSize() ) )
            .extractList( "users", User.class );

        if ( users.isEmpty() )
        {
            logger.info( "No users matching the identifier, only the default user will be added to the cache" );
        }

        users.forEach( p -> {
            p.getUserCredentials().setPassword( cfg.cacheUsersPassword() );
        } );

        if ( cfg.useDefaultUser() || users.isEmpty() )
        {
            users = new ArrayList<>( users ); // extractList returns unmodifiable collection
            users.add( getDefaultUser() );
        }

        return users;
    }

    public User getDefaultUser()
    {
        User defaultUser = getPayload( String.format(
            "/api/users?filter=userCredentials.username:eq:%s&fields=id,organisationUnits~pluck,userCredentials[username]",
            cfg.adminUsername() ) ).extractList( "users", User.class ).get( 0 );
        defaultUser.getUserCredentials().setPassword( cfg.adminPassword() );

        return defaultUser;
    }

    private ApiResponse getPayload( String url )
    {
        return new RestApiActions( "" ).get( url );
    }
}

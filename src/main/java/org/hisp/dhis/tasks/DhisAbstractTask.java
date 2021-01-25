package org.hisp.dhis.tasks;

import com.github.myzhan.locust4j.AbstractTask;
import com.github.myzhan.locust4j.Locust;

import io.restassured.response.Response;
import org.aeonbits.owner.ConfigFactory;
import org.hisp.dhis.cache.EntitiesCache;
import org.hisp.dhis.cache.User;
import org.hisp.dhis.cache.UserCredentials;
import org.hisp.dhis.locust.LocustConfig;
import org.hisp.dhis.random.UserRandomizer;

/**
 * @author Gintare Vilkelyte <vilkelyte.gintare@gmail.com>
 */
public abstract class DhisAbstractTask
    extends AbstractTask
{
    protected int weight;

    protected UserCredentials userCredentials;

    protected User user;

    protected EntitiesCache entitiesCache;

    public int getWeight()
    {
        return this.weight;
    }

    public abstract String getName();

    public abstract String getType();

    public abstract void execute()
        throws Exception;

    protected UserCredentials getUserCredentials( ) {
        UserCredentials creds;

        if (this.user != null) {
            return user.getUserCredentials();
        }

        if (this.userCredentials == null) {
            if (this.entitiesCache == null)
            {
                LocustConfig conf = ConfigFactory.create( LocustConfig.class );
                return new UserCredentials(conf.adminUsername(), conf.adminPassword());
            }

            creds = new UserRandomizer().getRandomUser( this.entitiesCache ).getUserCredentials();

            return creds;
        }

        return this.userCredentials;
    }

    protected User getUser() {
        User user;
        if (this.userCredentials == null) {
            if (this.entitiesCache != null) {
                user = new UserRandomizer().getRandomUser( this.entitiesCache );
                return user;
            }

            LocustConfig conf = ConfigFactory.create( LocustConfig.class );
            return new User( new UserCredentials(conf.adminUsername(), conf.adminPassword()));

        }

        return this.entitiesCache.getUsers().stream().filter( p -> p.getUserCredentials().equals( this.userCredentials ) )
            .findFirst()
            .orElse( null);
    }

    public void recordSuccess( Response response )
    {
        Locust.getInstance().recordSuccess( getType(), getName(), response.getTime(),
            response.getBody().asByteArray().length );
    }

    public void recordSuccess( long time, long length )
    {
        Locust.getInstance().recordSuccess( getType(), getName(), time, length );
    }

    public void recordFailure( long time, String message )
    {
        Locust.getInstance().recordFailure( getType(), getName(), time, message );
    }

    public void recordFailure( Response response )
    {
        Locust.getInstance().recordFailure( getType(), getName(), response.getTime(), response.getBody().print() );
    }
}

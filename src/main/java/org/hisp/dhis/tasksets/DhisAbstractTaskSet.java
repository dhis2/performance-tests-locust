package org.hisp.dhis.tasksets;

import org.hisp.dhis.cache.User;
import org.hisp.dhis.cache.UserCredentials;
import org.hisp.dhis.conf.TestConfig;
import org.hisp.dhis.random.UserRandomizer;
import org.hisp.dhis.tasks.DhisAbstractTask;
import org.hisp.dhis.utils.PredictableRandomizer;
import org.hisp.dhis.utils.Randomizer;

import static org.aeonbits.owner.ConfigFactory.create;
import static org.hisp.dhis.conf.ConfigFactory.cfg;

public abstract class DhisAbstractTaskSet
    extends DhisAbstractTask
{
    protected User user;

    private static final TestConfig cfg = create( TestConfig.class );

    protected DhisAbstractTaskSet( String name, int weight )
    {
        super(weight, new PredictableRandomizer(name.hashCode() * cfg.locustRandomSeed() ));
    }

    protected UserCredentials getUserCredentials(Randomizer rnd)
    {
        if ( this.userCredentials != null )
        {
            return this.userCredentials;
        }

        return this.getUser(rnd).getUserCredentials();

    }

    protected User getUser(Randomizer rnd)
    {
        if ( this.user != null )
        {
            return this.user;
        }

        if ( this.userCredentials == null )
        {
            if ( this.entitiesCache != null )
            {
                user = new UserRandomizer(rnd).getRandomUser( this.entitiesCache );
                return user;
            }

            return new User( new UserCredentials( cfg.adminUsername(), cfg.adminPassword() ) );
        }

        return this.entitiesCache.getUsers().stream().filter( p -> p.getUserCredentials().equals( this.userCredentials ) )
                .findFirst()
                .orElse( null );
    }
}

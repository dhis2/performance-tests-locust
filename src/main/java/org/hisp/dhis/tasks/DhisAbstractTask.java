package org.hisp.dhis.tasks;

import com.github.myzhan.locust4j.AbstractTask;
import com.github.myzhan.locust4j.Locust;
import io.restassured.response.Response;
import org.hisp.dhis.cache.EntitiesCache;
import org.hisp.dhis.cache.Program;
import org.hisp.dhis.cache.User;
import org.hisp.dhis.cache.UserCredentials;
import org.hisp.dhis.random.UserRandomizer;
import org.hisp.dhis.response.dto.ApiResponse;
import org.hisp.dhis.utils.PredictableRandomizer;
import org.hisp.dhis.utils.Randomizer;

import java.util.concurrent.Callable;
import java.util.function.Function;
import java.util.logging.Logger;

import static org.hisp.dhis.conf.ConfigFactory.cfg;

/**
 * @author Gintare Vilkelyte <vilkelyte.gintare@gmail.com>
 */
public abstract class DhisAbstractTask
    extends AbstractTask
{
    protected int weight;

    protected UserCredentials userCredentials;

    protected User user;

    private Logger logger = Logger.getLogger( this.getClass().getName() );

    protected EntitiesCache entitiesCache;

    private final Randomizer rnd;

    protected DhisAbstractTask( int weight, Randomizer rnd )
    {
        this.weight = weight;
        this.entitiesCache = EntitiesCache.getInstance();
        this.rnd = rnd;
    }

    public int getWeight()
    {
        return this.weight;
    }

    public abstract String getName();

    public abstract String getType();

    public abstract void execute()
        throws Exception;

    protected void waitBetweenTasks()
        throws InterruptedException
    {
        if ( cfg.locustMinWaitBetweenTasks() != 0 && cfg.locustMaxWaitBetweenTasks() != 0 )
        {
            Thread.currentThread()
                .sleep( rnd.randomIntInRange( cfg.locustMinWaitBetweenTasks(), cfg.locustMaxWaitBetweenTasks() ) );
        }
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

    public void recordSuccess( Response response )
    {
        Locust.getInstance().recordSuccess( getType(), getName(), response.getTime(),
            response.getBody().asByteArray().length );
    }

    public void recordSuccess( long time, long length )
    {
        Locust.getInstance().recordSuccess( getType(), getName(), time, length );
    }

    protected ApiResponse performTaskAndRecord( Callable<ApiResponse> function, Function<ApiResponse, Boolean> expectation )
        throws Exception
    {
        final long time = System.currentTimeMillis();
        ApiResponse response = function.call();

        if ( expectation != null )
        {
            boolean passed = expectation.apply( response );

            if ( passed )
            {
                recordSuccess( System.currentTimeMillis() - time, response.getRaw().getBody().asByteArray().length );
                return response;
            }

            recordFailure( System.currentTimeMillis() - time, response.getRaw() );
        }

        return response;
    }

    protected ApiResponse performTaskAndRecord( Callable<ApiResponse> function, int expectedStatusCode )
        throws Exception
    {
        return performTaskAndRecord( function, response -> response.statusCode() == expectedStatusCode );
    }

    protected ApiResponse performTaskAndRecord( Callable<ApiResponse> function )
        throws Exception
    {
        return performTaskAndRecord( function, 200 );
    }

    public void record( Response response )
    {
        if ( response.statusCode() == 200 )
        {
            recordSuccess( response );
        }

        else
        {
            recordFailure( response );
        }
    }

    protected void record( Response response, long time )
    {
        record( response, time, 200 );
    }

    protected void record( Response response, long time, int expectedStatusCode )
    {
        if ( response.statusCode() == expectedStatusCode )
        {
            recordSuccess( time, response.getBody().asByteArray().length );
        }

        else
        {
            recordFailure( time, response );
        }
    }

    public void record( Response response, int statusCode )
    {
        if ( response.statusCode() == statusCode )
        {
            recordSuccess( response );
        }

        else
        {
            recordFailure( response );
        }
    }

    public void record( Response response, Function<Response, Response> function )
    {
        function.apply( response );
    }

    public void recordFailure( long time, String message )
    {
        Locust.getInstance().recordFailure( getType(), getName(), time, message );
    }

    public void recordFailure( long time, Response response )
    {
        Locust.getInstance().recordFailure( getType(), getName(), time, getError( response ) );
    }

    public void recordFailure( Response response )
    {
        Locust.getInstance().recordFailure( getType(), getName(), response.getTime(), getError( response ) );
    }

    /*
    Log full responses to stdout and only status codes in locust master if full error logging is disabled.
     */
    //
    private String getError( Response response )
    {
        if ( cfg.locustLogFullErrors() )
        {
            return response.body().asString();
        }
        response.print();
        return "Status code: " + response.statusCode();
    }

    protected void logWarningIfDebugEnabled( String message )
    {
        if ( cfg.debug() ) {
            logger.warning( message );
        }
    }

    protected User getRandomUser( Randomizer rnd ) {
        return new UserRandomizer(rnd).getRandomUser( entitiesCache );
    }

    public String getRandomUserOrgUnit( User user, Randomizer rnd )
    {
        return new UserRandomizer(rnd).getRandomUserOrgUnit( user );
    }

    public String getRandomUserOrProgramOrgUnit( User user, Program program, Randomizer rnd )
    {
        return new UserRandomizer(rnd).getRandomUserOrProgramOrgUnit( user, program );
    }

    protected Randomizer getNextRandomizer(String name) {
        long seed = name.hashCode() * (cfg.locustRandomSeed() + rnd.randomInt(1000));
        logger.info("[" +name+ "] - "+ rnd +" generated seed " + seed);
        return new PredictableRandomizer(seed);
    }
}

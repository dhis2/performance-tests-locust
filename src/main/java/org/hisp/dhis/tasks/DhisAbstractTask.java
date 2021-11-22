package org.hisp.dhis.tasks;

import com.github.myzhan.locust4j.AbstractTask;
import com.github.myzhan.locust4j.Locust;
import io.restassured.response.Response;
import org.aeonbits.owner.ConfigFactory;
import org.hisp.dhis.TestConfig;
import org.hisp.dhis.cache.EntitiesCache;
import org.hisp.dhis.cache.User;
import org.hisp.dhis.cache.UserCredentials;
import org.hisp.dhis.random.UserRandomizer;
import org.hisp.dhis.response.dto.ApiResponse;
import org.hisp.dhis.utils.DataRandomizer;

import java.util.concurrent.Callable;
import java.util.function.Function;

import static org.aeonbits.owner.ConfigFactory.create;

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

    protected TestConfig cfg = create( TestConfig.class );

    protected DhisAbstractTask( int weight )
    {
        this.weight = weight;
        this.entitiesCache = EntitiesCache.getInstance();
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
                .sleep( DataRandomizer.randomIntInRange( cfg.locustMinWaitBetweenTasks(), cfg.locustMaxWaitBetweenTasks() ) );
        }
    }

    protected UserCredentials getUserCredentials()
    {
        if ( this.userCredentials != null )
        {
            return this.userCredentials;
        }

        return this.getUser().getUserCredentials();

    }

    protected User getUser()
    {
        if ( this.user != null )
        {
            return this.user;
        }

        if ( this.userCredentials == null )
        {
            if ( this.entitiesCache != null )
            {
                user = new UserRandomizer().getRandomUser( this.entitiesCache );
                return user;
            }

            TestConfig conf = ConfigFactory.create( TestConfig.class );
            return new User( new UserCredentials( conf.adminUsername(), conf.adminPassword() ) );
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
        Locust.getInstance().recordFailure( getType(), getName(), time, mask( response ) );
    }

    public void recordFailure( Response response )
    {
        Locust.getInstance().recordFailure( getType(), getName(), response.getTime(), mask( response ) );
    }

    // locust4j stores errors in a hashmap and most of our responses contains uid or another unique attribute,
    // so logging full response ends up being memory issue when there are many errors.
    // Eventually, we should strip away the unique elements of response, but for now logging errors to stdout.
    private String mask( Response response )
    {
        response.print();
        return "Status code: " + response.statusCode();
    }
}

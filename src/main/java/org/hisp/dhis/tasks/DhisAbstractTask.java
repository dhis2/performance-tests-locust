package org.hisp.dhis.tasks;

import com.github.myzhan.locust4j.AbstractTask;
import com.github.myzhan.locust4j.Locust;

import io.restassured.response.Response;

/**
 * @author Gintare Vilkelyte <vilkelyte.gintare@gmail.com>
 */
public abstract class DhisAbstractTask
    extends AbstractTask
{
    protected int weight;

    public int getWeight()
    {
        return this.weight;
    }

    public abstract String getName();

    public abstract void execute()
        throws Exception;

    public void recordSuccess( Response response )
    {
        Locust.getInstance().recordSuccess( "http", getName(), response.getTime(),
            response.getBody().asByteArray().length );
    }

    public void recordSuccess( long time, long length )
    {
        Locust.getInstance().recordSuccess( "http", getName(), time, length );
    }

    public void recordFailure( long time, String message )
    {
        Locust.getInstance().recordFailure( "http", getName(), time, message );
    }

    public void recordFailure( Response response )
    {
        Locust.getInstance().recordFailure( "http", getName(), response.getTime(), response.getBody().print() );
    }
}

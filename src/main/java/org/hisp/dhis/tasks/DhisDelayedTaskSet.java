package org.hisp.dhis.tasks;

import com.github.myzhan.locust4j.AbstractTask;
import com.github.myzhan.locust4j.taskset.AbstractTaskSet;

import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import java.util.stream.IntStream;

/**
 * @author Gintare Vilkelyte <vilkelyte.gintare@gmail.com>
 */
public class DhisDelayedTaskSet
    extends AbstractTaskSet
{
    private int delay;

    private TimeUnit timeUnit;

    private Logger logger = Logger.getLogger( this.getClass().getName() );

    public DhisDelayedTaskSet( int delay, TimeUnit timeUnit )
    {
        this.delay = delay;
        this.timeUnit = timeUnit;
    }

    @Override
    public void addTask( AbstractTask task )
    {
        this.tasks.add( task );
    }

    @Override
    public int getWeight()
    {
        return 1;
    }

    @Override
    public String getName()
    {
        return null;
    }

    /* jdk 8 bug - very high cpu consumption for scheduled tasks
    /*@Override
    public void execute()
    {
        ScheduledExecutorService threadpool = Executors.newScheduledThreadPool(tasks.size());

        IntStream.range( 0, tasks.size() )
            .parallel()
            .forEach( i -> {
                ScheduledFuture<?> futureTask = threadpool.schedule(() ->
                    {
                        try
                        {
                            tasks.get( i ).execute();
                        }
                        catch ( Exception e )
                        {
                            logger.warning("Failed executing task");
                            e.printStackTrace();
                        }
                    },
                    this.delay * i,  this.timeUnit
                );

                while ( !futureTask.isDone() ) {
                }
            } );

        threadpool.shutdown();
    }

     */

    @Override
    public void execute()
    {
        IntStream.range( 0, tasks.size() )
            .parallel()
            .forEach( i -> {
                try
                {
                    Thread.currentThread().sleep( this.delay * 1000 );
                    tasks.get( i ).execute();
                }
                catch ( InterruptedException e )
                {
                }
                catch ( Exception e )
                {
                    logger.warning( "Failed executing task" );
                    e.printStackTrace();
                }
            } );

    }
}

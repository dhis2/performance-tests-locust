package org.hisp.dhis.tasks;

import com.github.myzhan.locust4j.AbstractTask;
import com.github.myzhan.locust4j.taskset.AbstractTaskSet;

import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

/**
 * @author Gintare Vilkelyte <vilkelyte.gintare@gmail.com>
 */
public class DhisDelayedTaskSet
    extends AbstractTaskSet
{
    private int delay;

    private TimeUnit timeUnit;

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
                            System.out.println("Failed executing task");
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
                    Thread.sleep( this.delay * i * 1000 );
                }
                catch ( InterruptedException e ) { }
                try
                {
                    tasks.get( i ).execute();
                }
                catch ( Exception e )
                {
                    System.out.println( "Failed executing task" );
                    e.printStackTrace();
                }

            } );

    }
}

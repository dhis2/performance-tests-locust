package org.hisp.dhis.tasks;

import com.github.myzhan.locust4j.AbstractTask;
import com.github.myzhan.locust4j.taskset.AbstractTaskSet;
import org.hisp.dhis.dxf2.events.event.DataValue;
import org.hisp.dhis.tasks.tracker.events.AddDataValueTask;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

/**
 * @author Gintare Vilkelyte <vilkelyte.gintare@gmail.com>
 */
public class DhisDelayedTaskSet extends AbstractTaskSet
{
    private int delay;
    private TimeUnit timeUnit;
    public DhisDelayedTaskSet( int delay, TimeUnit timeUnit) {
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

    @Override
    public void execute()
    {
        ScheduledExecutorService threadpool = Executors.newScheduledThreadPool(tasks.size());

        IntStream.range( 0, tasks.size() )
            .parallel()
            .forEach( i -> {
                System.out.println( i );

                ScheduledFuture<?> futureTask = threadpool.schedule(() ->
                    {
                        try
                        {
                            tasks.get( i ).execute();
                        }
                        catch ( Exception e )
                        {
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
}

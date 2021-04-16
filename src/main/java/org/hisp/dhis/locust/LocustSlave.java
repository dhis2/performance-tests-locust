package org.hisp.dhis.locust;

import com.github.myzhan.locust4j.AbstractTask;
import com.github.myzhan.locust4j.Locust;
import org.aeonbits.owner.ConfigFactory;
import org.hisp.dhis.TestConfig;
import org.hisp.dhis.tasks.DhisAbstractTask;
import org.hisp.dhis.tests.Category;
import org.hisp.dhis.tests.CategoryType;
import org.hisp.dhis.tests.PerformanceTest;
import org.reflections.Reflections;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 * The Locust slave connects to a Locust Master instance that runs the
 * performance tests
 *
 * @author Gintare Vilkelyte <vilkelyte.gintare@gmail.com>
 */
public class LocustSlave
{
    private TestConfig cfg = ConfigFactory.create( TestConfig.class );

    public static LocustSlave newInstance()
    {
        return new LocustSlave();
    }

    public Locust init()
    {
        Locust locust = Locust.getInstance();

        locust.setMasterPort( cfg.locustMasterPort() );
        locust.setMasterHost( cfg.locustMasterHost() );

        return locust;
    }

    public void runTasks( AbstractTask... tasks )
    {
        com.github.myzhan.locust4j.Locust locust = init();

        locust.run(  tasks );
    }

    public void runTasks( CategoryType... categories )
        throws InstantiationException, IllegalAccessException
    {
        List<AbstractTask> tasks = new ArrayList<>();
        Set<Class<? extends PerformanceTest>> classes = new Reflections( PerformanceTest.class )
            .getSubTypesOf( PerformanceTest.class );
        for ( Class<? extends PerformanceTest> aClass : classes )
        {
            Category category = aClass.getAnnotation( Category.class );

            if ( category != null && Arrays.asList( categories ).contains( category.category() ) )
            {
                tasks.addAll( aClass.newInstance().getTasks() );
            }
        }

        com.github.myzhan.locust4j.Locust locust = init();

        locust.run( tasks );
    }
}

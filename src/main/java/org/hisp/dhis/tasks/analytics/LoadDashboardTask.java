package org.hisp.dhis.tasks.analytics;

import org.hisp.dhis.cache.Dashboard;
import org.hisp.dhis.cache.User;
import org.hisp.dhis.cache.Visualization;
import org.hisp.dhis.tasks.DhisAbstractTask;
import org.hisp.dhis.utils.Randomizer;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @author Gintare Vilkelyte <vilkelyte.gintare@gmail.com>
 */
public class LoadDashboardTask
    extends DhisAbstractTask
{
    public LoadDashboardTask( int weight, Randomizer randomizer )
    {
        super( weight,randomizer );
    }

    @Override
    public String getName()
    {
        return "Load dashboard";
    }

    @Override
    public String getType()
    {
        return "http";
    }

    @Override
    public void execute()
        throws Exception
    {
        Randomizer rnd = getNextRandomizer( getName() );
        User user = getUser( rnd );

        Dashboard dashboard = rnd.randomElementFromList( entitiesCache.getDashboards() );

        // app loads items as user scrolls, so load 10 at once
        List<Integer> randomSequence = IntStream.range(0, dashboard.getDashboardItems().size())
                .map(number -> rnd.randomInt(10))
                .boxed()
                .collect( Collectors.toList() );

        randomSequence.parallelStream()
            .forEach( o -> {
                Visualization vi = dashboard.getDashboardItems().get( o ).getVisualization();
                if ( vi != null )
                {
                    new GetAnalyticsTask( 1, vi, user.getUserCredentials(), rnd ).execute();
                }
            } );
        waitBetweenTasks();
    }
}

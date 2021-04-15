package org.hisp.dhis.tasks.analytics;

import org.hisp.dhis.cache.Dashboard;
import org.hisp.dhis.cache.EntitiesCache;
import org.hisp.dhis.cache.Visualization;
import org.hisp.dhis.tasks.DhisAbstractTask;
import org.hisp.dhis.utils.DataRandomizer;

import java.util.List;

/**
 * @author Gintare Vilkelyte <vilkelyte.gintare@gmail.com>
 */
public class LoadDashboardTask
    extends DhisAbstractTask
{
    public LoadDashboardTask( int weight, EntitiesCache cache )
    {
        this.weight = weight;
        this.entitiesCache = cache;
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
        user = getUser();

        Dashboard dashboard = DataRandomizer.randomElementFromList( entitiesCache.getDashboards() );

        // app loads items as user scrolls, so load 10 at once
        List<Integer> randomSequence = DataRandomizer.randomSequence( dashboard.getDashboardItems().size(), 10 );

        randomSequence.parallelStream()
            .forEach( o -> {
                Visualization vi = dashboard.getDashboardItems().get( o ).getVisualization();
                if ( vi != null )
                {
                    new GetAnalyticsTask( 1, entitiesCache, vi, user.getUserCredentials() ).execute();
                }
            } );
        waitBetweenTasks();
    }
}

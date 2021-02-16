package org.hisp.dhis.tasks.analytics;

import org.hisp.dhis.cache.Dashboard;
import org.hisp.dhis.cache.EntitiesCache;
import org.hisp.dhis.tasks.DhisAbstractTask;
import org.hisp.dhis.utils.DataRandomizer;

/**
 * @author Gintare Vilkelyte <vilkelyte.gintare@gmail.com>
 */
public class LoadDashboardTask extends DhisAbstractTask
{
    public LoadDashboardTask( int weight, EntitiesCache cache) {
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

        dashboard.getDashboardItems().parallelStream()
            .forEach( di -> {
                if (di.getVisualization() != null) {
                    new GetAnalyticsTask( 1, entitiesCache, di.getVisualization(), user.getUserCredentials()).execute();
                }
            });
    }
}

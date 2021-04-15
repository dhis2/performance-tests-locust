package org.hisp.dhis.tasks.analytics;

import org.hisp.dhis.actions.AuthenticatedApiActions;
import org.hisp.dhis.cache.EntitiesCache;
import org.hisp.dhis.cache.UserCredentials;
import org.hisp.dhis.cache.Visualization;
import org.hisp.dhis.tasks.DhisAbstractTask;
import org.hisp.dhis.utils.DataRandomizer;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Gintare Vilkelyte <vilkelyte.gintare@gmail.com>
 */
public class GetAnalyticsTask
    extends DhisAbstractTask
{
    private String endpoint = "/api/analytics";

    private Visualization visualization;

    public GetAnalyticsTask( int weight, EntitiesCache cache )
    {
        this.weight = weight;
        this.entitiesCache = cache;
    }

    public GetAnalyticsTask( int weight, EntitiesCache cache, Visualization visualization, UserCredentials userCredentials )
    {
        this( weight, cache );
        this.visualization = visualization;
        this.userCredentials = userCredentials;
    }

    @Override
    public String getName()
    {
        return endpoint;
    }

    @Override
    public String getType()
    {
        return "GET";
    }

    @Override
    public void execute()
    {
        user = getUser();

        if ( visualization == null )
        {
            visualization = DataRandomizer.randomElementFromList( entitiesCache.getVisualizations() );
        }

        String query = getRandomAnalyticsQuery( visualization );

        AuthenticatedApiActions authenticatedApiActions = new AuthenticatedApiActions( endpoint, user.getUserCredentials() );

        record( authenticatedApiActions.get( query ).getRaw() );
    }

    private String getRandomAnalyticsQuery( Visualization visualization )
    {
        return String
            .format( "?%s&dimension=%s&dimension=%s", getFilterDimension( visualization ), getRowDimension( visualization ),
                getColumnDimension( visualization ) );
    }

    private String getColumnDimension( Visualization visualization )
    {
        if ( visualization.getColumnDimensions() != null )
        {
            return visualization.getColumnDimensions().stream().map( p -> getDimensionValue( p, visualization ) )
                .collect( Collectors.joining( "," ) );
        }

        return "";
    }

    private String getRowDimension( Visualization visualization )
    {
        if ( visualization.getRowDimensions() != null )
        {
            return visualization.getRowDimensions().stream().map( p -> getDimensionValue( p, visualization ) )
                .collect( Collectors.joining( "," ) );
        }

        return "";
    }

    private String getFilterDimension( Visualization visualization )
    {
        if ( visualization.getFilterDimensions() != null )
        {
            return visualization.getFilterDimensions().stream().map( p -> getDimensionValue( p, visualization ) )
                .collect( Collectors.joining( "&filter=", "filter=", "" ) );
        }

        return "";
    }

    private String getDimensionValue( String dimension, Visualization visualization )
    {
        if ( dimension.equalsIgnoreCase( "ou" ) )
        {
            return "ou:" + getOrgUnit();
        }

        if ( dimension.equalsIgnoreCase( "pe" ) )
        {
            return "pe:" + getPeriod( visualization.getPeriods() );
        }

        if ( dimension.equalsIgnoreCase( "dx" ) )
        {
            if ( visualization.getDataDimensionItems() == null )
            {
                return "";
            }
            return "dx:" + String.join( ";", visualization.getDataDimensionItems() );
        }

        return "";
    }

    private String getOrgUnit()
    {
        return DataRandomizer.randomElementFromList( Arrays.asList( "USER_ORGUNIT" ) );
    }

    private String getPeriod( List<String> periods )
    {
        if ( periods != null && !periods.isEmpty() )
        {
            return String.join( ";", periods );
        }

        List<String> relativePeriods = Arrays
            .asList( "THIS_YEAR", "LAST_YEAR", "LAST_MONTH", "LAST_12_MONTHS", "MONTHS_THIS_YEAR", "LAST_QUARTER" );

        return DataRandomizer.randomElementFromList( relativePeriods );
    }
}

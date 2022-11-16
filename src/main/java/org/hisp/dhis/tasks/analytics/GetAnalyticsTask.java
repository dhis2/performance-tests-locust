package org.hisp.dhis.tasks.analytics;

import org.hisp.dhis.actions.AuthenticatedApiActions;
import org.hisp.dhis.cache.User;
import org.hisp.dhis.cache.UserCredentials;
import org.hisp.dhis.cache.Visualization;
import org.hisp.dhis.tasks.DhisAbstractTask;
import org.hisp.dhis.utils.Randomizer;

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

    public GetAnalyticsTask( int weight, Visualization visualization, UserCredentials userCredentials, Randomizer randomizer )
    {
        super( weight, randomizer );
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
        Randomizer rnd = getNextRandomizer( getName() );

        String query = getRandomAnalyticsQuery( visualization, rnd );

        AuthenticatedApiActions authenticatedApiActions = new AuthenticatedApiActions( endpoint, this.userCredentials );

        record( authenticatedApiActions.get( query ).getRaw() );
    }

    private String getRandomAnalyticsQuery(Visualization visualization, Randomizer rnd )
    {
        return String
            .format( "?%s&dimension=%s&dimension=%s", getFilterDimension( visualization, rnd ), getRowDimension( visualization, rnd ),
                getColumnDimension( visualization, rnd ) );
    }

    private String getColumnDimension(Visualization visualization, Randomizer rnd)
    {
        if ( visualization.getColumnDimensions() != null )
        {
            return visualization.getColumnDimensions().stream().map( p -> getDimensionValue( p, visualization, rnd ) )
                .collect( Collectors.joining( "," ) );
        }

        return "";
    }

    private String getRowDimension( Visualization visualization, Randomizer rnd )
    {
        if ( visualization.getRowDimensions() != null )
        {
            return visualization.getRowDimensions().stream().map( p -> getDimensionValue( p, visualization, rnd ) )
                .collect( Collectors.joining( "," ) );
        }

        return "";
    }

    private String getFilterDimension(Visualization visualization, Randomizer rnd )
    {
        if ( visualization.getFilterDimensions() != null )
        {
            return visualization.getFilterDimensions().stream().map( p -> getDimensionValue( p, visualization, rnd ) )
                .collect( Collectors.joining( "&filter=", "filter=", "" ) );
        }

        return "";
    }

    private String getDimensionValue(String dimension, Visualization visualization, Randomizer rnd)
    {
        if ( dimension.equalsIgnoreCase( "ou" ) )
        {
            return "ou:" + getOrgUnit( rnd );
        }

        if ( dimension.equalsIgnoreCase( "pe" ) )
        {
            return "pe:" + getPeriod( visualization.getPeriods(), rnd);
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

    private String getOrgUnit( Randomizer rnd )
    {
        return rnd.randomElementFromList( Arrays.asList( "USER_ORGUNIT" ) );
    }

    private String getPeriod(List<String> periods, Randomizer rnd)
    {
        if ( periods != null && !periods.isEmpty() )
        {
            return String.join( ";", periods );
        }

        List<String> relativePeriods = Arrays
            .asList( "THIS_YEAR", "LAST_YEAR", "LAST_MONTH", "LAST_12_MONTHS", "MONTHS_THIS_YEAR", "LAST_QUARTER" );

        return rnd.randomElementFromList( relativePeriods );
    }
}

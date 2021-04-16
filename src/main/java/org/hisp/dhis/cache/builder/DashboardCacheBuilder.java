package org.hisp.dhis.cache.builder;

import com.jayway.jsonpath.JsonPath;
import org.hisp.dhis.actions.RestApiActions;
import org.hisp.dhis.cache.Dashboard;
import org.hisp.dhis.cache.DashboardItem;
import org.hisp.dhis.cache.EntitiesCache;
import org.hisp.dhis.cache.Visualization;
import org.hisp.dhis.response.dto.ApiResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import static java.util.stream.Collectors.toList;

/**
 * @author Gintare Vilkelyte <vilkelyte.gintare@gmail.com>
 */
public class DashboardCacheBuilder
    implements CacheBuilder<Dashboard>
{
    private Logger logger = Logger.getLogger( this.getClass().getName() );

    @Override
    public void load( EntitiesCache cache )
    {
        List<Dashboard> dashboards = get();

        cache.setDashboards( dashboards );
        logger.info( () -> "Dashboards loaded in cache. Size: " + cache.getDashboards().size() );

        cache.setVisualizations( getVisualisations( dashboards ) );

        logger.info( () -> "Visualizations loaded in cache. Size: " + cache.getVisualizations().size() );
    }

    @Override
    public List<Dashboard> get()
    {
        List<Dashboard> dashboards = getPayload(
            "/api/dashboards?fields=*,dashboardItems[id,visualization[*,periods~pluck,!dataDimensionItems]]" )
            .extractList( "dashboards", Dashboard.class );

        dashboards.forEach( dashboard -> {
            for ( DashboardItem p : dashboard.getDashboardItems() )
            {
                if ( p.getVisualization() != null )
                {
                    Visualization visualization = p.getVisualization();
                    ApiResponse response = getPayload( "/api/visualizations/" + visualization.getId() + "?fields=*" );
                    if ( response.statusCode() == 200 )
                    {
                        visualization.setDataDimensionItems(
                            (List<String>) JsonPath.parse( response.getBody().toString() )
                                .read( "dataDimensionItems.**.id", List.class ).stream().collect( toList() ) );
                        visualization.setColumnDimensions( response.extractObject( "columnDimensions", List.class ) );
                        visualization.setRowDimensions( response.extractObject( "rowDimensions", List.class ) );
                    }
                }
            }
        } );

        return dashboards;
    }

    public List<Visualization> getVisualisations( List<Dashboard> dashboards )
    {
        return dashboards
            .stream()
            .flatMap( a -> a.getDashboardItems().stream() )
            .filter( b -> b.getVisualization() != null )
            .map( DashboardItem::getVisualization )
            .collect( toList() );
    }

    private ApiResponse getPayload( String url )
    {
        ApiResponse response = new RestApiActions( "" ).get( url );
        response.validate().statusCode( 200 ) ;
        return response;
    }
}

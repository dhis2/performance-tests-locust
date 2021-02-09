package org.hisp.dhis.cache.builder;

import org.hisp.dhis.actions.RestApiActions;
import org.hisp.dhis.cache.EntitiesCache;
import org.hisp.dhis.cache.TeiType;
import org.hisp.dhis.response.dto.ApiResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Gintare Vilkelyte <vilkelyte.gintare@gmail.com>
 */
public class TeiTypeCacheBuilder
    implements CacheBuilder<TeiType>
{
    @Override
    public void load( EntitiesCache cache )
    {
        List<TeiType> teiTypes = new ArrayList<>();

        List<Map> payload = getPayload( "/api/trackedEntityTypes" ).extractList( "trackedEntityTypes" );

        for ( Map map : payload )
        {
            teiTypes.add( new TeiType( (String) map.get( "id" ), (String) map.get( "displayName" ) ) );
        }

        cache.setTeiTypes( teiTypes) ;
    }

    private ApiResponse getPayload( String url )
    {
        return new RestApiActions( "" ).get( url );
    }
}

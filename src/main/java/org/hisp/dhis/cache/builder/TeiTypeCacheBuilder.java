package org.hisp.dhis.cache.builder;

import org.hisp.dhis.actions.RestApiActions;
import org.hisp.dhis.cache.EntitiesCache;
import org.hisp.dhis.cache.TeiType;
import org.hisp.dhis.response.dto.ApiResponse;

import java.util.List;
import java.util.logging.Logger;

/**
 * @author Gintare Vilkelyte <vilkelyte.gintare@gmail.com>
 */
public class TeiTypeCacheBuilder
    implements CacheBuilder<TeiType>
{
    private Logger logger = Logger.getLogger( this.getClass().getName() );

    @Override
    public void load( EntitiesCache cache )
    {
        List<TeiType> teiTypes = get();

        logger.info( "Tei types loaded in cache. Size: " + teiTypes.size() );
        cache.setTeiTypes( teiTypes );
    }

    @Override
    public List<TeiType> get()
    {
        return getPayload( "/api/trackedEntityTypes?fields=*" )
            .extractList( "trackedEntityTypes", TeiType.class );
    }

    private ApiResponse getPayload( String url )
    {
        return new RestApiActions( "" ).get( url );
    }
}

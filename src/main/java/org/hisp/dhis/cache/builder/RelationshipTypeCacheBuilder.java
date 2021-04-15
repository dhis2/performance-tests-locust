package org.hisp.dhis.cache.builder;

import org.hisp.dhis.actions.RestApiActions;
import org.hisp.dhis.cache.EntitiesCache;
import org.hisp.dhis.cache.RelationshipType;
import org.hisp.dhis.response.dto.ApiResponse;

import java.util.List;
import java.util.logging.Logger;

/**
 * @author Gintare Vilkelyte <vilkelyte.gintare@gmail.com>
 */
public class RelationshipTypeCacheBuilder
    implements CacheBuilder<RelationshipType>
{
    private Logger logger = Logger.getLogger( this.getClass().getName() );

    @Override
    public void load( EntitiesCache cache )
    {
        ApiResponse response = getPayload( "/api/relationshipTypes?fields=*" );

        List<RelationshipType> relationshipTypes = response.extractList( "relationshipTypes", RelationshipType.class );

        cache.setRelationshipTypes( relationshipTypes );
        logger.info( "Relationship types loaded in cache. Size: " + cache.getRelationshipTypes().size() );
    }

    private ApiResponse getPayload( String url )
    {
        return new RestApiActions( "" ).get( url );
    }
}

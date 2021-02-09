package org.hisp.dhis.cache.builder;

import com.jayway.jsonpath.JsonPath;
import org.hisp.dhis.actions.RestApiActions;
import org.hisp.dhis.cache.EntitiesCache;
import org.hisp.dhis.cache.RelationshipType;
import org.hisp.dhis.response.dto.ApiResponse;

import java.util.ArrayList;
import java.util.List;

import static java.util.stream.Collectors.toList;

/**
 * @author Gintare Vilkelyte <vilkelyte.gintare@gmail.com>
 */
public class RelationshipTypeCacheBuilder implements CacheBuilder<RelationshipType>
{
    @Override
    public void load( EntitiesCache cache )
    {
        ApiResponse response = getPayload( "/api/relationshipTypes?fields=*" );

        List<RelationshipType> relationshipTypes = response.extractList( "relationshipTypes", RelationshipType.class );

        cache.setRelationshipTypes( relationshipTypes );
        System.out.println("Relationship types loaded. Size: " + cache.getRelationshipTypes().size());
    }

    private ApiResponse getPayload( String url ) {
        return new RestApiActions( "" ).get(url);
    }
}

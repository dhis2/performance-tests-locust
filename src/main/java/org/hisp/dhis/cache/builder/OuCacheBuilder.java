package org.hisp.dhis.cache.builder;

import org.hisp.dhis.actions.RestApiActions;
import org.hisp.dhis.cache.EntitiesCache;
import org.hisp.dhis.cache.OrganisationUnit;
import org.hisp.dhis.request.QueryParamsBuilder;
import org.hisp.dhis.response.dto.ApiResponse;

import java.util.List;

/**
 * @author Gintare Vilkelyte <vilkelyte.gintare@gmail.com>
 */
public class OuCacheBuilder
    implements CacheBuilder<OrganisationUnit>
{

    @Override
    public void load( EntitiesCache cache )
    {
        cache.setRootOu( get().get( 0 ) );
    }

    @Override
    /**
     * Gets org units. Only level 1 org unit is used right now.
     */
    public List<OrganisationUnit> get()
    {
        ApiResponse response = new RestApiActions( "/api/organisationUnits" ).get( "", new QueryParamsBuilder()
            .add( "filter=children:gt:1" ).add( "filter=level:eq:1" ).add( "fields=level,id" ) );

        List<OrganisationUnit> organisationUnits = response.extractList( "organisationUnits", OrganisationUnit.class );

        return organisationUnits;
    }

    public OrganisationUnit getRootOu()
    {
        return get().get( 0 );
    }
}

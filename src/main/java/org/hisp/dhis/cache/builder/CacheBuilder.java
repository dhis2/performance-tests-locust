package org.hisp.dhis.cache.builder;

import org.hisp.dhis.cache.EntitiesCache;

/**
 * @author Gintare Vilkelyte <vilkelyte.gintare@gmail.com>
 */
public interface CacheBuilder<T>
{
    void load( EntitiesCache cache );
}

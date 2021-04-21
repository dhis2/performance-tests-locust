package org.hisp.dhis.cache.builder;

import org.hisp.dhis.cache.EntitiesCache;

import java.util.List;

/**
 * @author Gintare Vilkelyte <vilkelyte.gintare@gmail.com>
 */
public interface CacheBuilder<T>
{
    void load( EntitiesCache cache );

    List<T> get();
}

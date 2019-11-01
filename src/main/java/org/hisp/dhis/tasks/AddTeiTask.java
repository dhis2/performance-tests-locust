package org.hisp.dhis.tasks;

import org.hisp.dhis.cache.EntitiesCache;
import org.hisp.dhis.dxf2.events.trackedentity.TrackedEntityInstances;
import org.hisp.dhis.random.TrackedEntityInstanceRandomizer;

public class AddTeiTask
    extends
    DhisAbstractTask
{
    private EntitiesCache cache;

    public AddTeiTask( int weight, EntitiesCache entitiesCache )
    {
        this.weight = weight;
        this.cache = entitiesCache;
    }

    public int getWeight()
    {
        return this.weight;
    }

    public String getName()
    {
        return "POST /api/trackedEntityInstances";
    }

    public void execute()
    {
        TrackedEntityInstances trackedEntityInstances = new TrackedEntityInstanceRandomizer().create( this.cache, 5 );

        executeQuery( () -> post( "/api/trackedEntityInstances", trackedEntityInstances ) );

    }
}

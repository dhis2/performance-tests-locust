package org.hisp.dhis.tasksets;

import org.hisp.dhis.tasks.DhisAbstractTask;
import org.hisp.dhis.utils.PredictableRandomizer;

import static org.hisp.dhis.Main.cfg;

public abstract class DhisAbstractTaskSet
    extends DhisAbstractTask
{
    protected DhisAbstractTaskSet( String name, int weight )
    {
        super(weight, new PredictableRandomizer(name.hashCode() * cfg.locustRandomSeed() ));
    }
}

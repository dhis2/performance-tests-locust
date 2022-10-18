package org.hisp.dhis.tasksets;

import org.hisp.dhis.conf.TestConfig;
import org.hisp.dhis.tasks.DhisAbstractTask;
import org.hisp.dhis.utils.PredictableRandomizer;

import static org.aeonbits.owner.ConfigFactory.create;

public abstract class DhisAbstractTaskSet
    extends DhisAbstractTask
{
    private static final TestConfig cfg = create( TestConfig.class );

    protected DhisAbstractTaskSet( String name, int weight )
    {
        super(weight, new PredictableRandomizer(name.hashCode() * cfg.locustRandomSeed() ));
    }
}

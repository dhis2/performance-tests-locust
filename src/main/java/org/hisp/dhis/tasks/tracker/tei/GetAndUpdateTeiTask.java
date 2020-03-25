package org.hisp.dhis.tasks.tracker.tei;

import org.hisp.dhis.tasks.DhisAbstractTask;

/**
 * @author Gintare Vilkelyte <vilkelyte.gintare@gmail.com>
 */
public class UpdateTeiTask
    extends DhisAbstractTask
{
    private static String program = "";
    @Override
    public String getName()
    {
        return "PUT /trackedEntityInstances";
    }

    @Override
    public void execute()
        throws Exception
    {

    }
}

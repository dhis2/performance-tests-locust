package org.hisp.dhis.tests;

import org.hisp.dhis.tasks.DhisAbstractTask;
import org.hisp.dhis.tasks.aggregate.AddDataValueTask;
import org.hisp.dhis.tasks.analytics.LoadDashboardTask;
import org.hisp.dhis.tasks.tracker.PostRelationshipTask;
import org.hisp.dhis.tasksets.aggregate.Android_syncDataValuesTaskSet;
import org.hisp.dhis.tasksets.tracker.*;

import java.util.Arrays;
import java.util.List;

/**
 * @author Gintare Vilkelyte <vilkelyte.gintare@gmail.com>
 */
@Category( category = CategoryType.ALL )
public class AllTests
    implements PerformanceTest
{
    @Override
    public List<DhisAbstractTask> getTasks()
    {
        return Arrays.asList(
            new Android_syncTeisTaskSet( 5 ),
            new AddDataValueTask( 1 ),
            new Android_syncDataValuesTaskSet( 1 ),
            new TrackerCapture_addTeiTaskSet( 2 ),
            new Capture_addEventTaskSet( 2 ),
            new TrackerCapture_searchForTeiTaskSet( 1 ),
            new PostRelationshipTask( 1 ),
            new TrackerCapture_searchForTeiByUniqueAttributeTaskSet( 1 )
        );
    }
}

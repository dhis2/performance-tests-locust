package org.hisp.dhis.tests;

import org.hisp.dhis.tasks.DhisAbstractTask;
import org.hisp.dhis.tasksets.aggregate.Android_syncDataValuesTaskSet;
import org.hisp.dhis.tasksets.tracker.Android_downloadLatestEventsTaskSet;
import org.hisp.dhis.tasksets.tracker.Android_downloadLatestTeisTaskSet;
import org.hisp.dhis.tasksets.tracker.Android_syncTeisTaskSet;

import java.util.Arrays;
import java.util.List;

/**
 * @author Gintare Vilkelyte <vilkelyte.gintare@gmail.com>
 */
@Category( category = CategoryType.ANDROID )
public class AndroidTests
    implements PerformanceTest
{
    @Override
    public List<DhisAbstractTask> getTasks()
    {
        return Arrays.asList(
            new Android_syncTeisTaskSet( 1, 10 ),
            new Android_syncDataValuesTaskSet( 1 ),
            new Android_downloadLatestTeisTaskSet( 1 ),
            new Android_downloadLatestEventsTaskSet( 1 ),
            new Android_syncTeisTaskSet( 1 )
        );
    }
}

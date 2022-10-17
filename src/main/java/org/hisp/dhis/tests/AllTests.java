package org.hisp.dhis.tests;

import org.hisp.dhis.tasks.DhisAbstractTask;
import org.hisp.dhis.tasksets.aggregate.TrackerCapture_addDataValueTask;
import org.hisp.dhis.tasksets.aggregate.Android_syncDataValuesTaskSet;
import org.hisp.dhis.tasksets.tracker.*;
import org.hisp.dhis.utils.PredictableRandomizer;

import java.util.Arrays;
import java.util.List;

import static org.hisp.dhis.Main.cfg;

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
            new TrackerCapture_addDataValueTask( 1 ),
            new Android_syncDataValuesTaskSet( 1 ),
            new TrackerCapture_addTeiTaskSet( 2 ),
            new Capture_addEventTaskSet( 2 ),
            new TrackerCapture_searchForTeiTaskSet( 1 ),
            new TrackerCapture_postRelationshipTask( 1 ),
            new TrackerCapture_searchForTeiByUniqueAttributeTaskSet( 1 ),
            new Android_downloadTeisTaskSet( 1 ),
            new Android_downloadLatestEventsTaskSet( 1 ),
            new Android_downloadLatestTeisTaskSet( 1 )
        );
    }
}

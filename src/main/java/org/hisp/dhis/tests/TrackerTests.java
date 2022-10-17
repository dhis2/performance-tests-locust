package org.hisp.dhis.tests;

import org.hisp.dhis.tasks.DhisAbstractTask;
import org.hisp.dhis.tasksets.tracker.*;
import org.hisp.dhis.utils.PredictableRandomizer;

import java.util.Arrays;
import java.util.List;

import static org.hisp.dhis.Main.cfg;

/**
 * @author Gintare Vilkelyte <vilkelyte.gintare@gmail.com>
 */
@Category( category = CategoryType.TRACKER )
public class TrackerTests
    implements PerformanceTest
{
    @Override
    public List<DhisAbstractTask> getTasks()
    {
        return Arrays.asList(
            new TrackerCapture_searchForTeiByUniqueAttributeTaskSet( 1 ),
            new TrackerCapture_addTeiTaskSet( 1 ),
            new TrackerCapture_searchForTeiTaskSet( 1 ),
            new Capture_addEventTaskSet( 1 ),
            new Android_syncTeisTaskSet( 1 ),
            new TrackerCapture_postRelationshipTask( 1 ) );
    }
}

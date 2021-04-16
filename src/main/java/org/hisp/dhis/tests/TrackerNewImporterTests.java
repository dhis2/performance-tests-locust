package org.hisp.dhis.tests;

import org.hisp.dhis.tasks.DhisAbstractTask;
import org.hisp.dhis.tasksets.tracker.importer.Android_importer_syncTeisTaskSet;
import org.hisp.dhis.tasksets.tracker.importer.Capture_importer_addEventTaskSet;
import org.hisp.dhis.tasksets.tracker.importer.TrackerCapture_importer_addTeiTaskSet;
import org.hisp.dhis.tasksets.tracker.importer.TrackerCapture_importer_searchForTeiTaskSet;

import java.util.Arrays;
import java.util.List;

/**
 * @author Gintare Vilkelyte <vilkelyte.gintare@gmail.com>
 */
@Category( category = CategoryType.NTI )
public class TrackerNewImporterTests
    implements PerformanceTest
{
    @Override
    public List<DhisAbstractTask> getTasks()
    {
        return Arrays.asList(
            new Android_importer_syncTeisTaskSet( 1 ),
            new Capture_importer_addEventTaskSet( 1 ),
            new TrackerCapture_importer_searchForTeiTaskSet( 1 ),
            new TrackerCapture_importer_addTeiTaskSet( 1 )
        );
    }
}

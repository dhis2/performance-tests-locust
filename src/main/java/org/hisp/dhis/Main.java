package org.hisp.dhis;

import com.github.myzhan.locust4j.AbstractTask;
import com.github.myzhan.locust4j.Locust;
import io.restassured.RestAssured;
import org.hisp.dhis.cache.EntitiesCache;
import org.hisp.dhis.locust.LocustSlave;
import org.hisp.dhis.tasks.aggregate.AddDataValueTask;
import org.hisp.dhis.tasks.tracker.PostRelationshipTask;
import org.hisp.dhis.tasksets.aggregate.Android_syncDataValuesTaskSet;
import org.hisp.dhis.tasksets.tracker.*;
import org.hisp.dhis.tasksets.tracker.importer.Android_importer_syncTeisTaskSet;
import org.hisp.dhis.tasksets.tracker.importer.Capture_importer_addEventTaskSet;
import org.hisp.dhis.tasksets.tracker.importer.TrackerCapture_importer_addTeiTaskSet;
import org.hisp.dhis.tasksets.tracker.importer.TrackerCapture_importer_searchForTeiTaskSet;
import org.hisp.dhis.tests.CategoryType;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import static io.restassured.RestAssured.preemptive;
import static org.aeonbits.owner.ConfigFactory.create;
import static org.hisp.dhis.utils.CacheUtils.initCache;

/**
 * @author Gintare Vilkelyte <vilkelyte.gintare@gmail.com>
 */
public class Main
{
    private static final TestConfig cfg = create( TestConfig.class );

    public static void main( String[] args )
        throws IOException, InstantiationException, IllegalAccessException
    {
        RestAssuredConfig.init();

        RestAssured.authentication = preemptive().basic( cfg.adminUsername(), cfg.adminPassword() );

        EntitiesCache.setInstance( initCache( cfg ));

        LocustSlave locust = LocustSlave.newInstance();

       //ß locust.runTasks(  new TrackerCapture_addTeiTaskSet( 1 ) ) ;
        // categories are under /src/main/tests
        locust.runTasks( CategoryType.ALL, CategoryType.NTI );
    }

    private static void runNewAndOldImporterTests( Locust locust )
    {
        locust.run(
            new TrackerCapture_searchForTeiByUniqueAttributeTaskSet( 1 ),
            new Android_importer_syncTeisTaskSet( 1 ),
            new Capture_importer_addEventTaskSet( 1 ),
            new TrackerCapture_importer_searchForTeiTaskSet( 1 ),
            new TrackerCapture_importer_addTeiTaskSet( 1 ),
            new TrackerCapture_addTeiTaskSet( 1 ),
            new TrackerCapture_searchForTeiTaskSet( 1 ),
            new Capture_addEventTaskSet( 1 ),
            new Android_syncTeisTaskSet( 1 ),
            new PostRelationshipTask( 1 )
        );
    }

    private static List<AbstractTask> getOldImporterTests()
    {
        return Arrays.asList(
            new TrackerCapture_addTeiTaskSet( 1 ),
            //new TrackerCapture_searchForTeiTaskSet( 1, cache ),
            new Capture_addEventTaskSet( 1 ),
            new Android_syncTeisTaskSet( 1 ),
            new PostRelationshipTask( 1 )
        );
    }

    public static void runCovaxTests( Locust locust )
    {
        locust.run(
            new Android_syncTeisTaskSet( 5 ),
            new AddDataValueTask( 1 ),
            new Android_syncDataValuesTaskSet( 1 ),
            new TrackerCapture_addTeiTaskSet( 2 ),
            new Capture_addEventTaskSet( 2 ),
            new TrackerCapture_searchForTeiTaskSet( 1 ),
            //new LoadDashboardTask( 1, cache ),
            new PostRelationshipTask( 1 ),
            new TrackerCapture_searchForTeiByUniqueAttributeTaskSet( 1 )
        );
    }

    public static void runAndroidTests( Locust locust )
    {
        locust.run(
            new Android_syncTeisTaskSet( 2, 1 ),
            new Android_syncTeisTaskSet( 2, 10 ),
            new Android_syncTeisTaskSet( 2, 20 )
        );
    }

    public static void runNewImporterTests( Locust locust )
    {
        locust.run(

            new Android_importer_syncTeisTaskSet( 1 ),
            new Capture_importer_addEventTaskSet( 1 ),
            new TrackerCapture_importer_searchForTeiTaskSet( 1 ),
            new TrackerCapture_importer_addTeiTaskSet( 1 )
        );
    }

    public static void runTest( Locust locust )
    {
        locust.dryRun( new PostRelationshipTask( 1 ) );
    }
}

package org.hisp.dhis;

import com.github.myzhan.locust4j.AbstractTask;
import com.github.myzhan.locust4j.Locust;
import org.hisp.dhis.cache.EntitiesCache;
import org.hisp.dhis.locust.LocustConfig;
import org.hisp.dhis.locust.LocustSlave;
import org.hisp.dhis.tasks.LoginTask;
import org.hisp.dhis.tasks.aggregate.AddDataValueTask;
import org.hisp.dhis.tasks.tracker.PostRelationshipTask;
import org.hisp.dhis.tasksets.aggregate.Android_syncDataValuesTaskSet;
import org.hisp.dhis.tasksets.tracker.*;
import org.hisp.dhis.tasksets.tracker.importer.Android_importer_syncTeisTaskSet;
import org.hisp.dhis.tasksets.tracker.importer.Capture_importer_addEventTaskSet;
import org.hisp.dhis.tasksets.tracker.importer.TrackerCapture_importer_addTeiTaskSet;
import org.hisp.dhis.tasksets.tracker.importer.TrackerCapture_importer_searchForTeiTaskSet;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import static org.aeonbits.owner.ConfigFactory.create;
import static org.hisp.dhis.utils.CacheUtils.initCache;

/**
 * @author Gintare Vilkelyte <vilkelyte.gintare@gmail.com>
 */
public class Main
{
    private static final LocustConfig cfg = create( LocustConfig.class );

    private static Logger logger = Logger.getLogger( Main.class.getName() );

    public static void main( String[] args )
        throws IOException
    {
        Configuration.init();
        new LoginTask( cfg.adminUsername(), cfg.adminPassword() ).execute();
        EntitiesCache cache = initCache( cfg );

        Locust locust = LocustSlave.newInstance().init();

        runCovaxTests( locust, cache );
    }

    private static void runNewAndOldImporterTests( Locust locust, EntitiesCache cache )
    {
        locust.run(
            new TrackerCapture_searchForTeiByUniqueAttributeTaskSet( 1, cache ),
            new Android_importer_syncTeisTaskSet( 1, cache ),
            new Capture_importer_addEventTaskSet( 1, cache ),
            new TrackerCapture_importer_searchForTeiTaskSet( 1, cache ),
            new TrackerCapture_importer_addTeiTaskSet( 1, cache ),
            new TrackerCapture_addTeiTaskSet( 1, cache ),
            new TrackerCapture_searchForTeiTaskSet( 1, cache ),
            new Capture_addEventTaskSet( 1, cache ),
            new Android_syncTeisTaskSet( 1, cache ),
            new PostRelationshipTask( 1, cache )
        );
    }

    private static List<AbstractTask> getOldImporterTests( EntitiesCache cache )
    {
        return Arrays.asList(
            new TrackerCapture_addTeiTaskSet( 1, cache ),
            //new TrackerCapture_searchForTeiTaskSet( 1, cache ),
            new Capture_addEventTaskSet( 1, cache ),
            new Android_syncTeisTaskSet( 1, cache ),
            new PostRelationshipTask( 1, cache )
        );
    }

    public static void runCovaxTests( Locust locust, EntitiesCache cache )
    {
        locust.run(
            new Android_syncTeisTaskSet( 5, cache ),
            new AddDataValueTask( 1, cache ),
            new Android_syncDataValuesTaskSet( 1, cache ),
            new TrackerCapture_addTeiTaskSet( 2, cache ),
            new Capture_addEventTaskSet( 2, cache ),
            new TrackerCapture_searchForTeiTaskSet( 1, cache ),
            //new LoadDashboardTask( 1, cache ),
            new PostRelationshipTask( 1, cache ),
            new TrackerCapture_searchForTeiByUniqueAttributeTaskSet( 1, cache )
        );
    }

    public static void runAndroidTests( Locust locust, EntitiesCache cache )
    {
        locust.run(
            new Android_syncTeisTaskSet( 2, cache, 1 ),
            new Android_syncTeisTaskSet( 2, cache, 10 ),
            new Android_syncTeisTaskSet( 2, cache, 20 )
        );
    }

    public static void runNewImporterTests( Locust locust, EntitiesCache cache )
    {
        locust.run(

            new Android_importer_syncTeisTaskSet( 1, cache ),
            new Capture_importer_addEventTaskSet( 1, cache ),
            new TrackerCapture_importer_searchForTeiTaskSet( 1, cache ),
            new TrackerCapture_importer_addTeiTaskSet( 1, cache )
        );
    }

    public static void runTest( Locust locust, EntitiesCache cache )
    {
        locust.dryRun( new PostRelationshipTask( 1, cache ) );
    }
}

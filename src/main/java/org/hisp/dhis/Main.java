package org.hisp.dhis;

import io.restassured.RestAssured;
import org.hisp.dhis.cache.EntitiesCache;
import org.hisp.dhis.locust.LocustSlave;
import org.hisp.dhis.tests.CategoryType;

import java.io.IOException;

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

        EntitiesCache.setInstance( initCache( cfg ) );

        LocustSlave locust = LocustSlave.newInstance();

        // locust.runTasks(  new TrackerCapture_addTeiTaskSet( 1 ) ) ;
        // categories are under /src/main/tests
        locust.runTasks( CategoryType.ALL );
    }
}
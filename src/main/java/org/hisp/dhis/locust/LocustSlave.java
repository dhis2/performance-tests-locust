package org.hisp.dhis.locust;

import com.github.myzhan.locust4j.Locust;
import org.aeonbits.owner.ConfigFactory;

/**
 * The Locust slave connects to a Locust Master instance that runs the
 * performance tests
 *
 * @author Gintare Vilkelyte <vilkelyte.gintare@gmail.com>
 */
public class LocustSlave
{
    private LocustConfig cfg = ConfigFactory.create( LocustConfig.class );

    public static LocustSlave newInstance()
    {
        return new LocustSlave();
    }

    public Locust init()
    {
        Locust locust = Locust.getInstance();

        locust.setMasterPort( cfg.locustMasterPort() );
        locust.setMasterHost( cfg.locustMasterHost() );
        locust.setVerbose( true );

        return locust;
    }
}

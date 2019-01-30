package org.hisp.dhis.locust;

import com.github.myzhan.locust4j.Locust;

/**
 * @author Gintare Vilkelyte <vilkelyte.gintare@gmail.com>
 */
public class Benchmark
{
    public static Benchmark newInstance()
    {
        return new Benchmark();
    }

    public Locust init()
    {
        Locust locust = Locust.getInstance();

        locust.setMasterPort( 5557 );
        locust.setMasterHost( "127.0.0.1" );
        locust.setVerbose( true );

        return locust;
    }
}

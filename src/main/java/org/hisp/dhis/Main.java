package org.hisp.dhis;

import com.github.myzhan.locust4j.Locust;
import io.restassured.RestAssured;
import org.hisp.dhis.locust.Benchmark;
import org.hisp.dhis.tasks.MetadataExportImportTask;

/**
 * @author Gintare Vilkelyte <vilkelyte.gintare@gmail.com>
 */
public class Main
{
    public static void main( String[] args )
    {
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
        RestAssured.baseURI = "https://play.dhis2.org/dev";

        Benchmark benchmark = Benchmark.newInstance();

        Locust locust = benchmark.init();

        locust.run( new MetadataExportImportTask() );
    }
}

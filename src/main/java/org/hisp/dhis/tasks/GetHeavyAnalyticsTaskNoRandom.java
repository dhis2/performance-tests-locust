package org.hisp.dhis.tasks;

import static com.google.api.client.http.HttpStatusCodes.STATUS_CODE_OK;
import static io.restassured.RestAssured.given;
import static java.lang.String.join;
import static java.lang.String.valueOf;
import static java.util.Arrays.asList;

import java.util.List;

import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

/**
 * @author Maikel Arabori <maikelarabori@gmail.com>
 */
public class GetHeavyAnalyticsTaskNoRandom
    extends
    DhisAbstractTask
{
    /**
     * Configurable constants
     */
    private static final String ORG_UNIT = "ou:ImspTQPwCqd";

    private static final String PERIOD = "pe:LAST_12_MONTHS";

    private static final String DISPLAY_PROPERTY = "NAME";

    private static final boolean SKIP_META = true;

    private static final boolean INCLUDE_NUM_DEN = true;

    private static final String DIMENSIONS = "dx:" + join( ";",
        asList( "AUqdhY4mpvp", "EY8gsfEomc0", "EoYar8UxddG", "GQHKiAe3DHR", "GfYjGL16D0W", "JIVMtpjVZqJ", "Lzg9LtG1xg3",
            "MkXHDfrFRKV", "OdiHJayrsKo", "Q3M7Htpzg1Y", "RdkKj1rVp8R", "ReUHfIn0pTQ", "Rigf2d2Zbjp", "Tt5TAvdfdVK",
            "ULW9dQOiiTS", "Uvn6LCg7dVU", "X2XfzgH4NOR", "X3taFC1HtE5", "btgZ1oeF9pJ", "c8fABiNpT0B", "cTB6uRdmxUB",
            "dwEq7wi6nXV", "elTqMUTRgdk", "fLbrQ4jEtn6", "iug406zdfTE", "lI5Q5vkydYu", "lOiynlltFdy", "lZZxDlIsvTc",
            "n0GE1ISYrdM", "nfG18MJZX5o", "nkjlWUMIdHh", "oTOpKabJA1v", "puykO1tbcdi", "qmCKRmVj4WX", "ryFBhJdetdM",
            "sB79w2hiLp8", "sMTMkudvLCD", "tcs5YGnjiKo", "ulgL07PF8rq", "vKWOc4itBo2", "vihpFUg2WTy" ) );

    private static final int API_VERSION = 30;

    public GetHeavyAnalyticsTaskNoRandom( final int weight )
    {
        this.weight = weight;
    }

    public String getName()
    {
        return "(static) GET " + query();
    }

    public void execute()
    {
        // Warm-up
        final RequestSpecification request = given().queryParam( "filter", ORG_UNIT )
            .queryParam( "dimension", DIMENSIONS ).queryParam( "dimension", PERIOD )
            .queryParam( "displayProperty", DISPLAY_PROPERTY ).queryParam( "skipMeta", SKIP_META )
            .queryParam( "includeNumDen", INCLUDE_NUM_DEN );

        request.get( endpoint() );

        // Test
        final Response response = request.get( endpoint() );

        // Assert and record
        if ( response.statusCode() == STATUS_CODE_OK )
        {
            recordSuccess( response );
        }
        else
        {
            recordFailure( response );
        }
    }

    private String endpoint()
    {
        return "/api/" + API_VERSION + "/analytics.json";
    }

    private String query()
    {
        final List<String> params = asList( ORG_UNIT, PERIOD, DIMENSIONS, DISPLAY_PROPERTY, valueOf( SKIP_META ),
            valueOf( INCLUDE_NUM_DEN ) );
        return endpoint() + " with params: " + join( " # ", params );
    }
}

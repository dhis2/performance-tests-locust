package org.hisp.dhis.tasks.analytics;

import org.hisp.dhis.actions.RestApiActions;
import org.hisp.dhis.request.QueryParamsBuilder;
import org.hisp.dhis.response.dto.ApiResponse;
import org.hisp.dhis.tasks.DhisAbstractTask;
import org.hisp.dhis.utils.Randomizer;

import java.util.List;

import static com.google.api.client.http.HttpStatusCodes.STATUS_CODE_OK;
import static java.lang.String.join;
import static java.lang.String.valueOf;
import static java.util.Arrays.asList;

/**
 * @author Maikel Arabori <maikelarabori@gmail.com>
 */
public class GetHeavyAnalyticsTask
    extends
    DhisAbstractTask
{
    /**
     * Configurable constants
     */
    private static final String ORG_UNIT = "ou:ImspTQPwCqd";

    private static final String PERIOD = "pe:LAST_12_MONTHS";

    private static final String DISPLAY_PROPERTY = "NAME";

    private static final String SKIP_META = "true";

    private static final String INCLUDE_NUM_DEN = "true";

    private static final String DIMENSIONS = "dx:" + join( ";",
        asList( "AUqdhY4mpvp", "EY8gsfEomc0", "EoYar8UxddG", "GQHKiAe3DHR", "GfYjGL16D0W", "JIVMtpjVZqJ", "Lzg9LtG1xg3",
            "MkXHDfrFRKV", "OdiHJayrsKo", "Q3M7Htpzg1Y", "RdkKj1rVp8R", "ReUHfIn0pTQ", "Rigf2d2Zbjp", "Tt5TAvdfdVK",
            "ULW9dQOiiTS", "Uvn6LCg7dVU", "X2XfzgH4NOR", "X3taFC1HtE5", "btgZ1oeF9pJ", "c8fABiNpT0B", "cTB6uRdmxUB",
            "dwEq7wi6nXV", "elTqMUTRgdk", "fLbrQ4jEtn6", "iug406zdfTE", "lI5Q5vkydYu", "lOiynlltFdy", "lZZxDlIsvTc",
            "n0GE1ISYrdM", "nfG18MJZX5o", "nkjlWUMIdHh", "oTOpKabJA1v", "puykO1tbcdi", "qmCKRmVj4WX", "ryFBhJdetdM",
            "sB79w2hiLp8", "sMTMkudvLCD", "tcs5YGnjiKo", "ulgL07PF8rq", "vKWOc4itBo2", "vihpFUg2WTy" ) );

    private final int apiVersion;

    public GetHeavyAnalyticsTask( final int weight, final int apiVersion, Randomizer randomizer )
    {
        super( weight,randomizer);
        this.weight = weight;
        this.apiVersion = apiVersion;
    }

    public String getName()
    {
        return "(static) GET " + query();
    }

    @Override
    public String getType()
    {
        return "GET";
    }

    public void execute()
    {

        // Given
        RestApiActions restApiActions = new RestApiActions( endpoint() );

        QueryParamsBuilder queryParamsBuilder = new QueryParamsBuilder()
            .add( "filter", ORG_UNIT )
            .add( "dimension", PERIOD )
            .add( "dimension", DIMENSIONS )
            .add( "displayProperty", DISPLAY_PROPERTY )
            .add( "skipMeta", SKIP_META )
            .add( "includeNumDen", INCLUDE_NUM_DEN );

        // Test

        ApiResponse response = restApiActions.get( queryParamsBuilder.build() );
        // Assert and record

        if ( response.statusCode() == STATUS_CODE_OK )
        {
            recordSuccess( response.getRaw() );
        }
        else
        {
            recordFailure( response.getRaw() );
        }
    }

    private String endpoint()
    {
        return "/api/" + apiVersion + "/analytics";
    }

    private String query()
    {
        final List<String> params = asList( ORG_UNIT, PERIOD, DIMENSIONS, DISPLAY_PROPERTY, valueOf( SKIP_META ),
            valueOf( INCLUDE_NUM_DEN ) );
        return endpoint() + " with params: " + join( " # ", params );
    }
}

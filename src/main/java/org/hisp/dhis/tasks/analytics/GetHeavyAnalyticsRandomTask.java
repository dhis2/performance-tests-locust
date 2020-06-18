package org.hisp.dhis.tasks.analytics;

import org.hisp.dhis.actions.RestApiActions;
import org.hisp.dhis.cache.EntitiesCache;
import org.hisp.dhis.cache.Program;
import org.hisp.dhis.request.QueryParamsBuilder;
import org.hisp.dhis.response.dto.ApiResponse;
import org.hisp.dhis.tasks.DhisAbstractTask;
import org.hisp.dhis.utils.DataRandomizer;

import java.util.List;

import static com.google.api.client.http.HttpStatusCodes.STATUS_CODE_OK;
import static java.lang.String.*;
import static java.util.Arrays.asList;

/**
 * @author Maikel Arabori <maikelarabori@gmail.com>
 */
public class GetHeavyAnalyticsRandomTask
    extends
    DhisAbstractTask
{
    /**
     * Configurable constants
     */
    private static final String ORG_UNIT = "ou:%s";

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

    private final EntitiesCache entitiesCache;

    private final int apiVersion;

    public GetHeavyAnalyticsRandomTask( final int weight, final int apiVersion, final EntitiesCache entitiesCache )
    {
        this.weight = weight;
        this.apiVersion = apiVersion;
        this.entitiesCache = entitiesCache;
    }

    public String getName()
    {
        return "(random) GET " + query();
    }

    @Override
    public String getType()
    {
        return "GET";
    }

    public void execute()
    {
        // Given
        final Program aRandomProgram = randomProgram();
        final String aRandomOrgUnitUid = randomOrgUnitUid( aRandomProgram.getOrgUnits() );

        ApiResponse response = new RestApiActions( "api/analytics" )
            .get( "", new QueryParamsBuilder()
                .add( "filter", format( ORG_UNIT, aRandomOrgUnitUid ) )
                .add( "dimension", DIMENSIONS )
                .add( "dimension", PERIOD )
                .add( "displayProperty", DISPLAY_PROPERTY )
                .add( "skipMeta", SKIP_META )
                .add( "includeNumDen", INCLUDE_NUM_DEN )
            );

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
        return "/api/" + apiVersion + "/analytics.json";
    }

    private String query()
    {
        final List<String> params = asList( ORG_UNIT, PERIOD, DIMENSIONS, DISPLAY_PROPERTY, valueOf( SKIP_META ),
            valueOf( INCLUDE_NUM_DEN ) );
        return endpoint() + " with params: " + join( " # ", params );
    }

    private String randomOrgUnitUid( final List<String> programOrgUnits )
    {
        return DataRandomizer.randomElementFromList( programOrgUnits );
    }

    private Program randomProgram()
    {
        return DataRandomizer.randomElementFromList( entitiesCache.getPrograms() );
    }
}

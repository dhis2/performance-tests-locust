package org.hisp.dhis.tasks.tracker.tei;

import org.hisp.dhis.actions.RestApiActions;
import org.hisp.dhis.cache.EntitiesCache;
import org.hisp.dhis.dxf2.events.enrollment.Enrollment;
import org.hisp.dhis.random.EnrollmentRandomizer;
import org.hisp.dhis.random.EventRandomizer;
import org.hisp.dhis.random.RandomizerContext;
import org.hisp.dhis.random.TrackedEntityInstanceRandomizer;
import org.hisp.dhis.response.dto.ApiResponse;
import org.hisp.dhis.tasks.DhisAbstractTask;
import org.hisp.dhis.utils.DataRandomizer;
import org.hisp.dhis.utils.JsonParserUtils;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import io.restassured.http.ContentType;

/**
 * @author Gintare Vilkelyte <vilkelyte.gintare@gmail.com>
 */
public class GetAndUpdateTeiTask
    extends DhisAbstractTask
{
    private RestApiActions teiActions = new RestApiActions( "/api/trackedEntityInstances" );
    private EnrollmentRandomizer enrollmentRandomizer = new EnrollmentRandomizer();
    private EntitiesCache entitiesCache;
    public GetAndUpdateTeiTask( int weight, EntitiesCache cache ) {
        this.weight = weight;
        this.entitiesCache = cache;
    }

    @Override
    public String getName()
    {
        return "PUT /trackedEntityInstances";
    }

    @Override
    public void execute()
        throws Exception
    {
        JsonArray teis = new GetTeisTask( ).executeAndGetBody().getAsJsonArray( "trackedEntityInstances" );

        JsonObject tei = teis.get( DataRandomizer.randomIntInRange( 0, teis.getAsJsonArray().size() ) )
            .getAsJsonObject();

        Enrollment enrollment = enrollmentRandomizer.create( entitiesCache, new RandomizerContext() );

        JsonObject enrollmentJson = JsonParserUtils.toJsonObject( enrollment );
        tei.getAsJsonArray( "enrollments" ).add( enrollmentJson );

        ApiResponse response = teiActions.update( tei.get( "trackedEntityInstance" ).getAsString(), tei,
            ContentType.JSON.toString() );

        if ( response.statusCode() == 200 )
        {
            recordSuccess( response.getRaw() );
        }
        else
        {
            recordFailure( response.getRaw() );
        }

    }
}

package org.hisp.dhis.tasks.tracker.tei;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.restassured.http.ContentType;
import net.andreinc.mockneat.unit.types.Ints;
import org.hisp.dhis.actions.RestApiActions;
import org.hisp.dhis.cache.EntitiesCache;
import org.hisp.dhis.dxf2.events.enrollment.Enrollment;
import org.hisp.dhis.random.TrackedEntityInstanceRandomizer;
import org.hisp.dhis.response.dto.ApiResponse;
import org.hisp.dhis.tasks.DhisAbstractTask;
import org.hisp.dhis.dxf2.events.trackedentity.TrackedEntityInstance;
import org.hisp.dhis.tasks.GetAndUpdateEventsTask;
import org.hisp.dhis.utils.JsonParserUtils;

import java.util.Collections;

/**
 * @author Gintare Vilkelyte <vilkelyte.gintare@gmail.com>
 */
public class GetAndUpdateTeiTask
    extends DhisAbstractTask
{
    private RestApiActions teiActions = new RestApiActions( "/api/trackedEntityInstances" );

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

        JsonObject tei = teis.get( Ints.ints().range( 0, teis.getAsJsonArray().size() ).get() ).getAsJsonObject();

        Enrollment enrollment = new TrackedEntityInstanceRandomizer().createEnrollment( entitiesCache );

        JsonObject enrollmentJson = JsonParserUtils.toJsonObject( enrollment );
        tei.getAsJsonArray( "enrollments" ).add( enrollmentJson );

        ApiResponse response = teiActions.update( tei.get("trackedEntityInstance").getAsString(), tei, ContentType.JSON.toString() );

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

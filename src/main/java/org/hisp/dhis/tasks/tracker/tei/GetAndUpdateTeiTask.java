package org.hisp.dhis.tasks.tracker.tei;

import org.hisp.dhis.actions.RestApiActions;
import org.hisp.dhis.cache.EntitiesCache;
import org.hisp.dhis.cache.Program;
import org.hisp.dhis.cache.Tei;
import org.hisp.dhis.random.EnrollmentRandomizer;
import org.hisp.dhis.dxf2.events.trackedentity.Attribute;

import org.hisp.dhis.dxf2.events.enrollment.Enrollment;
import org.hisp.dhis.random.EnrollmentRandomizer;
import org.hisp.dhis.random.EventRandomizer;
import org.hisp.dhis.random.RandomizerContext;

import org.hisp.dhis.random.TrackedEntityInstanceRandomizer;
import org.hisp.dhis.response.dto.ApiResponse;
import org.hisp.dhis.tasks.DhisAbstractTask;
import org.hisp.dhis.utils.DataRandomizer;
import org.hisp.dhis.utils.JsonParserUtils;

import com.google.gson.JsonObject;

import io.restassured.http.ContentType;

import java.util.ArrayList;
import java.util.List;

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
        return "PUT /trackedEntityInstances/$id";
    }

    @Override
    public void execute()
    {
        List<Tei> teis = new ArrayList<>(  );
        Program program = new Program(  );

        // Some programs in cache doesn't necessarily have TEIs.
        while(teis == null || teis.size() == 0) {
            program = entitiesCache.getPrograms().get( DataRandomizer.randomIntInRange( 0, entitiesCache.getPrograms().size() ) );

            teis = entitiesCache.getTeis().get( program.getUid() );
        }

        List<Attribute> attributes = new TrackedEntityInstanceRandomizer(  ).getRandomAttributesList( program );

        Tei tei = teis.get( DataRandomizer.randomIntInRange( 0, teis.size() ) );

        // get full tei body

        JsonObject tei = teis.get( DataRandomizer.randomIntInRange( 0, teis.getAsJsonArray().size() ) )
            .getAsJsonObject();


        JsonObject teiBody = new GetTeiTask( tei.getUid() ).executeAndGetBody();

        teiBody.add( "attributes", JsonParserUtils.toJsonObject( attributes ) );


        // update

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

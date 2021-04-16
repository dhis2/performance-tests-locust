package org.hisp.dhis.tasks.tracker.tei;

import com.google.gson.JsonObject;
import io.restassured.http.ContentType;
import org.hisp.dhis.actions.RestApiActions;
import org.hisp.dhis.cache.Program;
import org.hisp.dhis.cache.Tei;
import org.hisp.dhis.dxf2.events.trackedentity.Attribute;
import org.hisp.dhis.random.TrackedEntityInstanceRandomizer;
import org.hisp.dhis.response.dto.ApiResponse;
import org.hisp.dhis.tasks.DhisAbstractTask;
import org.hisp.dhis.utils.DataRandomizer;
import org.hisp.dhis.utils.JsonParserUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Gintare Vilkelyte <vilkelyte.gintare@gmail.com>
 */
public class GetAndUpdateTeiTask
    extends DhisAbstractTask
{
    private RestApiActions teiActions = new RestApiActions( "/api/trackedEntityInstances" );

    public GetAndUpdateTeiTask( int weight )
    {
        super( weight );
    }

    @Override
    public String getName()
    {
        return "/trackedEntityInstances/$id";
    }

    @Override
    public String getType()
    {
        return "PUT";
    }

    @Override
    public void execute()
    {
        List<Tei> teis = new ArrayList<>();
        Program program = new Program();

        // Some programs in cache doesn't necessarily have TEIs.
        while ( teis == null || teis.isEmpty() )
        {
            program = entitiesCache.getTrackerPrograms()
                .get( DataRandomizer.randomIntInRange( 0, entitiesCache.getTrackerPrograms().size() ) );

            teis = entitiesCache.getTeis().get( program.getId() );
        }

        List<Attribute> attributes = new TrackedEntityInstanceRandomizer().getRandomAttributesList( program );

        Tei tei = teis.get( DataRandomizer.randomIntInRange( 0, teis.size() ) );

        // get full tei body

        ApiResponse response = new GetTeiTask( tei.getUid() ).executeAndGetResponse();

        JsonObject teiBody = response.getBody();
        teiBody.add( "attributes", JsonParserUtils.toJsonObject( attributes ) );

        // update

        response = teiActions.update( tei.getUid(), teiBody,
            ContentType.JSON.toString() );

        record( response.getRaw() );

    }

}

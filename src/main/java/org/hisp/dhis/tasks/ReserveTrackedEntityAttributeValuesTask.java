package org.hisp.dhis.tasks;

import com.github.myzhan.locust4j.Locust;
import org.hisp.dhis.actions.RestApiActions;
import org.hisp.dhis.request.QueryParamsBuilder;
import org.hisp.dhis.response.dto.ApiResponse;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ReserveTrackedEntityAttributeValuesTask
    extends
    DhisAbstractTask
{
    private String endpoint = "/api/trackedEntityAttributes";

    public ReserveTrackedEntityAttributeValuesTask( int weight )
    {
        this.weight = weight;
    }

    public String getName()
    {
        return "/api/trackedEntityAttributes/{id}/generateAndReserve";
    }

    @Override
    public String getType()
    {
        return "POST";
    }

    public void execute()
        throws Exception
    {
        RestApiActions trackedEntityAttributeActions = new RestApiActions( endpoint );
        long time = System.currentTimeMillis();

        List<ApiResponse> setupResponses = null;

        ApiResponse response = null;

        String attributeId = new CreateTrackedEntityAttributeTask().executeAndGetId();

        setupResponses = IntStream.range( 0, 10 ).mapToObj( r ->
            trackedEntityAttributeActions.get( attributeId + "/generateAndReserve",
                new QueryParamsBuilder().add( "numberToReserve", "800" ) ) )
            .collect( Collectors.toList() );

        response = trackedEntityAttributeActions.get( attributeId + "/generateAndReserve",
            new QueryParamsBuilder().add( "numberToReserve", "800" ) );

        if ( setupResponses.stream().allMatch( r -> r.statusCode() == 200 ) )
        {
            record( response.getRaw() );
        }

        else
        {
            ApiResponse failureResponse = setupResponses.stream().filter( r -> r.statusCode() != 200 ).findFirst().get();

            Locust.getInstance().recordFailure( "http", getName() + " SETUP",
                System.currentTimeMillis() - time, failureResponse.getRaw().getBody().print() );
        }
    }
}

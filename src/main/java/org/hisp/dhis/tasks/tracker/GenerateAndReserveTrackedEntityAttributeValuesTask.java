package org.hisp.dhis.tasks.tracker;

import org.hisp.dhis.actions.AuthenticatedApiActions;
import org.hisp.dhis.cache.UserCredentials;
import org.hisp.dhis.request.QueryParamsBuilder;
import org.hisp.dhis.response.dto.ApiResponse;
import org.hisp.dhis.tasks.DhisAbstractTask;

/**
 * @author Gintare Vilkelyte <vilkelyte.gintare@gmail.com>
 */
public class GenerateAndReserveTrackedEntityAttributeValuesTask
    extends DhisAbstractTask
{
    private String teiAttributeId;

    private int numberToReserve;

    private String endpoint = "/api/trackedEntityAttributes/id/generateAndReserve";

    private ApiResponse response;

    private String name = "";

    public GenerateAndReserveTrackedEntityAttributeValuesTask( int weight, String trackedEntityAttributeId,
        UserCredentials userCredentials, int numberToReserve )
    {
        super( weight );
        this.teiAttributeId = trackedEntityAttributeId;
        this.userCredentials = userCredentials;
        this.numberToReserve = numberToReserve;
    }

    public GenerateAndReserveTrackedEntityAttributeValuesTask( int weight, String trackedEntityAttributeId,
        UserCredentials userCredentials, int numberToReserve, String name )
    {
        super( weight );
        this.teiAttributeId = trackedEntityAttributeId;
        this.userCredentials = userCredentials;
        this.numberToReserve = numberToReserve;
        this.name = name;
    }
    @Override
    public String getName()
    {
        return endpoint + name;
    }

    @Override
    public String getType()
    {
        return "GET";
    }

    @Override
    public void execute()
    {
        AuthenticatedApiActions apiActions = new AuthenticatedApiActions( "", userCredentials );

        response = apiActions.get( endpoint.replace( "id", teiAttributeId ), new QueryParamsBuilder().add( "numberToReserve",
            String.valueOf( numberToReserve ) ) );

        record( response.getRaw() );
    }

    public ApiResponse executeAndGetResponse()
    {
        this.execute();
        return this.response;
    }
}


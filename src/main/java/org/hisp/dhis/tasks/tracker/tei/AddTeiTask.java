package org.hisp.dhis.tasks.tracker.tei;

import org.hisp.dhis.actions.AuthenticatedApiActions;
import org.hisp.dhis.cache.UserCredentials;
import org.hisp.dhis.dxf2.events.trackedentity.TrackedEntityInstances;
import org.hisp.dhis.random.RandomizerContext;
import org.hisp.dhis.random.TrackedEntityInstanceRandomizer;
import org.hisp.dhis.response.dto.ApiResponse;
import org.hisp.dhis.tasks.DhisAbstractTask;

public class AddTeiTask
    extends
    DhisAbstractTask
{
    private String endpoint = "/api/trackedEntityInstances";

    private TrackedEntityInstances trackedEntityInstanceBody;

    private ApiResponse response;

    public AddTeiTask( int weight, TrackedEntityInstances trackedEntityInstance,
        UserCredentials userCredentials )
    {
        super( weight );
        trackedEntityInstanceBody = trackedEntityInstance;
        this.userCredentials = userCredentials;

    }

    public String getName()
    {
        return this.endpoint;
    }

    @Override
    public String getType()
    {
        return "POST";
    }

    public void execute()
        throws Exception
    {

        trackedEntityInstanceBody = new TrackedEntityInstanceRandomizer()
            .create( this.entitiesCache, RandomizerContext.EMPTY_CONTEXT(), 5 );

        this.response = performTaskAndRecord(
            () -> new AuthenticatedApiActions( this.endpoint, getUserCredentials() ).post( trackedEntityInstanceBody ) );
    }

    public ApiResponse executeAndGetResponse()
        throws Exception
    {
        this.execute();
        return this.response;
    }
}

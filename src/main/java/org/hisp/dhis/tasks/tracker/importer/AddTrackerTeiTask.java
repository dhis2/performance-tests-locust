package org.hisp.dhis.tasks.tracker.importer;

import org.hisp.dhis.actions.AuthenticatedApiActions;
import org.hisp.dhis.cache.User;
import org.hisp.dhis.cache.UserCredentials;
import org.hisp.dhis.dxf2.events.trackedentity.TrackedEntityInstances;
import org.hisp.dhis.models.TrackedEntities;
import org.hisp.dhis.random.RandomizerContext;
import org.hisp.dhis.random.TrackedEntityInstanceRandomizer;
import org.hisp.dhis.random.UserRandomizer;
import org.hisp.dhis.request.QueryParamsBuilder;
import org.hisp.dhis.response.dto.TrackerApiResponse;
import org.hisp.dhis.tasks.DhisAbstractTask;
import org.hisp.dhis.tracker.domain.mapper.TrackedEntityMapperImpl;

import java.util.stream.Collectors;

/**
 * @author Gintare Vilkelyte <vilkelyte.gintare@gmail.com>
 */
public class AddTrackerTeiTask
    extends DhisAbstractTask
{
    private String endpoint = "/api/tracker";

    private TrackedEntities trackedEntityInstanceBody;

    private TrackerApiResponse response;

    public AddTrackerTeiTask( int weight )
    {
        super( weight );
    }

    public AddTrackerTeiTask( int weight, TrackedEntities trackedEntityInstance,
        UserCredentials userCredentials )
    {
        this( weight );
        trackedEntityInstanceBody = trackedEntityInstance;
        this.userCredentials = userCredentials;

    }

    public String getName()
    {
        return this.endpoint + ": teis";
    }

    @Override
    public String getType()
    {
        return "POST";
    }

    public void execute()
        throws Exception
    {
        User user = getUser();
        RandomizerContext context = new RandomizerContext();
        context.setOrgUnitUid( new UserRandomizer().getRandomUserOrgUnit( user ) );

        if ( trackedEntityInstanceBody == null )
        {
            TrackedEntityInstances ins = new TrackedEntityInstanceRandomizer()
                .create( this.entitiesCache, context, 5 );

            trackedEntityInstanceBody = TrackedEntities.builder().trackedEntities(
                ins.getTrackedEntityInstances().stream().map( p -> new TrackedEntityMapperImpl().from( p ) ).collect( Collectors
                    .toList() ) ).build();
        }

        response = new TrackerApiResponse( performTaskAndRecord(
            () -> new AuthenticatedApiActions( this.endpoint, user.getUserCredentials() )
                .post( trackedEntityInstanceBody, new QueryParamsBuilder().addAll( "async=false", "identifier=teis" ) ) ) );
    }

    public TrackerApiResponse executeAndGetResponse()
        throws Exception
    {
        this.execute();
        return this.response;
    }
}

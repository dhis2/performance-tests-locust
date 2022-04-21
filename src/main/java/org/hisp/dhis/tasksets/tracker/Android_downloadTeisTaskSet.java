package org.hisp.dhis.tasksets.tracker;

import org.hisp.dhis.actions.AuthenticatedApiActions;
import org.hisp.dhis.cache.User;
import org.hisp.dhis.random.UserRandomizer;
import org.hisp.dhis.request.QueryParamsBuilder;
import org.hisp.dhis.tasks.DhisAbstractTask;
import org.hisp.dhis.utils.DataRandomizer;

import java.time.Instant;

/**
 * @author Gintare Vilkelyte <vilkelyte.gintare@gmail.com>
 */
public class Android_downloadTeisTaskSet
    extends DhisAbstractTask
{
    private String endpoint = "/api/trackedEntityInstances";

    public Android_downloadTeisTaskSet( int weight )
    {
        super( weight );
    }

    @Override
    public String getName()
    {
        return "Android: download latest teis";
    }

    @Override
    public String getType()
    {
        return "GET";
    }

    @Override
    public void execute()
        throws Exception
    {
        User user = new UserRandomizer().getRandomUser( entitiesCache );

        QueryParamsBuilder queryParamsBuilder = new QueryParamsBuilder()
            .add( "ouMode=DESCENDANTS" )
            .add( "includeDeleted", "true" )
            .add( "includeAllAttributes", "true" )
            .add( "pageSize", "50" )
            .add( "page", String.valueOf( DataRandomizer.randomIntInRange( 1, 10 ) ))
            .add( "trackedEntityType", DataRandomizer.randomElementFromList( entitiesCache.getTeiTypes() ).getId() )
            .add( "paging", "true" )
            .add( "ou", new UserRandomizer().getRandomUserOrgUnit( user ) );

        performTaskAndRecord( () -> new AuthenticatedApiActions( endpoint, user.getUserCredentials() )
            .get( "", queryParamsBuilder ), response -> response.extractList( "trackedEntityInstances" ) != null );

        waitBetweenTasks();
    }
}

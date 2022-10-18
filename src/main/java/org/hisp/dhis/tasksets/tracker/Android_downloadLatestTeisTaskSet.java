package org.hisp.dhis.tasksets.tracker;

import org.hisp.dhis.actions.AuthenticatedApiActions;
import org.hisp.dhis.cache.User;
import org.hisp.dhis.request.QueryParamsBuilder;
import org.hisp.dhis.tasksets.DhisAbstractTaskSet;
import org.hisp.dhis.utils.Randomizer;

import java.time.Instant;

/**
 * @author Gintare Vilkelyte <vilkelyte.gintare@gmail.com>
 */
public class Android_downloadLatestTeisTaskSet extends DhisAbstractTaskSet
{
    private String endpoint = "/api/trackedEntityInstances";

    private static final String NAME = "Android: download latest teis";

    public Android_downloadLatestTeisTaskSet( int weight )
    {
        super( NAME, weight );
    }

    @Override
    public String getName()
    {
        return NAME;
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
        Randomizer rnd = getNextRandomizer( getName() );
        User user = getRandomUser(rnd);

        QueryParamsBuilder queryParamsBuilder = new QueryParamsBuilder()
            .add( "ouMode=DESCENDANTS" )
            .add( "includeDeleted", "true" )
            .add( "includeAllAttributes", "true" )
            .add( "pageSize", "50" )
            .add( "trackedEntityType", rnd.randomElementFromList( entitiesCache.getTeiTypes() ).getId() )
            .add( "paging", "true" )
            .add( "ou", getRandomUserOrgUnit( user, rnd ) )
            .add( "lastUpdatedStartDate", Instant.now().toString() );

        performTaskAndRecord( () -> new AuthenticatedApiActions( endpoint, user.getUserCredentials() )
            .get( "", queryParamsBuilder ), response -> response.extractList( "trackedEntityInstances" ) != null );

        waitBetweenTasks();
    }
}

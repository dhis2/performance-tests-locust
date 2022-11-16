package org.hisp.dhis.tasksets.tracker;

import org.hisp.dhis.actions.AuthenticatedApiActions;
import org.hisp.dhis.cache.Program;
import org.hisp.dhis.cache.User;
import org.hisp.dhis.request.QueryParamsBuilder;
import org.hisp.dhis.tasksets.DhisAbstractTaskSet;
import org.hisp.dhis.utils.Randomizer;

import java.time.Instant;

/**
 * @author Gintare Vilkelyte <vilkelyte.gintare@gmail.com>
 */
public class Android_downloadLatestEventsTaskSet
    extends DhisAbstractTaskSet
{
    private String endpoint = "/api/events";

    private static final String NAME = "Android: download latest events";

    public Android_downloadLatestEventsTaskSet( int weight )
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
        Program program = rnd.randomElementFromList( entitiesCache.getTrackerPrograms() );
        QueryParamsBuilder queryParamsBuilder = new QueryParamsBuilder()
            .add( "ouMode=DESCENDANTS" )
            .add( "includeDeleted", "true" )
            .add( "fields", "*" )
            .add( "includeAllAttributes", "true" )
            .add( "pageSize", "50" )
            .add( "program", program.getId() )
            .add( "paging", "true" )
            .add( "orgUnit", getRandomUserOrProgramOrgUnit( user, program, rnd ) )
            .add( "lastUpdatedStartDate", Instant.now().toString() );

        performTaskAndRecord( () -> new AuthenticatedApiActions( endpoint, user.getUserCredentials() )
            .get( "", queryParamsBuilder ), response -> response.extractList( "events" ) != null );

        waitBetweenTasks(rnd);
    }
}

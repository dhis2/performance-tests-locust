package org.hisp.dhis.tasksets.tracker;

import org.hisp.dhis.actions.AuthenticatedApiActions;
import org.hisp.dhis.cache.Program;
import org.hisp.dhis.cache.User;
import org.hisp.dhis.random.UserRandomizer;
import org.hisp.dhis.request.QueryParamsBuilder;
import org.hisp.dhis.tasks.DhisAbstractTask;
import org.hisp.dhis.utils.DataRandomizer;

import java.time.Instant;

/**
 * @author Gintare Vilkelyte <vilkelyte.gintare@gmail.com>
 */
public class Android_downloadLatestEventsTaskSet
    extends DhisAbstractTask
{
    private String endpoint = "/api/events";

    public Android_downloadLatestEventsTaskSet( int weight )
    {
        super( weight );
    }

    @Override
    public String getName()
    {
        return "Android: download latest events";
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
        Program program = DataRandomizer.randomElementFromList( entitiesCache.getEventPrograms() );
        QueryParamsBuilder queryParamsBuilder = new QueryParamsBuilder()
            .add( "ouMode=DESCENDANTS" )
            .add( "includeDeleted", "true" )
            .add( "fields", "*" )
            .add( "includeAllAttributes", "true" )
            .add( "pageSize", "50" )
            .add( "program", program.getId() )
            .add( "paging", "true" )
            .add( "orgUnit", new UserRandomizer().getRandomUserOrProgramOrgUnit( user, program ) )
            .add( "lastUpdatedStartDate", Instant.now().toString() );

        performTaskAndRecord( () -> new AuthenticatedApiActions( endpoint, user.getUserCredentials() )
            .get( "", queryParamsBuilder ), response -> response.extractList( "events" ) != null );

    }
}

package org.hisp.dhis.tasks.tracker;

import org.hisp.dhis.actions.AuthenticatedApiActions;
import org.hisp.dhis.cache.UserCredentials;
import org.hisp.dhis.tasks.DhisAbstractTask;
import org.hisp.dhis.utils.Randomizer;

/**
 * @author Gintare Vilkelyte <vilkelyte.gintare@gmail.com>
 */
public class QueryTrackerEventsTask
    extends
    DhisAbstractTask
{
    private String endpoint = "/api/tracker/events";

    private String query;

    public QueryTrackerEventsTask(String query, UserCredentials userCredentials, Randomizer randomizer)
    {
        super( 1, randomizer );
        this.query = query;
        this.userCredentials = userCredentials;
    }

    public String getName()
    {
        return endpoint;
    }

    @Override
    public String getType()
    {
        return "GET";
    }

    public void execute()
        throws Exception
    {
        performTaskAndRecord( () -> new AuthenticatedApiActions( this.endpoint, this.userCredentials ).get( this.query ) );
    }
}


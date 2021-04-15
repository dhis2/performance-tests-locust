package org.hisp.dhis.tasks.tracker.importer;

import org.hisp.dhis.actions.AuthenticatedApiActions;
import org.hisp.dhis.cache.UserCredentials;
import org.hisp.dhis.tasks.DhisAbstractTask;

/**
 * @author Gintare Vilkelyte <vilkelyte.gintare@gmail.com>
 */
public class QueryTrackerEventsTask
    extends
    DhisAbstractTask
{
    private String endpoint = "/api/tracker/events";

    private String query;

    private boolean savePayload = false;

    public QueryTrackerEventsTask( String query, UserCredentials userCredentials )
    {
        this.query = query;
        this.userCredentials = userCredentials;
    }

    public int getWeight()
    {
        return 1;
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
        performTaskAndRecord( () -> new AuthenticatedApiActions( this.endpoint, getUserCredentials() ).get( this.query ) );

    }
}


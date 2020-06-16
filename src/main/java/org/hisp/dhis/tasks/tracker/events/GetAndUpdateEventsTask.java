package org.hisp.dhis.tasks.tracker.events;

import com.google.gson.JsonObject;
import org.hisp.dhis.tasks.DhisAbstractTask;
import org.hisp.dhis.tasks.LoginTask;

/**
 * @author Gintare Vilkelyte <vilkelyte.gintare@gmail.com>
 */
public class GetAndUpdateEventsTask
    extends DhisAbstractTask
{
    private String query;

    public GetAndUpdateEventsTask( int weight, String eventsQuery )
    {
        this.weight = weight;
        this.query = eventsQuery;
    }

    @Override
    public String getName()
    {
        return "Get and update events task";
    }

    @Override
    public String getType()
    {
        return "http";
    }

    public void execute()
    {
        new LoginTask().execute();

        JsonObject events = new QueryEventsTask( query ).executeAndGetBody();

        new PostEventsTask( events ).execute();
    }
}

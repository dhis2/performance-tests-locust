package org.hisp.dhis.tasks;

import com.google.gson.JsonObject;

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

    public String getName()
    {
        return "Get and update events task";
    }

    public void execute()
    {
        new LoginTask().execute();

        JsonObject events = new QueryEventsTask( query ).executeAndGetBody();

        new PostEventsTask( events ).execute();
    }
}

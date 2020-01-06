package org.hisp.dhis.tasks;

import com.google.gson.JsonObject;

/**
 * @author Gintare Vilkelyte <vilkelyte.gintare@gmail.com>
 */
public class GetAndUpdateEventsTask
    extends DhisAbstractTask
{
    private String query;

    public GetAndUpdateEventsTask( String eventsQuery )
    {
        this.query = eventsQuery;
    }

    public int getWeight()
    {
        return 1;
    }

    public String getName()
    {
        return "Get and update events task";
    }

    public void execute()
        throws Exception
    {
        new LoginTask().execute();

        JsonObject events = new QueryEventsTask( query ).executeAndGetBody();

        new PostEventsTask( events ).execute();
    }
}

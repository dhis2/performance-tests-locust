package org.hisp.dhis.tasks.tracker.importer;

import org.hisp.dhis.actions.AuthenticatedApiActions;
import org.hisp.dhis.actions.RestApiActions;
import org.hisp.dhis.cache.UserCredentials;
import org.hisp.dhis.models.Events;
import org.hisp.dhis.random.EventRandomizer;
import org.hisp.dhis.random.RandomizerContext;
import org.hisp.dhis.response.dto.TrackerApiResponse;
import org.hisp.dhis.tasks.DhisAbstractTask;
import org.hisp.dhis.tracker.domain.Event;
import org.hisp.dhis.tracker.domain.mapper.EventMapperImpl;
import org.hisp.dhis.utils.Randomizer;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * @author Gintare Vilkelyte <vilkelyte.gintare@gmail.com>
 */
public class AddTrackerEventsTask
    extends DhisAbstractTask
{
    private EventRandomizer eventRandomizer;

    private String endpoint = "/api/tracker";

    private Events events;

    private TrackerApiResponse response;

    private Logger logger = Logger.getLogger( this.getClass().getName() );

    public AddTrackerEventsTask( int weight, Events events, UserCredentials userCredentials,
                                 Randomizer randomizer )
    {
        super( weight, randomizer );
        eventRandomizer = new EventRandomizer(randomizer);
        this.events = events;
        this.userCredentials = userCredentials;
    }

    @Override
    public String getName()
    {
        return endpoint + ": events";
    }

    @Override
    public String getType()
    {
        return "POST";
    }

    @Override
    public void execute()
        throws Exception
    {
        Randomizer rnd = getNextRandomizer( getName() );

        response = new AddTrackerDataTask( 1, this.userCredentials, events, "events", rnd ).executeAndGetBody();
    }

    public TrackerApiResponse executeAndGetResponse()
        throws Exception
    {
        this.execute();
        return response;
    }

}


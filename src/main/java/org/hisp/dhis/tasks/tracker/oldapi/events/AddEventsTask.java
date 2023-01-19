package org.hisp.dhis.tasks.tracker.oldapi.events;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.hisp.dhis.actions.AuthenticatedApiActions;
import org.hisp.dhis.cache.UserCredentials;
import org.hisp.dhis.dxf2.events.event.Event;
import org.hisp.dhis.dxf2.events.event.Events;
import org.hisp.dhis.random.EventRandomizer;
import org.hisp.dhis.response.dto.ApiResponse;
import org.hisp.dhis.tasks.DhisAbstractTask;
import org.hisp.dhis.utils.Randomizer;

import java.util.List;
import java.util.logging.Logger;

/**
 * @author Luciano Fiandesio <luciano@dhis2.org>
 */
public class AddEventsTask
    extends DhisAbstractTask
{
    ApiResponse response;

    private EventRandomizer eventRandomizer;

    private String endpoint = "/api/events";

    private Events events;

    private Logger logger = Logger.getLogger( this.getClass().getName() );

    public AddEventsTask( int weight, Events events, UserCredentials userCredentials,
                          Randomizer randomizer )
    {
        super( weight,randomizer );
        eventRandomizer = new EventRandomizer(randomizer);
        this.events = events;
        this.userCredentials = userCredentials;
    }

    @Override
    public String getName()
    {
        return endpoint;
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
        response = performTaskAndRecord( () -> new AuthenticatedApiActions( this.endpoint, this.userCredentials ).post( this.events ) );
    }

    public ApiResponse executeAndGetResponse()
        throws Exception
    {
        this.execute();
        return response;
    }

    // We need to wrap the list of events with a root element
    static class EventWrapper
    {
        @JsonProperty( "events" )
        private List<Event> events;

        public EventWrapper( List<Event> events )
        {
            this.events = events;
        }

        public List<Event> getEvents()
        {
            return events;
        }
    }
}

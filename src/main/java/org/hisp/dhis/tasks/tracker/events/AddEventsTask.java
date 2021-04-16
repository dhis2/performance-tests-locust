package org.hisp.dhis.tasks.tracker.events;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.hisp.dhis.actions.AuthenticatedApiActions;
import org.hisp.dhis.cache.UserCredentials;
import org.hisp.dhis.dxf2.events.event.Event;
import org.hisp.dhis.random.EventRandomizer;
import org.hisp.dhis.random.RandomizerContext;
import org.hisp.dhis.response.dto.ApiResponse;
import org.hisp.dhis.tasks.DhisAbstractTask;
import org.hisp.dhis.utils.DataRandomizer;

import java.util.ArrayList;
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

    private List<String> blackListedTeis = new ArrayList<>();

    private List<Event> events;

    private Logger logger = Logger.getLogger( this.getClass().getName() );

    public AddEventsTask( int weight )
    {
        super( weight );
        eventRandomizer = new EventRandomizer();
    }

    public AddEventsTask( int weight, List<Event> events, UserCredentials userCredentials )
    {
        super( weight );
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

    private List<Event> createRandomEvents()
    {

        List<Event> rndEvents = new ArrayList<>();
        for ( int i = 0; i < DataRandomizer.randomIntInRange( 5, 10 ); i++ )
        {
            Event randomEvent = null;
            try
            {
                randomEvent = eventRandomizer.create( entitiesCache, RandomizerContext.EMPTY_CONTEXT() );

            }
            catch ( Exception e )
            {
                logger.warning( "An error occurred while creating a random event: " + e.getMessage() );
            }

            if ( randomEvent != null && !blackListedTeis.contains( randomEvent.getTrackedEntityInstance() ) )
            {
                rndEvents.add( randomEvent );
            }
        }

        return rndEvents;
    }

    @Override
    public void execute()
        throws Exception
    {
        List<Event> rndEvents = events != null ? events : createRandomEvents();

        EventWrapper ew = new EventWrapper( rndEvents );

        response = performTaskAndRecord( () -> new AuthenticatedApiActions( this.endpoint, getUserCredentials() ).post( ew ) );
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

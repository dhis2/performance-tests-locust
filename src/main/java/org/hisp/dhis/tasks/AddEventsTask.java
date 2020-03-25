package org.hisp.dhis.tasks;

import static com.google.api.client.http.HttpStatusCodes.STATUS_CODE_OK;

import java.util.ArrayList;
import java.util.List;

import org.hisp.dhis.actions.RestApiActions;
import org.hisp.dhis.cache.EntitiesCache;
import org.hisp.dhis.dxf2.events.event.Event;
import org.hisp.dhis.random.EventRandomizer;
import org.hisp.dhis.random.RandomizerContext;
import org.hisp.dhis.response.dto.ApiResponse;
import org.hisp.dhis.utils.DataRandomizer;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author Luciano Fiandesio <luciano@dhis2.org>
 */
public class AddEventsTask
    extends DhisAbstractTask
{
    private EntitiesCache entitiesCache;
    private EventRandomizer eventRandomizer;
    private String endpoint = "/api/events";
    public AddEventsTask(int weight, EntitiesCache entitiesCache )
    {
        this.weight = weight;
        this.entitiesCache = entitiesCache;
        eventRandomizer = new EventRandomizer();
    }

    @Override
    public String getName()
    {
        return "POST events";
    }

    private Event createRandomEvent()
    {
        try
        {
            return eventRandomizer.create( entitiesCache, RandomizerContext.EMPTY_CONTEXT() );
        }
        catch ( Exception e )
        {
            System.out.println("An error occurred while creating a random event: " + e.getMessage());
        }
        return null;
    }

    @Override
    public void execute()
    {
        List<Event> rndEvents = new ArrayList<>();
        for ( int i = 0; i < DataRandomizer.randomIntInRange( 5, 10 ); i++ )
        {
            Event randomEvent = createRandomEvent();
            if ( randomEvent != null )
            {
                rndEvents.add( randomEvent );
            }
        }
        
        RestApiActions apiActions = new RestApiActions( this.endpoint );

        ApiResponse response = apiActions.post( new EventWrapper( rndEvents ) );

        if ( response.statusCode() == STATUS_CODE_OK )
        {
            recordSuccess( response.getRaw() );
        }
        else
        {
            recordFailure( response.getRaw() );
        }
    }

    // We need to wrap the list of events with a root element
    static class EventWrapper
    {
        @JsonProperty( "events" )
        private List<Event> events;

        public EventWrapper(List<Event> events) {
            this.events = events;
        }

        public List<Event> getEvents()
        {
            return events;
        }
    }
}

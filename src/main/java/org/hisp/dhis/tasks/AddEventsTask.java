package org.hisp.dhis.tasks;

import static com.google.api.client.http.HttpStatusCodes.STATUS_CODE_OK;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.map.ObjectMapper;
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

    private List<String> blackListedTeis = new ArrayList<>();

    public AddEventsTask( int weight, EntitiesCache entitiesCache )
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
            System.out.println( "An error occurred while creating a random event: " + e.getMessage() );
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

            if ( randomEvent != null && !blackListedTeis.contains( randomEvent.getTrackedEntityInstance() ) )
            {

                rndEvents.add( randomEvent );
            }
        }

        RestApiActions apiActions = new RestApiActions( this.endpoint );
        EventWrapper ew = new EventWrapper( rndEvents );
        ApiResponse response = apiActions.post( ew );

        if ( response.statusCode() == STATUS_CODE_OK )
        {
            recordSuccess( response.getRaw() );
        }
        else
        {
            addTeiToBlacklist( response );

            recordFailure( response.getRaw() );
        }
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

    /**
     * A new PSI, must be linked to a Program Instance.
     * Since it is not possible to fetch Program Instances via API (and therefore cache them),
     * in order to avoid reusing invalid Tracked Entity Instances (which are the link between a PSI and a PI),
     * this method analyzed the response payload and checks if one of the ImportSummaries contains an error
     * ending for: 'is not enrolled in program". This error signal that there was Program Instance found by Tei + Program.
     *
     * If the message is found the TEI uid is extracted and added to a "Tei Black List", so that the same tei is not reused
     * in the context of the performance test
     *
     * @param response
     */
    private void addTeiToBlacklist( ApiResponse response )
    {

        try
        {
            String errorString = response.getAsString();
            Map<String, Object> map = new ObjectMapper().readValue( errorString, Map.class );
            Map<String, Object> responseJson = (Map<String, Object>) map.get( "response" );
            List importSummaries = (List) responseJson.get( "importSummaries" );
            for ( Object importSummary : importSummaries )
            {

                Map is = (Map) importSummary;
                if ( is.get( "status" ).equals( "ERROR" ) )
                {
                    String desc = (String) is.get( "description" );
                    if ( desc != null && desc.contains( "is not enrolled in program" ) )
                    {
                        blackListedTeis.add( StringUtils.substringBetween( desc, "instance: ", " is not" ) );
                    }
                }

            }
        }
        catch ( IOException e )
        {
            e.printStackTrace();
        }
    }
}

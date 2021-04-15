package org.hisp.dhis.tasks.tracker.importer;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.hisp.dhis.actions.AuthenticatedApiActions;
import org.hisp.dhis.actions.RestApiActions;
import org.hisp.dhis.cache.EntitiesCache;
import org.hisp.dhis.cache.UserCredentials;
import org.hisp.dhis.models.Events;
import org.hisp.dhis.random.EventRandomizer;
import org.hisp.dhis.random.RandomizerContext;
import org.hisp.dhis.request.QueryParamsBuilder;
import org.hisp.dhis.response.dto.ApiResponse;
import org.hisp.dhis.response.dto.TrackerApiResponse;
import org.hisp.dhis.tasks.DhisAbstractTask;
import org.hisp.dhis.tracker.domain.Event;
import org.hisp.dhis.tracker.domain.mapper.EventMapperImpl;
import org.hisp.dhis.utils.DataRandomizer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * @author Gintare Vilkelyte <vilkelyte.gintare@gmail.com>
 */
public class AddTrackerEventsTask
    extends DhisAbstractTask
{
    private EventRandomizer eventRandomizer;

    private String endpoint = "/api/tracker";

    private List<String> blackListedTeis = new ArrayList<>();

    private Events events;

    private TrackerApiResponse response;

    private Logger logger = Logger.getLogger( this.getClass().getName() );

    public AddTrackerEventsTask( int weight, EntitiesCache entitiesCache )
    {
        this.weight = weight;
        this.entitiesCache = entitiesCache;
        eventRandomizer = new EventRandomizer();
    }

    public AddTrackerEventsTask( int weight, EntitiesCache entitiesCache, Events events, UserCredentials userCredentials )
    {
        this( weight, entitiesCache );
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

    private Events createRandomEvents()
    {

        List<Event> rndEvents = new ArrayList<>();
        for ( int i = 0; i < DataRandomizer.randomIntInRange( 5, 10 ); i++ )
        {
            Event randomEvent = null;
            try
            {
                randomEvent = new EventMapperImpl()
                    .from( eventRandomizer.create( entitiesCache, RandomizerContext.EMPTY_CONTEXT() ) );

            }
            catch ( Exception e )
            {
                logger.warning( "An error occurred while creating a random event: " + e.getMessage() );
            }

            if ( randomEvent != null && !blackListedTeis.contains( randomEvent.getTrackedEntity() ) )
            {
                rndEvents.add( randomEvent );
            }
        }

        return Events.builder().events( rndEvents ).build();
    }

    @Override
    public void execute()
        throws Exception
    {
        Events rndEvents = events != null ? events : createRandomEvents();

        RestApiActions apiActions = new AuthenticatedApiActions( this.endpoint, getUserCredentials() );

        response = (TrackerApiResponse) performTaskAndRecord( () -> new TrackerApiResponse(
            apiActions.post( rndEvents, new QueryParamsBuilder().addAll( "async=false", "identifier=events" ) ) ) );
    }

    public TrackerApiResponse executeAndGetResponse()
        throws Exception
    {
        this.execute();
        return response;
    }

    /**
     * A new PSI, must be linked to a Program Instance.
     * Since it is not possible to fetch Program Instances via API (and therefore cache them),
     * in order to avoid reusing invalid Tracked Entity Instances (which are the link between a PSI and a PI),
     * this method analyzed the response payload and checks if one of the ImportSummaries contains an error
     * ending for: 'is not enrolled in program". This error signal that there was Program Instance found by Tei + Program.
     * <p>
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


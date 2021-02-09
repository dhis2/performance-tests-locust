package org.hisp.dhis.tasks.tracker.importer;

import com.google.common.collect.Sets;
import org.hisp.dhis.actions.AuthenticatedApiActions;
import org.hisp.dhis.cache.UserCredentials;
import org.hisp.dhis.models.Events;
import org.hisp.dhis.request.QueryParamsBuilder;
import org.hisp.dhis.response.dto.ApiResponse;
import org.hisp.dhis.tasks.DhisAbstractTask;
import org.hisp.dhis.tracker.domain.DataValue;
import org.hisp.dhis.tracker.domain.Event;

import java.util.Arrays;

/**
 * @author Gintare Vilkelyte <vilkelyte.gintare@gmail.com>
 */
public class AddTrackerDataValueTask extends DhisAbstractTask
{
    private String endpoint = "/api/tracker";
    private String eventId;
    private DataValue dataValue;
    private String eventProgram;

    public AddTrackerDataValueTask(int weight, String eventId, DataValue dataValue, String program) {
        this.weight = weight;
        this.eventId = eventId;
        this.dataValue = dataValue;
        this.eventProgram = program;
    }

    public AddTrackerDataValueTask(int weight, String eventId, DataValue dataValue, String program, UserCredentials userCredentials ) {
        this(weight, eventId, dataValue, program);
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
    {
        Event event = Event.builder().event( eventId )
            .dataValues( Sets.newHashSet( this.dataValue ) )
            .build();

        Events events = Events.builder().events( Arrays.asList( event) ).build();

        ApiResponse response = new AuthenticatedApiActions( "/api/tracker", getUserCredentials() )
            .post( events, new QueryParamsBuilder().add( "async=false" ) );

        if ( response.statusCode() == 200 ) {
            recordSuccess( response.getRaw() );
        }

        else {
            recordFailure( response.getRaw() );
        }
    }
}
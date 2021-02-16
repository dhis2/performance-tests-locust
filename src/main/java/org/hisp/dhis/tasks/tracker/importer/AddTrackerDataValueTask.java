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
    private Event event;
    private DataValue dataValue;
    private String eventProgram;

    public AddTrackerDataValueTask(int weight, Event event, DataValue dataValue, String program) {
        this.weight = weight;
        this.event = event;
        this.dataValue = dataValue;
        this.eventProgram = program;
    }

    public AddTrackerDataValueTask(int weight, Event event, DataValue dataValue, String program, UserCredentials userCredentials ) {
        this(weight, event, dataValue, program);
        this.userCredentials = userCredentials;
    }


    @Override
    public String getName()
    {
        return endpoint + ": event data values";
    }

    @Override
    public String getType()
    {
        return "POST";
    }

    @Override
    public void execute()
    {
        event.setDataValues( Sets.newHashSet( this.dataValue ) );

        Events events = Events.builder().events( Arrays.asList( event) ).build();

        ApiResponse response = new AuthenticatedApiActions( "/api/tracker", getUserCredentials() )
            .post( events, new QueryParamsBuilder().addAll( "async=false", "identifier=eventdatavalues" ) );

        if ( response.statusCode() == 200 ) {
            recordSuccess( response.getRaw() );
        }

        else {
            recordFailure( response.getRaw() );
        }
    }
}
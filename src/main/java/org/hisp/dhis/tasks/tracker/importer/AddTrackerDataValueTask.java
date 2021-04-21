package org.hisp.dhis.tasks.tracker.importer;

import com.google.common.collect.Sets;
import com.google.gson.Gson;
import org.hisp.dhis.actions.AuthenticatedApiActions;
import org.hisp.dhis.cache.UserCredentials;
import org.hisp.dhis.models.Events;
import org.hisp.dhis.request.QueryParamsBuilder;
import org.hisp.dhis.tasks.DhisAbstractTask;
import org.hisp.dhis.tracker.domain.DataValue;
import org.hisp.dhis.tracker.domain.Event;

import java.util.Arrays;

/**
 * @author Gintare Vilkelyte <vilkelyte.gintare@gmail.com>
 */
public class AddTrackerDataValueTask
    extends DhisAbstractTask
{
    private String endpoint = "/api/tracker";

    private Event event;

    private DataValue dataValue;

    public AddTrackerDataValueTask( int weight, Event event, DataValue dataValue )
    {
        super( weight );
        this.event = new Gson().fromJson( new Gson().toJson( event ), Event.class );
        this.dataValue = dataValue;
    }

    public AddTrackerDataValueTask( int weight, Event event, DataValue dataValue, UserCredentials userCredentials )
    {
        this( weight, event, dataValue );
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
        throws Exception
    {
        event.setDataValues( Sets.newHashSet( this.dataValue ) );

        performTaskAndRecord( () -> new AuthenticatedApiActions( "/api/tracker", this.userCredentials )
            .post( Events.builder().events( Arrays.asList( event ) ).build(),
                new QueryParamsBuilder().addAll( "async=false", "identifier=eventdatavalues" ) ) );
    }
}
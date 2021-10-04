package org.hisp.dhis.tasks.tracker.events;

import com.google.gson.JsonObject;
import io.restassured.http.ContentType;
import org.hisp.dhis.actions.AuthenticatedApiActions;
import org.hisp.dhis.cache.UserCredentials;
import org.hisp.dhis.dxf2.events.event.DataValue;
import org.hisp.dhis.tasks.DhisAbstractTask;
import org.hisp.dhis.utils.JsonObjectBuilder;
import org.hisp.dhis.utils.JsonParserUtils;

/**
 * @author Gintare Vilkelyte <vilkelyte.gintare@gmail.com>
 */
public class AddDataValueTask
    extends DhisAbstractTask
{
    private String eventId;

    private DataValue dataValue;

    private String eventProgram;

    private String tei;

    public AddDataValueTask( int weight, String eventId, DataValue dataValue, String program, String tei )
    {
        super( weight );
        this.eventId = eventId;
        this.dataValue = dataValue;
        this.eventProgram = program;
        this.tei = tei;
    }

    public AddDataValueTask( int weight, String eventId, DataValue dataValue, String program, String tei, UserCredentials userCredentials )
    {
        this( weight, eventId, dataValue, program, tei );
        this.userCredentials = userCredentials;
    }

    @Override
    public String getName()
    {
        return "/api/events/id/de";
    }

    @Override
    public String getType()
    {
        return "PUT";
    }

    @Override
    public void execute()
        throws Exception
    {
        JsonObject payload = new JsonObjectBuilder()
            .addProperty( "program", eventProgram )
            .addProperty( "trackedEntityInstance", tei )
            .addProperty( "event", eventId )
            .addOrAppendToArray( "dataValues", JsonParserUtils.toJsonObject( dataValue ).getAsJsonObject() )
            .build();

        performTaskAndRecord( () -> new AuthenticatedApiActions( "/api/events", getUserCredentials() )
            .update( eventId + "/" + dataValue.getDataElement(), payload, ContentType.JSON.toString() ) );

    }
}

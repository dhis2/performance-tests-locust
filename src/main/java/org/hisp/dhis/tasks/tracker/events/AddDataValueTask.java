package org.hisp.dhis.tasks.tracker.events;

import com.github.myzhan.locust4j.AbstractTask;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.restassured.http.ContentType;
import org.hisp.dhis.actions.AuthenticatedApiActions;
import org.hisp.dhis.actions.RestApiActions;
import org.hisp.dhis.cache.UserCredentials;
import org.hisp.dhis.dxf2.events.event.DataValue;
import org.hisp.dhis.dxf2.events.event.Event;
import org.hisp.dhis.response.dto.ApiResponse;
import org.hisp.dhis.tasks.DhisAbstractTask;
import org.hisp.dhis.utils.JsonObjectBuilder;
import org.hisp.dhis.utils.JsonParserUtils;

/**
 * @author Gintare Vilkelyte <vilkelyte.gintare@gmail.com>
 */
public class AddDataValueTask extends DhisAbstractTask
{
    private String eventId;
    private DataValue dataValue;
    private String eventProgram;

    public AddDataValueTask(int weight, String eventId, DataValue dataValue, String program) {
        this.weight = weight;
        this.eventId = eventId;
        this.dataValue = dataValue;
        this.eventProgram = program;
    }

    public AddDataValueTask(int weight, String eventId, DataValue dataValue, String program, UserCredentials userCredentials ) {
        this(weight, eventId, dataValue, program);
        this.userCredentials = userCredentials;
    }

    @Override
    public String getName()
    {
        return "/events/id/de";
    }

    @Override
    public String getType()
    {
        return "PUT";
    }

    @Override
    public void execute()
    {
        JsonObject payload = new JsonObjectBuilder()
            .addProperty( "program", eventProgram )
            .addOrAppendToArray( "dataValues", JsonParserUtils.toJsonObject( dataValue ).getAsJsonObject() )
            .build();

        System.out.println("Ssending request");
        ApiResponse response = new AuthenticatedApiActions( "/api/events", getUserCredentials() )
            .update( eventId + "/" + dataValue.getDataElement(), payload, ContentType.JSON.toString() );

        if (response.statusCode() == 200) {
            recordSuccess( response.getRaw() );
        }

        else {
            recordFailure( response.getRaw() );
        }
    }
}

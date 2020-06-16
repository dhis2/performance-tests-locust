package org.hisp.dhis.tasks;

import org.hisp.dhis.actions.RestApiActions;
import org.hisp.dhis.cache.GeneratedTrackedEntityAttribute;
import org.hisp.dhis.response.dto.ApiResponse;

import java.util.UUID;

public class CreateTrackedEntityAttributeTask
    extends
    DhisAbstractTask
{
    private String id;

    private String endpoint = "/api/trackedEntityAttributes";

    public CreateTrackedEntityAttributeTask( int weight )
    {
        this.weight = weight;
    }

    public CreateTrackedEntityAttributeTask()
    {
        this.weight = 1;
    }

    public String getName()
    {
        return endpoint;
    }

    @Override
    public String getType()
    {
        return "POST";
    }

    public void execute()
    {
        long time = System.currentTimeMillis();

        GeneratedTrackedEntityAttribute generatedAttribute = new GeneratedTrackedEntityAttribute();
        generatedAttribute.setName( generatedAttribute.getName() + UUID.randomUUID() );
        generatedAttribute.setShortName( "" + UUID.randomUUID() );

        ApiResponse response = new RestApiActions( this.endpoint ).post( generatedAttribute );

        if ( response.statusCode() == 201 )
        {
            id = response.extractString( "response.uid" );
        }

        if ( response.statusCode() == 201 )
        {
            recordSuccess( response.getRaw() );
        }
        else
        {
            recordFailure( response.getRaw() );
        }
    }

    public String executeAndGetId()
    {
        this.execute();
        return id;
    }
}

package org.hisp.dhis.tasks;

import org.hisp.dhis.actions.RestApiActions;
import org.hisp.dhis.models.GeneratedTrackedEntityAttribute;
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
        throws Exception
    {
        GeneratedTrackedEntityAttribute generatedAttribute = new GeneratedTrackedEntityAttribute();
        generatedAttribute.setName( generatedAttribute.getName() + UUID.randomUUID() );
        generatedAttribute.setShortName( "" + UUID.randomUUID() );

        ApiResponse response = performTaskAndRecord( () -> new RestApiActions( this.endpoint ).post( generatedAttribute ), 201 );

        if ( response.statusCode() == 201 )
        {
            id = response.extractString( "response.uid" );
        }

    }

    public String executeAndGetId()
        throws Exception
    {
        this.execute();
        return id;
    }
}

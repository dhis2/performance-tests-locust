package org.hisp.dhis.tasks.tracker;

import org.hisp.dhis.actions.AuthenticatedApiActions;
import org.hisp.dhis.cache.UserCredentials;
import org.hisp.dhis.dxf2.events.trackedentity.Attribute;
import org.hisp.dhis.dxf2.events.trackedentity.TrackedEntityInstance;
import org.hisp.dhis.models.ReserveAttributeValuesException;
import org.hisp.dhis.request.QueryParamsBuilder;
import org.hisp.dhis.response.dto.ApiResponse;
import org.hisp.dhis.tasks.DhisAbstractTask;
import org.hisp.dhis.utils.Randomizer;

import java.util.List;

/**
 * @author Gintare Vilkelyte <vilkelyte.gintare@gmail.com>
 */
public class GenerateAndReserveTrackedEntityAttributeValuesTask
    extends DhisAbstractTask
{
    private String teiAttributeId;

    private int numberToReserve;

    private String endpoint = "/api/trackedEntityAttributes/id/generateAndReserve";

    public GenerateAndReserveTrackedEntityAttributeValuesTask( int weight, String trackedEntityAttributeId,
        UserCredentials userCredentials, int numberToReserve, Randomizer randomizer )
    {
        super( weight,randomizer );
        this.teiAttributeId = trackedEntityAttributeId;
        this.userCredentials = userCredentials;
        this.numberToReserve = numberToReserve;
    }

    @Override
    public String getName()
    {
        return endpoint ;
    }

    @Override
    public String getType()
    {
        return "GET";
    }

    @Override
    public void execute()
        throws Exception
    {
        this.executeAndGetResponse();
    }

    public void executeAndAddAttributes( List<TrackedEntityInstance> teis)
        throws Exception
    {
        ApiResponse apiResponse = executeAndGetResponse();

        if ( ! teis.isEmpty() )
        {
            List<String> values = apiResponse.extractList( "value" );

            if ( apiResponse.statusCode() != 200 || values == null || values.isEmpty()) {

                logWarningIfDebugEnabled( apiResponse.prettyPrint());
                throw new ReserveAttributeValuesException("Failed to generate attributes. Attributes weren't added to TEI.");
            }

            for ( int i = 0; i < teis.size(); i++ )
            {
                Attribute attribute = teis.get( i ).getAttributes().stream()
                    .filter( teiAtr -> teiAtr.getAttribute().equals( this.teiAttributeId ) )
                    .findFirst().orElse( null );

                if ( attribute == null )
                {
                    attribute = new Attribute();
                    attribute.setAttribute( this.teiAttributeId );
                }

                attribute.setValue( values.get( i ) );
            }
        }
    }
    public ApiResponse executeAndGetResponse()
        throws Exception
    {
        AuthenticatedApiActions apiActions = new AuthenticatedApiActions( "", userCredentials );

        return performTaskAndRecord( () -> {
            return apiActions.get( endpoint.replace( "id", teiAttributeId ), new QueryParamsBuilder().add( "numberToReserve",
                String.valueOf( numberToReserve ) ) );
        }, response -> ( response.statusCode() == 200) );
    }
}


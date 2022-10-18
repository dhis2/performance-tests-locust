package org.hisp.dhis.tasksets.aggregate;

import org.hisp.dhis.actions.AuthenticatedApiActions;
import org.hisp.dhis.cache.User;
import org.hisp.dhis.dxf2.datavalue.DataValue;
import org.hisp.dhis.random.DataValueRandomizer;
import org.hisp.dhis.request.QueryParamsBuilder;
import org.hisp.dhis.response.dto.ApiResponse;
import org.hisp.dhis.tasksets.DhisAbstractTaskSet;
import org.hisp.dhis.utils.Randomizer;

/**
 * @author Gintare Vilkelyte <vilkelyte.gintare@gmail.com>
 */
public class TrackerCapture_addDataValueTask
    extends DhisAbstractTaskSet
{
    private static final String NAME = "/api/dataValues";

    public TrackerCapture_addDataValueTask(final int weight )
    {
        super( NAME, weight );
    }

    @Override
    public String getName()
    {
        return NAME;
    }

    @Override
    public String getType()
    {
        return "POST";
    }

    @Override
    public void execute()
        throws InterruptedException
    {
        Randomizer rnd = getNextRandomizer( getName() );
        User user = getUser(rnd);
        AuthenticatedApiActions dataValueActions = new AuthenticatedApiActions( NAME, user.getUserCredentials() );

        DataValue aggregateDataValue = new DataValueRandomizer(rnd)
            .create( getRandomUserOrgUnit( user, rnd ), entitiesCache );

        ApiResponse response = dataValueActions.post( aggregateDataValue, new QueryParamsBuilder()
            .add( "de", aggregateDataValue.getDataElement() )
            .add( "pe", aggregateDataValue.getPeriod() )
            .add( "value=", aggregateDataValue.getValue() )
            //.add( "ds=" + aggregateDataValue.get())
            .add( "ou=", aggregateDataValue.getOrgUnit() ) );

        record( response.getRaw(), 201 );
        waitBetweenTasks();
    }
}

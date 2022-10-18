package org.hisp.dhis.tasksets.aggregate;

import org.hisp.dhis.actions.AuthenticatedApiActions;
import org.hisp.dhis.cache.User;
import org.hisp.dhis.dxf2.datavalueset.DataValueSet;
import org.hisp.dhis.random.DataValueRandomizer;
import org.hisp.dhis.response.dto.ApiResponse;
import org.hisp.dhis.tasksets.DhisAbstractTaskSet;
import org.hisp.dhis.utils.Randomizer;

/**
 * @author Gintare Vilkelyte <vilkelyte.gintare@gmail.com>
 */
public class Android_syncDataValuesTaskSet
    extends DhisAbstractTaskSet
{
    private static final String ENDPOINT = "/api/dataValueSets";

    public Android_syncDataValuesTaskSet( int weight )
    {
        super( ENDPOINT, weight );
    }

    @Override
    public String getName()
    {
        return ENDPOINT;
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
        AuthenticatedApiActions dataValueSetActions = new AuthenticatedApiActions(ENDPOINT, user.getUserCredentials() );

        DataValueSet aggregateDataValues = new DataValueRandomizer( rnd )
            .create( getRandomUserOrgUnit( user, rnd ), entitiesCache, 10, 50 );

        ApiResponse response = dataValueSetActions.post( aggregateDataValues );

        record( response.getRaw() );

        waitBetweenTasks();
    }
}

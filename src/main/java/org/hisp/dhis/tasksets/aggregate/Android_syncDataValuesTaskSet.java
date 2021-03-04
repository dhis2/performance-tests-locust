package org.hisp.dhis.tasksets.aggregate;

import org.hisp.dhis.actions.AuthenticatedApiActions;
import org.hisp.dhis.cache.EntitiesCache;
import org.hisp.dhis.cache.User;
import org.hisp.dhis.dxf2.datavalueset.DataValueSet;
import org.hisp.dhis.random.DataValueRandomizer;
import org.hisp.dhis.random.UserRandomizer;
import org.hisp.dhis.response.dto.ApiResponse;
import org.hisp.dhis.tasks.DhisAbstractTask;

/**
 * @author Gintare Vilkelyte <vilkelyte.gintare@gmail.com>
 */
public class Android_syncDataValuesTaskSet extends DhisAbstractTask
{
    private String endpoint = "/api/dataValueSets";

    public Android_syncDataValuesTaskSet(int weight, EntitiesCache entitiesCache ) {
        this.entitiesCache = entitiesCache;
        this.weight = weight;
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
        throws InterruptedException
    {
        User user = getUser();
        AuthenticatedApiActions dataValueSetActions  = new AuthenticatedApiActions( endpoint, user.getUserCredentials() );

        DataValueSet aggregateDataValues = new DataValueRandomizer().create( new UserRandomizer().getRandomUserOrgUnit( user ), entitiesCache, 10, 50 );

        ApiResponse response = dataValueSetActions.post( aggregateDataValues );

        record( response.getRaw() );

        waitBetweenTasks();
    }
}

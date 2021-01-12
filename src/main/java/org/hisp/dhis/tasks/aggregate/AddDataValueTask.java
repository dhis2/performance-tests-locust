package org.hisp.dhis.tasks.aggregate;

import org.hisp.dhis.RestAssured;
import org.hisp.dhis.actions.AuthenticatedApiActions;
import org.hisp.dhis.actions.RestApiActions;
import org.hisp.dhis.cache.AggregateDataValue;
import org.hisp.dhis.cache.EntitiesCache;
import org.hisp.dhis.cache.User;
import org.hisp.dhis.random.DataValueRandomizer;
import org.hisp.dhis.random.UserRandomizer;
import org.hisp.dhis.request.QueryParamsBuilder;
import org.hisp.dhis.response.dto.ApiResponse;
import org.hisp.dhis.tasks.DhisAbstractTask;

import static io.restassured.RestAssured.preemptive;

/**
 * @author Gintare Vilkelyte <vilkelyte.gintare@gmail.com>
 */
public class AddDataValueTask
    extends DhisAbstractTask
{
    private String endpoint = "/api/dataValues";
    private int weight;
    private EntitiesCache entitiesCache;

    public AddDataValueTask(final int weight, final EntitiesCache entitiesCache ) {
        this.weight = weight;
        this.entitiesCache = entitiesCache;
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
    {
        UserRandomizer userRandomizer = new UserRandomizer();
        User randomUser = userRandomizer.getRandomUser( entitiesCache );

        AuthenticatedApiActions dataValueActions  = new AuthenticatedApiActions( endpoint, randomUser.getUserCredentials() );

        System.out.println(String.format( "User %s, %s. Id: %s", randomUser.getUsername(), randomUser.getPassword(), randomUser.getId()));
        AggregateDataValue aggregateDataValue = new DataValueRandomizer().create( userRandomizer.getRandomUserOrgUnit( randomUser ), entitiesCache );

        ApiResponse response = dataValueActions.post(aggregateDataValue, new QueryParamsBuilder()
            .add( "de", aggregateDataValue.getDe())
            .add( "pe",  aggregateDataValue.getPe())
            .add( "value=", aggregateDataValue.getValue())
            .add( "ds=" + aggregateDataValue.getDs())
            .add( "ou=", aggregateDataValue.getOu() ));

        if (response.statusCode() == 201 ) {
            recordSuccess( response.getRaw() );
            return;
        }

        recordFailure( response.getRaw() );
    }
}

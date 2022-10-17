package org.hisp.dhis.tasks.tracker.enrollments;

import org.hisp.dhis.actions.AuthenticatedApiActions;
import org.hisp.dhis.cache.UserCredentials;
import org.hisp.dhis.dxf2.events.enrollment.Enrollment;
import org.hisp.dhis.random.EnrollmentRandomizer;
import org.hisp.dhis.random.RandomizerContext;
import org.hisp.dhis.response.dto.ApiResponse;
import org.hisp.dhis.tasks.DhisAbstractTask;
import org.hisp.dhis.utils.Randomizer;

/**
 * @author Gintare Vilkelyte <vilkelyte.gintare@gmail.com>
 */
public class AddEnrollmentTask
    extends DhisAbstractTask
{
    private String endpoint = "/api/enrollments";

    private RandomizerContext ctx = RandomizerContext.EMPTY_CONTEXT();

    private ApiResponse response;

    public AddEnrollmentTask( int weight, RandomizerContext context, UserCredentials userCredentials, Randomizer randomizer )
    {
        super( weight,randomizer );
        this.ctx = context;
        this.userCredentials = userCredentials;
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
        throws Exception
    {
        Randomizer rnd = getNextRandomizer( getName() );
        EnrollmentRandomizer enrollmentRandomizer = new EnrollmentRandomizer( rnd );

        Enrollment enrollment = enrollmentRandomizer.createWithoutEvents( entitiesCache, ctx );

        AuthenticatedApiActions authenticatedApiActions = new AuthenticatedApiActions( endpoint, getUserCredentials( rnd ) );

        response = performTaskAndRecord( () -> authenticatedApiActions.post( enrollment ), 201 );
    }

    public ApiResponse executeAndGetBody()
        throws Exception
    {
        this.execute();
        return response;
    }
}

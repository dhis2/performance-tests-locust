package org.hisp.dhis.tasks.tracker.enrollments;

import org.hisp.dhis.actions.AuthenticatedApiActions;
import org.hisp.dhis.cache.EntitiesCache;
import org.hisp.dhis.cache.UserCredentials;
import org.hisp.dhis.dxf2.events.enrollment.Enrollment;
import org.hisp.dhis.random.EnrollmentRandomizer;
import org.hisp.dhis.random.RandomizerContext;
import org.hisp.dhis.response.dto.ApiResponse;
import org.hisp.dhis.tasks.DhisAbstractTask;

/**
 * @author Gintare Vilkelyte <vilkelyte.gintare@gmail.com>
 */
public class AddEnrollmentTask extends DhisAbstractTask
{
    private String endpoint = "/api/enrollments";
    private RandomizerContext ctx = RandomizerContext.EMPTY_CONTEXT();
    private ApiResponse response;

    public AddEnrollmentTask(int weight, EntitiesCache entitiesCache ) {
        this.weight = weight;
        this.entitiesCache = entitiesCache;
    }

    public AddEnrollmentTask(int weight, EntitiesCache cache, RandomizerContext context, UserCredentials userCredentials ) {
        this(weight, cache);
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
    {
        EnrollmentRandomizer enrollmentRandomizer = new EnrollmentRandomizer();

        Enrollment enrollment = enrollmentRandomizer.createWithoutEvents( entitiesCache, ctx );

        AuthenticatedApiActions authenticatedApiActions = new AuthenticatedApiActions( endpoint, getUserCredentials() );

        response = authenticatedApiActions.post( enrollment );

        record(response.getRaw());
    }

    public ApiResponse executeAndGetBody() {
        this.execute();
        return response;
    }
}

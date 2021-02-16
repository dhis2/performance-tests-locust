package org.hisp.dhis.tasks.tracker.importer;

import com.google.common.collect.Lists;
import org.hisp.dhis.actions.AuthenticatedApiActions;
import org.hisp.dhis.cache.EntitiesCache;
import org.hisp.dhis.cache.UserCredentials;
import org.hisp.dhis.models.Enrollments;
import org.hisp.dhis.random.EnrollmentRandomizer;
import org.hisp.dhis.random.RandomizerContext;
import org.hisp.dhis.request.QueryParamsBuilder;
import org.hisp.dhis.response.dto.TrackerApiResponse;
import org.hisp.dhis.tasks.DhisAbstractTask;
import org.hisp.dhis.tracker.domain.Enrollment;
import org.hisp.dhis.tracker.domain.mapper.EnrollmentMapperImpl;
import org.hisp.dhis.utils.JsonParserUtils;

/**
 * @author Gintare Vilkelyte <vilkelyte.gintare@gmail.com>
 */
public class AddTrackerEnrollmentTask extends DhisAbstractTask
{
    private String endpoint = "/api/tracker";
    private RandomizerContext ctx = RandomizerContext.EMPTY_CONTEXT();
    private TrackerApiResponse response;

    public AddTrackerEnrollmentTask(int weight, EntitiesCache entitiesCache ) {
        this.weight = weight;
        this.entitiesCache = entitiesCache;
    }

    public AddTrackerEnrollmentTask(int weight, EntitiesCache cache, RandomizerContext context, UserCredentials userCredentials ) {
        this(weight, cache);
        this.ctx = context;
        this.userCredentials = userCredentials;
    }

    @Override
    public String getName()
    {
        return endpoint + ": enrollment";
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

        Enrollment enrollment = new EnrollmentMapperImpl().from( enrollmentRandomizer.createWithoutEvents( entitiesCache, ctx ));

        AuthenticatedApiActions authenticatedApiActions = new AuthenticatedApiActions( endpoint, getUserCredentials() );

        Enrollments enrollments = new Enrollments();
        enrollments.setEnrollments( Lists.newArrayList( enrollment ) );

        response = new TrackerApiResponse( authenticatedApiActions.post( enrollments, new QueryParamsBuilder().addAll( "async=false", "identifier=enrollment" ) ));

        if ( response.statusCode() == 200 ) {
            recordSuccess( response.getRaw() );
            return;
        }

        System.out.println( JsonParserUtils.toJsonObject( enrollments ).toString() );
        recordFailure( response.getRaw() );
    }

    public TrackerApiResponse executeAndGetBody() {
        this.execute();
        return response;
    }
}


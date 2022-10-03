package org.hisp.dhis.tasks.tracker.importer;

import com.google.common.collect.Lists;
import org.hisp.dhis.actions.AuthenticatedApiActions;
import org.hisp.dhis.cache.UserCredentials;
import org.hisp.dhis.models.Enrollments;
import org.hisp.dhis.random.EnrollmentRandomizer;
import org.hisp.dhis.random.RandomizerContext;
import org.hisp.dhis.request.QueryParamsBuilder;
import org.hisp.dhis.response.dto.TrackerApiResponse;
import org.hisp.dhis.tasks.DhisAbstractTask;
import org.hisp.dhis.tracker.domain.Enrollment;
import org.hisp.dhis.tracker.domain.mapper.EnrollmentMapperImpl;

/**
 * @author Gintare Vilkelyte <vilkelyte.gintare@gmail.com>
 */
public class AddTrackerEnrollmentTask
    extends DhisAbstractTask
{
    private String endpoint = "/api/tracker";

    private RandomizerContext ctx = RandomizerContext.EMPTY_CONTEXT();

    private TrackerApiResponse response;

    public AddTrackerEnrollmentTask( int weight, RandomizerContext context, UserCredentials userCredentials )
    {
        super( weight );
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
        throws Exception
    {
        EnrollmentRandomizer enrollmentRandomizer = new EnrollmentRandomizer();

        Enrollment enrollment = new EnrollmentMapperImpl().from( enrollmentRandomizer.createWithoutEvents( entitiesCache, ctx ) );
        Enrollments enrollments = new Enrollments();
        enrollments.setEnrollments( Lists.newArrayList( enrollment ) );

        response = new AddTrackerDataTask( 1, getUserCredentials(), enrollments, "enrollment"  ).executeAndGetBody();
    }

    public TrackerApiResponse executeAndGetBody()
        throws Exception
    {
        this.execute();
        return response;
    }
}


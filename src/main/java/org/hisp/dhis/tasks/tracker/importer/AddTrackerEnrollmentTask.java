package org.hisp.dhis.tasks.tracker.importer;

import com.google.common.collect.Lists;
import org.hisp.dhis.cache.UserCredentials;
import org.hisp.dhis.models.Enrollments;
import org.hisp.dhis.random.EnrollmentRandomizer;
import org.hisp.dhis.random.RandomizerContext;
import org.hisp.dhis.response.dto.TrackerApiResponse;
import org.hisp.dhis.tasks.DhisAbstractTask;
import org.hisp.dhis.tracker.domain.Enrollment;
import org.hisp.dhis.tracker.domain.mapper.EnrollmentMapperImpl;
import org.hisp.dhis.utils.Randomizer;

/**
 * @author Gintare Vilkelyte <vilkelyte.gintare@gmail.com>
 */
public class AddTrackerEnrollmentTask
    extends DhisAbstractTask
{
    private String endpoint = "/api/tracker";

    private RandomizerContext ctx;

    private TrackerApiResponse response;

    public AddTrackerEnrollmentTask( int weight, RandomizerContext context, UserCredentials userCredentials,
                                     Randomizer randomizer)
    {
        super( weight, randomizer );
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
        Randomizer rnd = getNextRandomizer( getName() );
        EnrollmentRandomizer enrollmentRandomizer = new EnrollmentRandomizer(rnd);

        Enrollment enrollment = new EnrollmentMapperImpl().from( enrollmentRandomizer.createWithoutEvents( entitiesCache, ctx ) );
        Enrollments enrollments = new Enrollments();
        enrollments.setEnrollments( Lists.newArrayList( enrollment ) );

        response = new AddTrackerDataTask( 1, getUserCredentials( rnd ), enrollments, "enrollment", rnd  ).executeAndGetBody();
    }

    public TrackerApiResponse executeAndGetBody()
        throws Exception
    {
        this.execute();
        return response;
    }
}


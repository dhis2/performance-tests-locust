package org.hisp.dhis.tasks.tracker;

import org.hisp.dhis.cache.UserCredentials;
import org.hisp.dhis.models.Enrollments;
import org.hisp.dhis.response.dto.TrackerApiResponse;
import org.hisp.dhis.tasks.DhisAbstractTask;
import org.hisp.dhis.utils.Randomizer;

/**
 * @author Gintare Vilkelyte <vilkelyte.gintare@gmail.com>
 */
public class AddTrackerEnrollmentTask
    extends DhisAbstractTask
{
    private String endpoint = "/api/tracker";

    private Enrollments enrollments;

    private TrackerApiResponse response;

    public AddTrackerEnrollmentTask( int weight, Enrollments enrollments, UserCredentials userCredentials,
                                     Randomizer randomizer)
    {
        super( weight, randomizer );
        this.enrollments = enrollments;
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

        response = new AddTrackerDataTask( 1, this.userCredentials, this.enrollments, "enrollment", rnd  ).executeAndGetBody();
    }

    public TrackerApiResponse executeAndGetBody()
        throws Exception
    {
        this.execute();
        return response;
    }
}


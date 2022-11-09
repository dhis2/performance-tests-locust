package org.hisp.dhis.tasks.tracker.importer;

import org.hisp.dhis.cache.User;
import org.hisp.dhis.cache.UserCredentials;
import org.hisp.dhis.models.TrackedEntities;
import org.hisp.dhis.random.RandomizerContext;
import org.hisp.dhis.response.dto.TrackerApiResponse;
import org.hisp.dhis.tasks.DhisAbstractTask;
import org.hisp.dhis.utils.Randomizer;

/**
 * @author Gintare Vilkelyte <vilkelyte.gintare@gmail.com>
 */
public class AddTrackerTeiTask
    extends DhisAbstractTask
{
    private String endpoint = "/api/tracker";

    private TrackedEntities trackedEntityInstanceBody;

    private TrackerApiResponse response;

    public AddTrackerTeiTask( int weight, TrackedEntities trackedEntityInstance,
        UserCredentials userCredentials, Randomizer randomizer )
    {
        super( weight, randomizer );
        trackedEntityInstanceBody = trackedEntityInstance;
        this.userCredentials = userCredentials;
    }

    public String getName()
    {
        return this.endpoint + ": teis";
    }

    @Override
    public String getType()
    {
        return "POST";
    }

    public void execute()
        throws Exception
    {
        Randomizer rnd = getNextRandomizer( getName() );

        response = new AddTrackerDataTask( 1, this.userCredentials, trackedEntityInstanceBody, "teis", rnd ).executeAndGetBody();
    }

    public TrackerApiResponse executeAndGetResponse()
        throws Exception
    {
        this.execute();
        return this.response;
    }
}

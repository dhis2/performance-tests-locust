package org.hisp.dhis.tasksets.tracker;

import org.hisp.dhis.cache.Program;
import org.hisp.dhis.cache.User;
import org.hisp.dhis.tasks.tracker.GetTeisTask;
import org.hisp.dhis.tasksets.DhisAbstractTaskSet;
import org.hisp.dhis.utils.Randomizer;

/**
 * @author Marc Pratllusa <marc@dhis2.org>
 */
public class TrackerCapture_getEntitiesOrderedByEnrolledAt
    extends DhisAbstractTaskSet
{
    private static final String NAME = "TrackerCapture: get entities ordered by enrollment date";

    public TrackerCapture_getEntitiesOrderedByEnrolledAt(int weight )
    {
        super( NAME, weight );
    }

    @Override
    public String getName()
    {
        return NAME;
    }

    @Override
    public String getType()
    {
        return "http";
    }

    @Override
    public void execute()
        throws Exception
    {
        Randomizer rnd = getNextRandomizer( getName() );
        Program program = rnd.randomElementFromList( this.entitiesCache.getTrackerPrograms() );
        User user = getUser(rnd);
        String ou = rnd.randomElementFromList( user.getOrganisationUnits() );

        String url = String
                .format( "?orgUnit=%s&program=%s&order=enrolledAt", ou, program.getId());

        new GetTeisTask( 1, url, user.getUserCredentials(), "get entities, order by enrollment date", rnd )
            .execute();

        waitBetweenTasks(rnd);
    }
}

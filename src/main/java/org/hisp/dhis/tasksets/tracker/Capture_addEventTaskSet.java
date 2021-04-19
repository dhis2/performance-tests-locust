package org.hisp.dhis.tasksets.tracker;

import com.google.common.collect.Lists;
import org.hisp.dhis.cache.Program;
import org.hisp.dhis.cache.User;
import org.hisp.dhis.dxf2.events.event.Event;
import org.hisp.dhis.random.EventRandomizer;
import org.hisp.dhis.random.RandomizerContext;
import org.hisp.dhis.random.UserRandomizer;
import org.hisp.dhis.tasks.DhisAbstractTask;
import org.hisp.dhis.tasks.tracker.events.AddEventsTask;
import org.hisp.dhis.tasks.tracker.events.QueryEventsTask;
import org.hisp.dhis.utils.DataRandomizer;

/**
 * @author Gintare Vilkelyte <vilkelyte.gintare@gmail.com>
 */
public class Capture_addEventTaskSet
    extends DhisAbstractTask
{
    public Capture_addEventTaskSet( int weight )
    {
        super( weight );
    }

    @Override
    public String getName()
    {
        return "Capture: add event";
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
        User user = new UserRandomizer().getRandomUser( entitiesCache );
        Program program = DataRandomizer.randomElementFromList( entitiesCache.getEventPrograms() );
        String ou = new UserRandomizer().getRandomUserOrProgramOrgUnit( user, program );

        new QueryEventsTask(
            String.format( "?page=1&pageSize=15&totalPages=true&order=eventDate:desc&program=%s&orgUnit=%s", program.getId(), ou ),
            user.getUserCredentials() ).execute();

        RandomizerContext context = new RandomizerContext();
        context.setProgram( program );
        context.setProgramStage( DataRandomizer.randomElementFromList( program.getProgramStages() ) );
        context.setOrgUnitUid( ou );

        Event event = new EventRandomizer().create( entitiesCache, context );

        new AddEventsTask( 1, Lists.newArrayList( event ), user.getUserCredentials() ).execute();

        waitBetweenTasks();
    }
}

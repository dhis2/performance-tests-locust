package org.hisp.dhis.tasksets.tracker;

import com.google.common.collect.Lists;
import org.hisp.dhis.cache.Program;
import org.hisp.dhis.cache.User;
import org.hisp.dhis.dxf2.events.event.Event;
import org.hisp.dhis.dxf2.events.event.Events;
import org.hisp.dhis.random.EventRandomizer;
import org.hisp.dhis.random.RandomizerContext;
import org.hisp.dhis.tasks.tracker.events.AddEventsTask;
import org.hisp.dhis.tasks.tracker.events.QueryEventsTask;
import org.hisp.dhis.tasks.tracker.importer.QueryTrackerEventsTask;
import org.hisp.dhis.tasksets.DhisAbstractTaskSet;
import org.hisp.dhis.utils.Randomizer;

/**
 * @author Gintare Vilkelyte <vilkelyte.gintare@gmail.com>
 */
public class Capture_addEventTaskSet
    extends DhisAbstractTaskSet
{
    private static final String NAME = "Capture: add event";

    public Capture_addEventTaskSet( int weight )
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
        User user = getRandomUser( rnd );
        Program program = rnd.randomElementFromList( entitiesCache.getEventPrograms() );
        String ou = getRandomUserOrProgramOrgUnit( user, program, rnd );

        new QueryEventsTask(
            String.format( "?page=1&pageSize=15&totalPages=true&order=eventDate:desc&program=%s&orgUnit=%s", program.getId(), ou ),
            user.getUserCredentials(), rnd ).execute();

        RandomizerContext context = new RandomizerContext();
        context.setProgram( program );
        context.setProgramStage( rnd.randomElementFromList( program.getProgramStages() ) );
        context.setOrgUnitUid( ou );

        Event event = new EventRandomizer(rnd).create( entitiesCache, context );

        Events events = new Events();
        events.setEvents(Lists.newArrayList( event ));
        new AddEventsTask( 1, events, user.getUserCredentials(), rnd ).execute();

        waitBetweenTasks(rnd);
    }
}

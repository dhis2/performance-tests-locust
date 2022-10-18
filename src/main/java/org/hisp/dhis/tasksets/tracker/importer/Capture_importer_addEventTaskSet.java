package org.hisp.dhis.tasksets.tracker.importer;

import org.hisp.dhis.cache.Program;
import org.hisp.dhis.cache.User;
import org.hisp.dhis.models.Events;
import org.hisp.dhis.random.EventRandomizer;
import org.hisp.dhis.random.RandomizerContext;
import org.hisp.dhis.tasks.tracker.importer.AddTrackerDataTask;
import org.hisp.dhis.tasks.tracker.importer.QueryTrackerEventsTask;
import org.hisp.dhis.tasksets.DhisAbstractTaskSet;
import org.hisp.dhis.tracker.domain.Event;
import org.hisp.dhis.tracker.domain.mapper.EventMapperImpl;
import org.hisp.dhis.utils.Randomizer;

/**
 * @author Gintare Vilkelyte <vilkelyte.gintare@gmail.com>
 */
public class Capture_importer_addEventTaskSet
    extends DhisAbstractTaskSet
{
    private static final String NAME = "Capture: add event (importer)";

    public Capture_importer_addEventTaskSet( int weight )
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
        User user = getRandomUser(rnd);
        Program program =rnd.randomElementFromList( entitiesCache.getEventPrograms() );
        String ou = getRandomUserOrProgramOrgUnit( user, program, rnd );

        new QueryTrackerEventsTask( String
            .format( "?page=1&pageSize=15&totalPages=true&order=occurredAt:desc&program=%s&orgUnit=%s", program.getId(), ou ),
            user.getUserCredentials(), rnd ).execute();

        RandomizerContext context = new RandomizerContext();
        context.setProgram( program );
        context.setProgramStage(rnd.randomElementFromList( program.getProgramStages() ) );
        context.setOrgUnitUid( ou );
        context.setSkipGenerationWhenAssignedByProgramRules( true );

        Event event = new EventMapperImpl().from( new EventRandomizer(rnd).create( entitiesCache, context ) );

        new AddTrackerDataTask( 1, user.getUserCredentials(), Events.builder().build().addEvent( event ),
            "events", rnd ).execute();

        waitBetweenTasks();
    }
}

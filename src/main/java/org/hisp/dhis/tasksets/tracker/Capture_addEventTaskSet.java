package org.hisp.dhis.tasksets.tracker;

import com.google.common.collect.Lists;
import org.hisp.dhis.cache.EntitiesCache;
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
public class Capture_addEventTaskSet extends DhisAbstractTask
{
    public Capture_addEventTaskSet(int weight, EntitiesCache entitiesCache ) {
        this.weight = weight;
        this.entitiesCache = entitiesCache;
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
    {
        User user = new UserRandomizer().getRandomUser( entitiesCache );
        String ou = new UserRandomizer().getRandomUserOrgUnit( user );
        Program program = DataRandomizer.randomElementFromList( entitiesCache.getEventPrograms() );

        new QueryEventsTask( String.format( "?page=1&pageSize=15&totalPages=true&order=eventDate:desc&program=%s&orgUnit=%s", program.getUid(), ou), user.getUserCredentials() ).execute();

        RandomizerContext context = new RandomizerContext();
        context.setProgram( program );
        context.setProgramStage( DataRandomizer.randomElementFromList( program.getStages() ) );
        context.setOrgUnitUid( ou );

        Event event = new EventRandomizer().create( entitiesCache, context );

        new AddEventsTask( 1, entitiesCache, Lists.newArrayList(event), user.getUserCredentials() ).execute();
    }
}

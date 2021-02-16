package org.hisp.dhis.tasksets.tracker.importer;

import com.google.common.collect.Lists;
import org.hisp.dhis.cache.EntitiesCache;
import org.hisp.dhis.cache.Program;
import org.hisp.dhis.cache.User;
import org.hisp.dhis.models.Events;
import org.hisp.dhis.random.EventRandomizer;
import org.hisp.dhis.random.RandomizerContext;
import org.hisp.dhis.random.UserRandomizer;
import org.hisp.dhis.tasks.DhisAbstractTask;
import org.hisp.dhis.tasks.tracker.importer.AddTrackerEventsTask;
import org.hisp.dhis.tasks.tracker.importer.QueryTrackerEventsTask;
import org.hisp.dhis.tracker.domain.Event;
import org.hisp.dhis.tracker.domain.mapper.EventMapperImpl;
import org.hisp.dhis.utils.DataRandomizer;

/**
 * @author Gintare Vilkelyte <vilkelyte.gintare@gmail.com>
 */
public class Capture_importer_addEventTaskSet
    extends DhisAbstractTask
{
    public Capture_importer_addEventTaskSet(int weight, EntitiesCache entitiesCache ) {
        this.weight = weight;
        this.entitiesCache = entitiesCache;
    }

    @Override
    public String getName()
    {
        return "Capture: add event (importer)";
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

        new QueryTrackerEventsTask( String.format( "?page=1&pageSize=15&totalPages=true&order=occurredAt:desc&program=%s&orgUnit=%s", program.getUid(), ou), user.getUserCredentials() ).execute();

        RandomizerContext context = new RandomizerContext();
        context.setProgram( program );
        context.setProgramStage( DataRandomizer.randomElementFromList( program.getStages() ));
        context.setOrgUnitUid( ou );

        Event event = new EventMapperImpl().from( new EventRandomizer().create( entitiesCache, context ));

        new AddTrackerEventsTask( 1, entitiesCache, Events.builder().build().addEvent(event), user.getUserCredentials() ).execute();
    }
}

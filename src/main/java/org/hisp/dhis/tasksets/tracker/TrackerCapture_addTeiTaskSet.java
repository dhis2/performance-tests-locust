package org.hisp.dhis.tasksets.tracker;

import com.google.common.collect.Lists;
import org.apache.commons.collections.set.ListOrderedSet;
import org.hisp.dhis.cache.Program;
import org.hisp.dhis.cache.User;
import org.hisp.dhis.cache.UserCredentials;
import org.hisp.dhis.dxf2.events.event.DataValue;
import org.hisp.dhis.dxf2.events.event.Event;
import org.hisp.dhis.dxf2.events.trackedentity.TrackedEntityInstance;
import org.hisp.dhis.dxf2.events.trackedentity.TrackedEntityInstances;
import org.hisp.dhis.random.EventDataValueRandomizer;
import org.hisp.dhis.random.EventRandomizer;
import org.hisp.dhis.random.RandomizerContext;
import org.hisp.dhis.random.TrackedEntityInstanceRandomizer;
import org.hisp.dhis.response.dto.ApiResponse;
import org.hisp.dhis.tasks.DhisDelayedTaskSet;
import org.hisp.dhis.tasks.tracker.GenerateTrackedEntityAttributeValueTask;
import org.hisp.dhis.tasks.tracker.enrollments.AddEnrollmentTask;
import org.hisp.dhis.tasks.tracker.events.AddDataValueTask;
import org.hisp.dhis.tasks.tracker.events.AddEventsTask;
import org.hisp.dhis.tasks.tracker.tei.AddTeiTask;
import org.hisp.dhis.tasks.tracker.tei.QueryFilterTeiTask;
import org.hisp.dhis.tasksets.DhisAbstractTaskSet;
import org.hisp.dhis.utils.Randomizer;

/**
 * @author Gintare Vilkelyte <vilkelyte.gintare@gmail.com>
 */
public class TrackerCapture_addTeiTaskSet
    extends DhisAbstractTaskSet
{
    private static final String NAME = "Tracker capture: add TEI";

    public TrackerCapture_addTeiTaskSet( int weight )
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
        // user ou
        User user = getRandomUser( rnd );
        Program program = rnd.randomElementFromList( entitiesCache.getTrackerPrograms() );
        String ou = getRandomUserOrProgramOrgUnit( user, program, rnd );

        RandomizerContext context = new RandomizerContext();
        context.setProgram( program );
        context.setOrgUnitUid( ou );

        TrackedEntityInstance tei = new TrackedEntityInstanceRandomizer( rnd ).createWithoutEnrollment( entitiesCache, context );
        // add tei
        generateAttributes( program, tei, user.getUserCredentials(), rnd );

        TrackedEntityInstances trackedEntityInstances = new TrackedEntityInstances();
        trackedEntityInstances.setTrackedEntityInstances( Lists.newArrayList( tei ) );

        long time = System.currentTimeMillis();

        new QueryFilterTeiTask( 1,
            String.format( "?program=%s&ou=%s&ouMode=SELECTED&pageSize=50&page=1&totalPages=false", program.getId(), ou ),
            user.getUserCredentials(), "TC load", rnd ).execute();

        ApiResponse body = new AddTeiTask( 1, trackedEntityInstances, user.getUserCredentials(), rnd)
            .executeAndGetResponse();

        context.setTeiId( body.extractUid() );

        if ( context.getTeiId() == null )
        {
            recordFailure( System.currentTimeMillis() - time, "TEI wasn't created" );
            return;
        }

        ApiResponse response = new AddEnrollmentTask( 1, context, user.getUserCredentials(), rnd ).executeAndGetBody();

        context.setEnrollmentId( response.extractUid() );

        if ( context.getEnrollmentId() == null )
        {
            recordFailure( System.currentTimeMillis() - time, "Enrollment wasn't created" );
            return;
        }

        context.setSkipTeiInEnrollment( false );
        context.setSkipTeiInEvent( false );

        Event event = new EventRandomizer(rnd).createWithoutDataValues( entitiesCache, context );

        response = new AddEventsTask( 1, Lists.newArrayList( event ), user.getUserCredentials(), rnd )
            .executeAndGetResponse();

        String eventId = response.extractUid();

        if ( eventId == null )
        {
            recordFailure( System.currentTimeMillis() - time, "Event wasn't created" );
            return;
        }

        ListOrderedSet dataValueSet = new EventDataValueRandomizer(rnd)
            .create( entitiesCache, context );

        DhisDelayedTaskSet taskSet = new DhisDelayedTaskSet( 2 );
        dataValueSet.forEach( dv -> {
            taskSet.addTask(
                new AddDataValueTask( 1, eventId, (DataValue) dv, program.getId(), tei.getTrackedEntityInstance(),
                    user.getUserCredentials(), rnd )
            );
        } );

        taskSet.execute();
        waitBetweenTasks();

    }

    private void generateAttributes( Program program, TrackedEntityInstance tei, UserCredentials userCredentials, Randomizer rnd )
    {

        program.getGeneratedAttributes()
            .forEach( att -> {
                ApiResponse response = new GenerateTrackedEntityAttributeValueTask( 1, att.getTrackedEntityAttribute(),
                    userCredentials, rnd ).executeAndGetResponse();

                String value = response.extractString( "value" );

                tei.getAttributes().stream()
                    .filter( teiAtr -> teiAtr.getAttribute().equals( att.getTrackedEntityAttribute() ) )
                    .findFirst().ifPresent(at -> at.setValue( value ) );
            } );
    }
}

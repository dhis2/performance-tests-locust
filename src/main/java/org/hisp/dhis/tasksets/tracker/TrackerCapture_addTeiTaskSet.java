package org.hisp.dhis.tasksets.tracker;

import com.google.common.collect.Lists;
import org.apache.commons.collections.set.ListOrderedSet;
import org.hisp.dhis.cache.EntitiesCache;
import org.hisp.dhis.cache.Program;
import org.hisp.dhis.cache.User;
import org.hisp.dhis.cache.UserCredentials;
import org.hisp.dhis.dxf2.events.event.DataValue;
import org.hisp.dhis.dxf2.events.event.Event;
import org.hisp.dhis.dxf2.events.trackedentity.Attribute;
import org.hisp.dhis.dxf2.events.trackedentity.TrackedEntityInstance;
import org.hisp.dhis.dxf2.events.trackedentity.TrackedEntityInstances;
import org.hisp.dhis.random.EventRandomizer;
import org.hisp.dhis.random.RandomizerContext;
import org.hisp.dhis.random.TrackedEntityInstanceRandomizer;
import org.hisp.dhis.random.UserRandomizer;
import org.hisp.dhis.response.dto.ApiResponse;
import org.hisp.dhis.tasks.DhisAbstractTask;
import org.hisp.dhis.tasks.DhisDelayedTaskSet;
import org.hisp.dhis.tasks.tracker.GenerateTrackedEntityAttributeValueTask;
import org.hisp.dhis.tasks.tracker.enrollments.AddEnrollmentTask;
import org.hisp.dhis.tasks.tracker.events.AddDataValueTask;
import org.hisp.dhis.tasks.tracker.events.AddEventsTask;
import org.hisp.dhis.tasks.tracker.tei.AddTeiTask;
import org.hisp.dhis.tasks.tracker.tei.QueryFilterTeiTask;
import org.hisp.dhis.utils.DataRandomizer;

import java.util.concurrent.TimeUnit;

/**
 * @author Gintare Vilkelyte <vilkelyte.gintare@gmail.com>
 */
public class TrackerCapture_addTeiTaskSet extends DhisAbstractTask
{
    public TrackerCapture_addTeiTaskSet(int weight, EntitiesCache entitiesCache ) {
        this.weight = weight;
        this.entitiesCache = entitiesCache;
    }

    @Override
    public String getName()
    {
        return "Tracker capture: add TEI";
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

        // user ou
        User user = new UserRandomizer().getRandomUser( entitiesCache );
        String ou = new UserRandomizer().getRandomUserOrgUnit( user );
        Program program = DataRandomizer.randomElementFromList( entitiesCache.getTrackerPrograms() );

        RandomizerContext context = new RandomizerContext();
        context.setProgram( program );
        context.setOrgUnitUid( ou );

        TrackedEntityInstance tei = new TrackedEntityInstanceRandomizer().createWithoutEnrollment( entitiesCache, context );
        // add tei
        generateAttributes( program, tei, user.getUserCredentials());

        TrackedEntityInstances trackedEntityInstances = new TrackedEntityInstances();
        trackedEntityInstances.setTrackedEntityInstances( Lists.newArrayList( tei ) );

        long time = System.currentTimeMillis();

        new QueryFilterTeiTask( 1, String.format( "?program=%s&ou=%s&ouMode=SELECTED&pageSize=50&page=1&totalPages=false", program.getUid(), ou), user.getUserCredentials() );

        ApiResponse body = new AddTeiTask( 1, entitiesCache, trackedEntityInstances, user.getUserCredentials() ).executeAndGetResponse();

        context.setTeiId( body.extractUid() );

        if (context.getTeiId() == null) {
            recordFailure( System.currentTimeMillis() - time, "TEI wasn't created" );
            return;
        }

        ApiResponse response = new AddEnrollmentTask( 1, entitiesCache, context, user.getUserCredentials() ).executeAndGetBody();

        context.setEnrollmentId( response.extractUid() );

        if (context.getEnrollmentId() == null ){
            recordFailure( System.currentTimeMillis() - time, "Enrollment wasn't created" );
            return;
        }

        context.setSkipTeiInEnrollment( false );
        context.setSkipTeiInEvent( false );

        Event event = new EventRandomizer().createWithoutDataValues( entitiesCache, context );

        response = new AddEventsTask(1, entitiesCache, Lists.newArrayList(event) , user.getUserCredentials() ).executeAndGetResponse();

        String eventId = response.extractUid();

        if (eventId == null) {
            recordFailure( System.currentTimeMillis() - time, "Event wasn't created" );
            return;
        }

        int dataValuesToCreate = context.getProgramStage().getDataElements().size();

        ListOrderedSet dataValueSet = new EventRandomizer().createDataValues( context.getProgramStage(), dataValuesToCreate / 4, dataValuesToCreate);

        DhisDelayedTaskSet taskSet = new DhisDelayedTaskSet(5, TimeUnit.SECONDS);
        dataValueSet.forEach( dv -> {
            taskSet.addTask(
                new AddDataValueTask( 1, eventId, (DataValue) dv, program.getUid(), user.getUserCredentials() )
            );
        } );

        taskSet.execute();
        recordSuccess( System .currentTimeMillis() - time, 0);

        waitBetweenTasks();

    }

    private void generateAttributes(Program program, TrackedEntityInstance tei, UserCredentials userCredentials ) {

        program.getAttributes().stream().filter( p ->
            p.isGenerated()
        ).forEach( att -> {
            ApiResponse response = new GenerateTrackedEntityAttributeValueTask( 1, att.getTrackedEntityAttribute(),userCredentials ).executeAndGetResponse();

            String value = response.extractString( "value" );

            Attribute attribute = tei.getAttributes().stream().filter( teiAtr -> teiAtr.getAttribute().equals( att.getTrackedEntityAttribute()))
                .findFirst().orElse( null);

            attribute.setValue( value );
        } );
    }
}

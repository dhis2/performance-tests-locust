package org.hisp.dhis.tasksets.tracker.importer;

import com.google.common.collect.Lists;
import org.apache.commons.collections.set.ListOrderedSet;
import org.hisp.dhis.cache.EntitiesCache;
import org.hisp.dhis.cache.Program;
import org.hisp.dhis.cache.User;
import org.hisp.dhis.cache.UserCredentials;
import org.hisp.dhis.models.Events;
import org.hisp.dhis.models.TrackedEntities;
import org.hisp.dhis.random.EventRandomizer;
import org.hisp.dhis.random.RandomizerContext;
import org.hisp.dhis.random.TrackedEntityInstanceRandomizer;
import org.hisp.dhis.random.UserRandomizer;
import org.hisp.dhis.response.dto.ApiResponse;
import org.hisp.dhis.response.dto.TrackerApiResponse;
import org.hisp.dhis.tasks.DhisAbstractTask;
import org.hisp.dhis.tasks.DhisDelayedTaskSet;
import org.hisp.dhis.tasks.tracker.GenerateTrackedEntityAttributeValueTask;
import org.hisp.dhis.tasks.tracker.importer.*;
import org.hisp.dhis.tracker.domain.Attribute;
import org.hisp.dhis.tracker.domain.Event;
import org.hisp.dhis.tracker.domain.TrackedEntity;
import org.hisp.dhis.tracker.domain.mapper.DataValueMapperImpl;
import org.hisp.dhis.tracker.domain.mapper.EventMapperImpl;
import org.hisp.dhis.tracker.domain.mapper.TrackedEntityMapperImpl;
import org.hisp.dhis.utils.DataRandomizer;

import java.util.concurrent.TimeUnit;

/**
 * @author Gintare Vilkelyte <vilkelyte.gintare@gmail.com>
 */
public class TrackerCapture_importer_addTeiTaskSet
    extends DhisAbstractTask
{
    public TrackerCapture_importer_addTeiTaskSet(int weight, EntitiesCache entitiesCache ) {
        this.weight = weight;
        this.entitiesCache = entitiesCache;
    }

    @Override
    public String getName()
    {
        return "Tracker capture: add TEI (importer)";
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

        TrackedEntity tei = new TrackedEntityMapperImpl().from( new TrackedEntityInstanceRandomizer().createWithoutEnrollment( entitiesCache, context ));
        // add tei
        generateAttributes( program, tei, user.getUserCredentials());

        TrackedEntities trackedEntityInstances = TrackedEntities.builder().trackedEntities( Lists.newArrayList( tei ) ).build();

        long time = System.currentTimeMillis();

        new QueryTrackerTeisTask( 1, String.format( "?program=%s&orgUnit=%s&ouMode=SELECTED&pageSize=50&page=1&totalPages=false", program.getUid(), ou), user.getUserCredentials() ).execute();

        TrackerApiResponse body = new AddTrackerTeiTask( 1, entitiesCache, trackedEntityInstances, user.getUserCredentials() ).executeAndGetResponse();

        if ( body.extractImportedTeis().size() == 0) {
            recordFailure( System.currentTimeMillis() - time, "TEI wasn't created" );
            return;
        }

        context.setTeiId( body.extractImportedTeis().get( 0 ) );

        //EnrollmentRandomizer randomizer = new EnrollmentRandomizer();
        //Enrollment enrollment = randomizer.createWithoutEvents( entitiesCache, context );

        TrackerApiResponse response = new AddTrackerEnrollmentTask( 1, entitiesCache, context, user.getUserCredentials() ).executeAndGetBody();

        if ( response.extractImportedEnrollments() == null || response.extractImportedEnrollments().size() == 0) {
            recordFailure( System.currentTimeMillis() - time, "Enrollment wasn't created" );
            return;
        }

        context.setEnrollmentId( response.extractImportedEnrollments().get( 0 ) );
        context.setSkipTeiInEnrollment( false );
        context.setSkipTeiInEvent( false );

        Event event = new EventMapperImpl().from( new EventRandomizer().createWithoutDataValues( entitiesCache, context ));
        response = new AddTrackerEventsTask(1, entitiesCache, Events.builder().build().addEvent( event ) , user.getUserCredentials() ).executeAndGetResponse();

        if (response.extractImportedEvents( ) == null || response.extractImportedEvents().size() == 0)
        {
            recordFailure( System.currentTimeMillis() - time, "Event wasn't created" );
            return;
        }

        String eventId = response.extractImportedEvents().get( 0 );
        event.setEvent( eventId );
        int dataValuesToCreate = context.getProgramStage().getDataElements().size();

        ListOrderedSet dataValueSet = new EventRandomizer().createDataValues( context.getProgramStage(), dataValuesToCreate / 4, dataValuesToCreate);

        DhisDelayedTaskSet taskSet = new DhisDelayedTaskSet(5, TimeUnit.SECONDS);

        dataValueSet.forEach( dv -> {
            taskSet.addTask(
                new AddTrackerDataValueTask( 1, event, new DataValueMapperImpl().from(
                    (org.hisp.dhis.dxf2.events.event.DataValue) dv ), program.getUid(), user.getUserCredentials() )
            );
        } );

        taskSet.execute();
        recordSuccess( System .currentTimeMillis() - time, 0);

        waitBetweenTasks();
    }

    private void generateAttributes(Program program, TrackedEntity tei, UserCredentials userCredentials ) {

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


package org.hisp.dhis.tasksets.tracker.importer;

import com.google.common.collect.Lists;
import org.apache.commons.collections.set.ListOrderedSet;
import org.hisp.dhis.cache.Program;
import org.hisp.dhis.cache.User;
import org.hisp.dhis.cache.UserCredentials;
import org.hisp.dhis.dxf2.events.event.DataValue;
import org.hisp.dhis.models.Enrollments;
import org.hisp.dhis.models.Events;
import org.hisp.dhis.models.TrackedEntities;
import org.hisp.dhis.random.EnrollmentRandomizer;
import org.hisp.dhis.random.EventDataValueRandomizer;
import org.hisp.dhis.random.EventRandomizer;
import org.hisp.dhis.random.RandomizerContext;
import org.hisp.dhis.random.TrackedEntityInstanceRandomizer;
import org.hisp.dhis.response.dto.ApiResponse;
import org.hisp.dhis.response.dto.TrackerApiResponse;
import org.hisp.dhis.tasks.DhisDelayedTaskSet;
import org.hisp.dhis.tasks.tracker.oldapi.GenerateTrackedEntityAttributeValueTask;
import org.hisp.dhis.tasks.tracker.AddTrackerDataValueTask;
import org.hisp.dhis.tasks.tracker.AddTrackerEnrollmentTask;
import org.hisp.dhis.tasks.tracker.AddTrackerEventsTask;
import org.hisp.dhis.tasks.tracker.AddTrackerTeiTask;
import org.hisp.dhis.tasks.tracker.QueryTrackerTeisTask;
import org.hisp.dhis.tasksets.DhisAbstractTaskSet;
import org.hisp.dhis.tracker.domain.Attribute;
import org.hisp.dhis.tracker.domain.Enrollment;
import org.hisp.dhis.tracker.domain.Event;
import org.hisp.dhis.tracker.domain.TrackedEntity;
import org.hisp.dhis.tracker.domain.mapper.DataValueMapperImpl;
import org.hisp.dhis.tracker.domain.mapper.EnrollmentMapperImpl;
import org.hisp.dhis.tracker.domain.mapper.EventMapperImpl;
import org.hisp.dhis.tracker.domain.mapper.TrackedEntityMapperImpl;
import org.hisp.dhis.utils.Randomizer;

/**
 * @author Gintare Vilkelyte <vilkelyte.gintare@gmail.com>
 */
public class TrackerCapture_importer_addTeiTaskSet
    extends DhisAbstractTaskSet
{
    private static final String NAME = "Tracker capture: add TEI (importer)";

    public TrackerCapture_importer_addTeiTaskSet( int weight )
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
        User user = getRandomUser(rnd);
        Program program =rnd.randomElementFromList( entitiesCache.getTrackerPrograms() );
        String ou = getRandomUserOrProgramOrgUnit( user, program, rnd );

        RandomizerContext context = new RandomizerContext();
        context.setProgram( program );
        context.setOrgUnitUid( ou );

        TrackedEntity tei = new TrackedEntityMapperImpl()
            .from( new TrackedEntityInstanceRandomizer(rnd).createWithoutEnrollment( entitiesCache, context ) );
        // add tei
        generateAttributes( program, tei, user.getUserCredentials(), rnd );

        TrackedEntities trackedEntityInstances = TrackedEntities.builder().trackedEntities( Lists.newArrayList( tei ) ).build();

        new QueryTrackerTeisTask( 1,
            String.format( "?program=%s&orgUnit=%s&ouMode=SELECTED&pageSize=50&page=1&totalPages=false", program.getId(), ou ),
            user.getUserCredentials(), rnd ).execute();

        TrackerApiResponse body = new AddTrackerTeiTask( 1, trackedEntityInstances, user.getUserCredentials(), rnd )
            .executeAndGetResponse();

        if ( body.extractString( "status" ).equalsIgnoreCase( "ERROR" ) || body.extractImportedTeis().isEmpty() )
        {
            logWarningIfDebugEnabled( "TEI wasn't created" );
            return;
        }

        context.setTeiId( body.extractImportedTeis().get( 0 ) );

        Enrollment enrollment = new EnrollmentMapperImpl().from(
                new EnrollmentRandomizer(rnd).createWithoutEvents( entitiesCache, context ) );
        Enrollments enrollments = Enrollments.builder().enrollments(Lists.newArrayList( enrollment )).build();

        TrackerApiResponse response = new AddTrackerEnrollmentTask( 1, enrollments, user.getUserCredentials(), rnd )
            .executeAndGetBody();

        if ( response.extractImportedEnrollments() == null || response.extractImportedEnrollments().isEmpty() )
        {
            logWarningIfDebugEnabled( "Enrollment wasn't created" );
            return;
        }

        context.setEnrollmentId( response.extractImportedEnrollments().get( 0 ) );
        context.setSkipTeiInEnrollment( false );
        context.setSkipTeiInEvent( false );
        context.setSkipGenerationWhenAssignedByProgramRules( true );

        Event event = new EventMapperImpl().from( new EventRandomizer(rnd).createWithoutDataValues( entitiesCache, context ) );
        Events events = Events.builder().events(Lists.newArrayList(event)).build();
        response = new AddTrackerEventsTask( 1, events, user.getUserCredentials(), rnd ).executeAndGetResponse();

        if ( response.extractImportedEvents() == null || response.extractImportedEvents().isEmpty() )
        {
            logWarningIfDebugEnabled( "Event wasn't created" );
            return;
        }

        String eventId = response.extractImportedEvents().get( 0 );
        event.setEvent( eventId );

        ListOrderedSet dataValueSet = new EventDataValueRandomizer(rnd).create( entitiesCache, context );
        DhisDelayedTaskSet taskSet = new DhisDelayedTaskSet( 3 );

        dataValueSet.forEach( dv -> {
            taskSet.addTask( new AddTrackerDataValueTask( 1, event, new DataValueMapperImpl().from(
                (DataValue) dv ), user.getUserCredentials(), rnd ) );
        } );

        taskSet.execute();
        //recordSuccess( System.currentTimeMillis() - time, 0 );

        waitBetweenTasks(rnd);
    }

    private void generateAttributes( Program program, TrackedEntity tei, UserCredentials userCredentials, Randomizer rnd )
    {
        program.getGeneratedAttributes().forEach( att -> {
            ApiResponse response = new GenerateTrackedEntityAttributeValueTask( 1, att.getTrackedEntityAttribute(),
                userCredentials, rnd ).executeAndGetResponse();

            String value = response.extractString( "value" );

            Attribute attribute = tei.getAttributes().stream()
                .filter( teiAtr -> teiAtr.getAttribute().equals( att.getTrackedEntityAttribute() ) )
                .findFirst().orElse( null );

            attribute.setValue( value );
        } );
    }
}


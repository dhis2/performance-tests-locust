package org.hisp.dhis.tasksets.tracker.importer;

import org.hisp.dhis.cache.Program;
import org.hisp.dhis.cache.TrackedEntityAttribute;
import org.hisp.dhis.cache.User;
import org.hisp.dhis.cache.UserCredentials;
import org.hisp.dhis.dxf2.events.trackedentity.Relationship;
import org.hisp.dhis.dxf2.events.trackedentity.TrackedEntityInstance;
import org.hisp.dhis.dxf2.events.trackedentity.TrackedEntityInstances;
import org.hisp.dhis.models.TrackedEntities;
import org.hisp.dhis.models.TrackerPayload;
import org.hisp.dhis.random.RandomizerContext;
import org.hisp.dhis.random.RelationshipRandomizer;
import org.hisp.dhis.random.TrackedEntityInstanceRandomizer;
import org.hisp.dhis.tasks.tracker.GenerateAndReserveTrackedEntityAttributeValuesTask;
import org.hisp.dhis.tasks.tracker.importer.AddTrackerDataTask;
import org.hisp.dhis.tasksets.DhisAbstractTaskSet;
import org.hisp.dhis.tracker.domain.TrackedEntity;
import org.hisp.dhis.tracker.domain.mapper.RelationshipMapperImpl;
import org.hisp.dhis.tracker.domain.mapper.TrackedEntityMapperImpl;
import org.hisp.dhis.utils.Randomizer;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


/**
 * @author Gintare Vilkelyte <vilkelyte.gintare@gmail.com>
 */
public class Android_importer_syncTeisTaskSet
    extends DhisAbstractTaskSet
{
    private static final String ENDPOINT = "/api/tracker";

    private static final String NAME = String.format( "Android: sync teis (%s)", ENDPOINT);

    private static final boolean withRelationship = false;

    private boolean skipRuleEngine = false;

    public Android_importer_syncTeisTaskSet( int weight )
    {
        super( NAME, weight );
    }

    public Android_importer_syncTeisTaskSet( int weight, boolean skipRuleEngine )
    {
        this( weight );
        this.skipRuleEngine = skipRuleEngine;
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
        Program program =rnd.randomElementFromList( entitiesCache.getTrackerPrograms() );

        RandomizerContext context = new RandomizerContext();
        context.setSkipGenerationWhenAssignedByProgramRules( true );
        context.setOrgUnitUid( getRandomUserOrProgramOrgUnit( user, program, rnd ) );
        context.setProgram( program );
        context.setGenerateIds( true );
        context.setProgramAttributesInEnrollment( true );
        TrackedEntityInstances instances = new TrackedEntityInstanceRandomizer(rnd).create( entitiesCache, context, 20, 20 );

        generateAttributes( context.getProgram(), instances.getTrackedEntityInstances(), user.getUserCredentials(), rnd );

        TrackedEntities trackedEntities = TrackedEntities.builder()
            .trackedEntities( instances.getTrackedEntityInstances().stream().
                map( p -> new TrackedEntityMapperImpl().from( p ) ).collect( Collectors.toList() ) )
            .build();

        TrackerPayload payload = TrackerPayload.builder()
            .trackedEntities( trackedEntities.getTrackedEntities() )
            .build();

        if ( withRelationship )
        {
            List<org.hisp.dhis.tracker.domain.Relationship> relationships = generateRelationships(
                trackedEntities.getTrackedEntities(), rnd );
            payload.setRelationships( relationships );
        }

        new AddTrackerDataTask( 1, user.getUserCredentials(), payload, "FULL", rnd, "skipRuleEngine=" + this.skipRuleEngine, "skipSideEffects=" + this.skipRuleEngine ).execute();

        waitBetweenTasks();
    }

    private void generateAttributes( Program program, List<TrackedEntityInstance> teis, UserCredentials userCredentials, Randomizer rnd )
        throws Exception
    {
        for ( TrackedEntityAttribute att : program.getGeneratedAttributes() )
        {
            new GenerateAndReserveTrackedEntityAttributeValuesTask( 1, att.getTrackedEntityAttribute(),
                userCredentials, teis.size(), rnd ).executeAndAddAttributes( teis );
        }
    }

    private List<org.hisp.dhis.tracker.domain.Relationship> generateRelationships( List<TrackedEntity> teis, Randomizer rnd )
    {
        List<org.hisp.dhis.tracker.domain.Relationship> relationships = new ArrayList<>();
        for ( int i = 0; i < teis.size() - 1; i++ )
        {
            Relationship relationship = new RelationshipRandomizer(rnd)
                .create( entitiesCache, teis.get( i ).getTrackedEntity(), teis.get( i + 1 ).getTrackedEntity() );

            relationships.add( new RelationshipMapperImpl().from( relationship ) );
        }

        return relationships;
    }
}

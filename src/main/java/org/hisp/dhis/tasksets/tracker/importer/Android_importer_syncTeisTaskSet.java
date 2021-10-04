package org.hisp.dhis.tasksets.tracker.importer;

import org.hisp.dhis.cache.Program;
import org.hisp.dhis.cache.TrackedEntityAttribute;
import org.hisp.dhis.cache.User;
import org.hisp.dhis.cache.UserCredentials;
import org.hisp.dhis.dxf2.events.trackedentity.Attribute;
import org.hisp.dhis.dxf2.events.trackedentity.Relationship;
import org.hisp.dhis.dxf2.events.trackedentity.TrackedEntityInstance;
import org.hisp.dhis.dxf2.events.trackedentity.TrackedEntityInstances;
import org.hisp.dhis.models.Relationships;
import org.hisp.dhis.models.TrackedEntities;
import org.hisp.dhis.models.TrackerPayload;
import org.hisp.dhis.random.RandomizerContext;
import org.hisp.dhis.random.RelationshipRandomizer;
import org.hisp.dhis.random.TrackedEntityInstanceRandomizer;
import org.hisp.dhis.random.UserRandomizer;
import org.hisp.dhis.response.dto.ApiResponse;
import org.hisp.dhis.tasks.DhisAbstractTask;
import org.hisp.dhis.tasks.GenerateIdTask;
import org.hisp.dhis.tasks.tracker.GenerateAndReserveTrackedEntityAttributeValuesTask;
import org.hisp.dhis.tasks.tracker.importer.AddTrackerDataTask;
import org.hisp.dhis.tracker.domain.TrackedEntity;
import org.hisp.dhis.tracker.domain.mapper.RelationshipMapperImpl;
import org.hisp.dhis.tracker.domain.mapper.TrackedEntityMapperImpl;
import org.hisp.dhis.utils.DataRandomizer;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Gintare Vilkelyte <vilkelyte.gintare@gmail.com>
 */
public class Android_importer_syncTeisTaskSet
    extends DhisAbstractTask
{
    private String endpoint = "/api/tracker";

    public Android_importer_syncTeisTaskSet( int weight )
    {
        super( weight );
    }

    @Override
    public String getName()
    {
        return String.format( "Android: sync teis (%s)", endpoint );
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
        Program program = DataRandomizer.randomElementFromList( entitiesCache.getTrackerPrograms() );

        RandomizerContext context = new RandomizerContext();
        context.setOrgUnitUid( new UserRandomizer().getRandomUserOrProgramOrgUnit( user, program ) );

        TrackedEntityInstances instances = new TrackedEntityInstanceRandomizer().create( entitiesCache, context, 20, 20 );

        generateAttributes( context.getProgram(), instances.getTrackedEntityInstances(), user.getUserCredentials() );

        TrackedEntities trackedEntities = TrackedEntities.builder()
            .trackedEntities( instances.getTrackedEntityInstances().stream().
                map( p -> new TrackedEntityMapperImpl().from( p ) ).collect( Collectors.toList() ) )
            .build();

        setIds( trackedEntities.getTrackedEntities(), user.getUserCredentials() );
        List<org.hisp.dhis.tracker.domain.Relationship> relationships = generateRelationships( trackedEntities.getTrackedEntities() );

        TrackerPayload payload = TrackerPayload.builder().trackedEntities( trackedEntities.getTrackedEntities() )
            .relationships( relationships )
            .build();


        new AddTrackerDataTask( 1, user.getUserCredentials(), payload, "FULL" ).execute();

        waitBetweenTasks();
    }

    private void generateAttributes( Program program, List<TrackedEntityInstance> teis, UserCredentials userCredentials )
    {
        program.getAttributes().stream().filter( TrackedEntityAttribute::isGenerated
        ).forEach( att -> {
            ApiResponse response = new GenerateAndReserveTrackedEntityAttributeValuesTask( 1, att.getTrackedEntityAttribute(),
                userCredentials, teis.size() ).executeAndGetResponse();
            List<String> values = response.extractList( "value" );

            for ( int i = 0; i < teis.size(); i++ )
            {
                Attribute attribute = teis.get( i ).getAttributes().stream()
                    .filter( teiAtr -> teiAtr.getAttribute().equals( att.getTrackedEntityAttribute() ) )
                    .findFirst().orElse( null );

                attribute.setValue( values.get( i ) );
            }
        } );
    }

    private void setIds( List<TrackedEntity> trackedEntities, UserCredentials credentials)
        throws Exception
    {
        List<String> ids = new GenerateIdTask( credentials, trackedEntities.size()).executeAndGetResponse();

        for ( int i = 0; i < trackedEntities.size(); i++ )
        {
            trackedEntities.get( i ).setTrackedEntity( ids.get( i ) );
        }

    }

    private List<org.hisp.dhis.tracker.domain.Relationship> generateRelationships( List<TrackedEntity> teis )  {
        List<org.hisp.dhis.tracker.domain.Relationship> relationships = new ArrayList<>();
        for ( int i = 0; i < teis.size() - 1; i++ )
        {
            Relationship relationship = new RelationshipRandomizer().create( entitiesCache, teis.get( i ).getTrackedEntity(), teis.get( i + 1 ).getTrackedEntity() );

            relationships.add( new RelationshipMapperImpl().from( relationship ));
        }

        return relationships;
    }
}

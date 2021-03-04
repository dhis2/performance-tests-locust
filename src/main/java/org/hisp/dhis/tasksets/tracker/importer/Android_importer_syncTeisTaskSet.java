package org.hisp.dhis.tasksets.tracker.importer;

import org.hisp.dhis.actions.AuthenticatedApiActions;
import org.hisp.dhis.cache.EntitiesCache;
import org.hisp.dhis.cache.Program;
import org.hisp.dhis.cache.User;
import org.hisp.dhis.cache.UserCredentials;
import org.hisp.dhis.dxf2.events.trackedentity.Attribute;
import org.hisp.dhis.dxf2.events.trackedentity.TrackedEntityInstance;
import org.hisp.dhis.dxf2.events.trackedentity.TrackedEntityInstances;
import org.hisp.dhis.models.TrackedEntities;
import org.hisp.dhis.random.RandomizerContext;
import org.hisp.dhis.random.TrackedEntityInstanceRandomizer;
import org.hisp.dhis.random.UserRandomizer;
import org.hisp.dhis.request.QueryParamsBuilder;
import org.hisp.dhis.response.dto.ApiResponse;
import org.hisp.dhis.response.dto.TrackerApiResponse;
import org.hisp.dhis.tasks.DhisAbstractTask;
import org.hisp.dhis.tasks.tracker.GenerateAndReserveTrackedEntityAttributeValuesTask;
import org.hisp.dhis.tracker.domain.mapper.TrackedEntityMapperImpl;
import org.hisp.dhis.utils.DataRandomizer;
import org.hisp.dhis.utils.JsonParserUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Gintare Vilkelyte <vilkelyte.gintare@gmail.com>
 */
public class Android_importer_syncTeisTaskSet
    extends DhisAbstractTask
{
    private String endpoint = "/api/tracker";

    public Android_importer_syncTeisTaskSet( int weight, EntitiesCache cache )
    {
        this.weight = weight;
        this.entitiesCache = cache;
    }

    @Override
    public String getName()
    {
        return "Android: sync teis (importer)";
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

        RandomizerContext context = new RandomizerContext();
        context.setOrgUnitUid( DataRandomizer.randomElementFromList( user.getOrganisationUnits() ) );

        TrackedEntityInstances instances = new TrackedEntityInstanceRandomizer().create( entitiesCache, context, 3, 10 );

        generateAttributes( context.getProgram(), instances.getTrackedEntityInstances(), user.getUserCredentials() );

        TrackedEntities trackedEntities = TrackedEntities.builder()
            .trackedEntities( instances.getTrackedEntityInstances().stream().
                map( p -> {
                    return new TrackedEntityMapperImpl().from( p );
                } ).collect( Collectors.toList() ) )
            .build();


        performTaskAndRecord(() -> new TrackerApiResponse(  new AuthenticatedApiActions( endpoint, user.getUserCredentials() ).post( trackedEntities, new QueryParamsBuilder().addAll( "async=false", "identifier=full" ) )) );

        waitBetweenTasks();
    }

    private void generateAttributes( Program program, List<TrackedEntityInstance> teis, UserCredentials userCredentials )
    {
        program.getAttributes().stream().filter( p ->
            p.isGenerated()
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
}

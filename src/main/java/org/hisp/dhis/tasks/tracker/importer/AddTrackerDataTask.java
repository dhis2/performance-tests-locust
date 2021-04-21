package org.hisp.dhis.tasks.tracker.importer;

import org.hisp.dhis.actions.AuthenticatedApiActions;
import org.hisp.dhis.cache.Program;
import org.hisp.dhis.cache.UserCredentials;
import org.hisp.dhis.dxf2.events.trackedentity.Attribute;
import org.hisp.dhis.dxf2.events.trackedentity.TrackedEntityInstance;
import org.hisp.dhis.dxf2.events.trackedentity.TrackedEntityInstances;
import org.hisp.dhis.models.TrackedEntities;
import org.hisp.dhis.random.RandomizerContext;
import org.hisp.dhis.random.TrackedEntityInstanceRandomizer;
import org.hisp.dhis.request.QueryParamsBuilder;
import org.hisp.dhis.response.dto.ApiResponse;
import org.hisp.dhis.tasks.DhisAbstractTask;
import org.hisp.dhis.tasks.tracker.GenerateAndReserveTrackedEntityAttributeValuesTask;
import org.hisp.dhis.tracker.domain.mapper.TrackedEntityMapperImpl;
import org.hisp.dhis.utils.DataRandomizer;

import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * @author Gintare Vilkelyte <vilkelyte.gintare@gmail.com>
 */
public class AddTrackerDataTask
    extends DhisAbstractTask
{
    private String endpoint = "/api/tracker";

    private Object payload;

    private boolean async = false;

    private Logger logger = Logger.getLogger( this.getClass().getName() );

    private String identifier = "";

    public AddTrackerDataTask( int weight )
    {
        super( weight );
    }

    public AddTrackerDataTask( int weight, UserCredentials userCredentials, Object payload,
        boolean isAsync, String identifier )
    {
        this( weight );
        this.userCredentials = userCredentials;
        this.payload = payload;
        this.async = isAsync;
        this.identifier = identifier;
    }

    @Override
    public String getName()
    {
        return endpoint + ": " + this.identifier;
    }

    @Override
    public String getType()
    {
        return "POST";
    }

    @Override
    public void execute()
        throws Exception
    {
        user = getUser();
        AuthenticatedApiActions trackerActions = new AuthenticatedApiActions( endpoint, user.getUserCredentials() );

        if ( payload == null )
        {
            RandomizerContext context = new RandomizerContext();
            context.setOrgUnitUid( DataRandomizer.randomElementFromList( user.getOrganisationUnits() ) );
            TrackedEntityInstances instances = new TrackedEntityInstanceRandomizer().create( entitiesCache, context, 2, 3 );

            generateAttributes( context.getProgram(), instances.getTrackedEntityInstances(), user.getUserCredentials() );

            payload = TrackedEntities.builder()
                .trackedEntities( instances.getTrackedEntityInstances().stream().
                    map( p -> new TrackedEntityMapperImpl().from( p ) ).collect( Collectors.toList() ) )
                .build();
        }

        performTaskAndRecord( () -> {
            ApiResponse response = trackerActions
                .post( payload, new QueryParamsBuilder().addAll( "async=" + this.async, "identifier=" + identifier ) );

            if ( this.async )
            {
                String jobId = response.extractString( "response.id" );

                this.waitUntilJobIsCompleted( jobId, user.getUserCredentials() );

                response = trackerActions.get( String.format( "/jobs/%s/report?reportMode=%s", jobId, "FULL" ) );
            }

            return response;
        }, response -> response.extractString( "status" ).equalsIgnoreCase( "ERROR" ) ? false : true );
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

    public ApiResponse waitUntilJobIsCompleted( String jobId, UserCredentials credentials )
        throws Exception
    {
        ApiResponse response = null;
        boolean completed = false;
        int attempts = 600;

        while ( !completed && attempts > 0 )
        {
            Thread.currentThread().sleep( 100 );

            response = new GetImportJobTask( 1, credentials, jobId ).executeAndGetResponse();
            completed = response.extractList( "completed" ).contains( true );
            attempts--;
        }

        if ( attempts == 0 )
        {
            logger.info( "MAX ATTEMPTS REACHED" );
        }

        return response;
    }
}

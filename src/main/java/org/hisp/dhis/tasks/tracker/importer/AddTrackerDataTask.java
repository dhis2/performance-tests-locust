package org.hisp.dhis.tasks.tracker.importer;

import com.google.gson.GsonBuilder;
import org.hisp.dhis.actions.AuthenticatedApiActions;
import org.hisp.dhis.cache.Program;
import org.hisp.dhis.cache.TrackedEntityAttribute;
import org.hisp.dhis.cache.User;
import org.hisp.dhis.cache.UserCredentials;
import org.hisp.dhis.dxf2.events.trackedentity.TrackedEntityInstance;
import org.hisp.dhis.dxf2.events.trackedentity.TrackedEntityInstances;
import org.hisp.dhis.models.TrackedEntities;
import org.hisp.dhis.random.RandomizerContext;
import org.hisp.dhis.random.TrackedEntityInstanceRandomizer;
import org.hisp.dhis.request.QueryParamsBuilder;
import org.hisp.dhis.response.dto.ApiResponse;
import org.hisp.dhis.response.dto.TrackerApiResponse;
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

    private QueryParamsBuilder builder = new QueryParamsBuilder();

    private Object payload;

    private boolean async = super.cfg.useAsyncTrackerImporter();

    private TrackerApiResponse response;

    private Logger logger = Logger.getLogger( this.getClass().getName() );

    private String identifier = "";

    public AddTrackerDataTask( int weight, UserCredentials userCredentials, Object payload,
        String identifier )
    {
        super( weight );
        this.userCredentials = userCredentials;
        this.payload = payload;
        this.identifier = identifier;
    }

    public AddTrackerDataTask( int weight, UserCredentials userCredentials, Object payload,
        String identifier, String... params )
    {
        this( weight, userCredentials, payload, identifier );
        this.builder.addAll( params );
    }

    @Override
    public String getName()
    {
        return endpoint + builder.build() + ": " + this.identifier;
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
        User user = getUser();
        AuthenticatedApiActions trackerActions = new AuthenticatedApiActions( endpoint, getUserCredentials() );

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

        builder.addAll( "async=" + this.async, "identifier=" + identifier );
        response = (TrackerApiResponse) performTaskAndRecord( () -> {
            ApiResponse response = trackerActions
                .post( payload, builder );

            if ( this.async )
            {
                String jobId = response.extractString( "response.id" );

                this.waitUntilJobIsCompleted( jobId, user.getUserCredentials() );

                response = trackerActions.get( String.format( "/jobs/%s/report?reportMode=%s", jobId, "FULL" ) );
            }

            if ( !response.extractString( "status" ).equalsIgnoreCase( "OK" ) && cfg.debug() )
            {
                logWarningIfDebugEnabled( new GsonBuilder().setPrettyPrinting().create().toJson( response.getBody() ) );
            }

            return new TrackerApiResponse( response );
        }, response -> response.extractString( "status" ).equalsIgnoreCase( "OK" ) );
    }

    public TrackerApiResponse executeAndGetBody()
        throws Exception
    {
        this.execute();
        return response;
    }

    private void generateAttributes( Program program, List<TrackedEntityInstance> teis, UserCredentials userCredentials )
        throws Exception
    {
        for ( TrackedEntityAttribute att : program.getGeneratedAttributes() )
        {
            new GenerateAndReserveTrackedEntityAttributeValuesTask( 1, att.getTrackedEntityAttribute(),
                userCredentials, teis.size() ).executeAndAddAttributes( teis );

        }
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

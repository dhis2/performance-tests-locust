package org.hisp.dhis.tasks.tracker;

import com.google.gson.GsonBuilder;
import org.hisp.dhis.actions.AuthenticatedApiActions;
import org.hisp.dhis.cache.UserCredentials;
import org.hisp.dhis.request.QueryParamsBuilder;
import org.hisp.dhis.response.dto.ApiResponse;
import org.hisp.dhis.response.dto.TrackerApiResponse;
import org.hisp.dhis.tasks.DhisAbstractTask;
import org.hisp.dhis.utils.Randomizer;

import java.util.logging.Logger;

import static org.hisp.dhis.conf.ConfigFactory.cfg;

/**
 * @author Gintare Vilkelyte <vilkelyte.gintare@gmail.com>
 */
public class AddTrackerDataTask
    extends DhisAbstractTask
{
    private String endpoint = "/api/tracker";

    private QueryParamsBuilder builder = new QueryParamsBuilder();

    private Object payload;

    private boolean async = cfg.useAsyncTrackerImporter();

    private TrackerApiResponse response;

    private Logger logger = Logger.getLogger( this.getClass().getName() );

    private String identifier = "";

    public AddTrackerDataTask( int weight, UserCredentials userCredentials, Object payload,
        String identifier, Randomizer randomizer )
    {
        super( weight, randomizer );
        this.userCredentials = userCredentials;
        this.payload = payload;
        this.identifier = identifier;
    }

    public AddTrackerDataTask(int weight, UserCredentials userCredentials, Object payload,
                              String identifier, Randomizer randomizer, String... params )
    {
        this( weight, userCredentials, payload, identifier, randomizer );
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
        Randomizer rnd = getNextRandomizer( getName() );
        AuthenticatedApiActions trackerActions = new AuthenticatedApiActions( endpoint, this.userCredentials );

        builder.addAll( "async=" + this.async, "identifier=" + identifier );
        response = (TrackerApiResponse) performTaskAndRecord( () -> {
            ApiResponse response = trackerActions
                .post( payload, builder );

            if ( this.async )
            {
                String jobId = response.extractString( "response.id" );

                this.waitUntilJobIsCompleted( jobId, this.userCredentials, rnd);

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

    public ApiResponse waitUntilJobIsCompleted(String jobId, UserCredentials credentials, Randomizer rnd)
        throws Exception
    {
        ApiResponse response = null;
        boolean completed = false;
        int attempts = 600;

        while ( !completed && attempts > 0 )
        {
            Thread.currentThread().sleep( 100 );

            response = new GetImportJobTask( 1, credentials, jobId, rnd ).executeAndGetResponse();
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

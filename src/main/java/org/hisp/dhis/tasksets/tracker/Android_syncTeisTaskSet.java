package org.hisp.dhis.tasksets.tracker;

import jdk.nashorn.internal.parser.JSONParser;
import org.hisp.dhis.actions.AuthenticatedApiActions;
import org.hisp.dhis.cache.Program;
import org.hisp.dhis.cache.TrackedEntityAttribute;
import org.hisp.dhis.cache.User;
import org.hisp.dhis.cache.UserCredentials;
import org.hisp.dhis.dxf2.events.trackedentity.Attribute;
import org.hisp.dhis.dxf2.events.trackedentity.TrackedEntityInstance;
import org.hisp.dhis.dxf2.events.trackedentity.TrackedEntityInstances;
import org.hisp.dhis.random.RandomizerContext;
import org.hisp.dhis.random.TrackedEntityInstanceRandomizer;
import org.hisp.dhis.random.UserRandomizer;
import org.hisp.dhis.request.QueryParamsBuilder;
import org.hisp.dhis.response.dto.ApiResponse;
import org.hisp.dhis.tasks.DhisAbstractTask;
import org.hisp.dhis.tasks.tracker.GenerateAndReserveTrackedEntityAttributeValuesTask;
import org.hisp.dhis.utils.DataRandomizer;
import org.hisp.dhis.utils.JsonParserUtils;

import java.util.List;

/**
 * @author Gintare Vilkelyte <vilkelyte.gintare@gmail.com>
 */
public class Android_syncTeisTaskSet
    extends DhisAbstractTask
{
    private int minPayload = 10;

    private int maxPayload = 10;

    public Android_syncTeisTaskSet( int weight )
    {
        super( weight );
    }

    public Android_syncTeisTaskSet( int weight, int payloadSize )
    {
        this( weight );
        this.minPayload = payloadSize;
        this.maxPayload = payloadSize;
    }

    @Override
    public String getName()
    {
        return String.format( "Android: sync teis (%d, %d)", minPayload, maxPayload );
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
        String ou = new UserRandomizer().getRandomUserOrProgramOrgUnit( user, program );

        RandomizerContext context = new RandomizerContext();
        context.setProgram( program );
        context.setOrgUnitUid( ou );

        TrackedEntityInstances teis = new TrackedEntityInstanceRandomizer()
            .create( this.entitiesCache, context, minPayload, maxPayload );

        generateAttributes( program, teis.getTrackedEntityInstances(), user.getUserCredentials() );

        performTaskAndRecord( () -> {
            ApiResponse response = new AuthenticatedApiActions( "/api/trackedEntityInstances", user.getUserCredentials() )
                .post( teis, new QueryParamsBuilder().add( "strategy=SYNC" ) );

            return response;
        }, response -> response.extractString( "status" ).equalsIgnoreCase( "ERROR" ) ? false : true );

        waitBetweenTasks();
    }

    private void generateAttributes( Program program, List<TrackedEntityInstance> teis, UserCredentials userCredentials )
        throws Exception
    {
        for ( TrackedEntityAttribute att : program.getAttributes() )
        {
            if ( att.isGenerated() )
            {
                new GenerateAndReserveTrackedEntityAttributeValuesTask( 1, att.getTrackedEntityAttribute(),
                    userCredentials, teis.size() ).executeAndAddAttributes( teis );
            }
        }
    }
}

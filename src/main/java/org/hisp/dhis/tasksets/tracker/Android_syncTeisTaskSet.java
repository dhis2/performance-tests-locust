package org.hisp.dhis.tasksets.tracker;

import org.hisp.dhis.actions.AuthenticatedApiActions;
import org.hisp.dhis.cache.Program;
import org.hisp.dhis.cache.TrackedEntityAttribute;
import org.hisp.dhis.cache.User;
import org.hisp.dhis.cache.UserCredentials;
import org.hisp.dhis.dxf2.events.trackedentity.TrackedEntityInstance;
import org.hisp.dhis.dxf2.events.trackedentity.TrackedEntityInstances;
import org.hisp.dhis.random.RandomizerContext;
import org.hisp.dhis.random.TrackedEntityInstanceRandomizer;
import org.hisp.dhis.request.QueryParamsBuilder;
import org.hisp.dhis.response.dto.ApiResponse;
import org.hisp.dhis.tasks.tracker.GenerateAndReserveTrackedEntityAttributeValuesTask;
import org.hisp.dhis.tasksets.DhisAbstractTaskSet;
import org.hisp.dhis.utils.Randomizer;

import java.util.List;

/**
 * @author Gintare Vilkelyte <vilkelyte.gintare@gmail.com>
 */
public class Android_syncTeisTaskSet
    extends DhisAbstractTaskSet
{
    private int minPayload = 10;

    private int maxPayload = 10;

    public Android_syncTeisTaskSet( int weight )
    {
        super( "Android: sync teis", weight );
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
        Randomizer rnd = getNextRandomizer( getName() );
        User user = getRandomUser( rnd );
        Program program = rnd.randomElementFromList( entitiesCache.getTrackerPrograms() );
        String ou = getRandomUserOrProgramOrgUnit( user, program, rnd );

        RandomizerContext context = new RandomizerContext();
        context.setProgram( program );
        context.setOrgUnitUid( ou );

        TrackedEntityInstances teis = new TrackedEntityInstanceRandomizer(rnd)
            .create( this.entitiesCache, context, minPayload, maxPayload );

        generateAttributes( program, teis.getTrackedEntityInstances(), user.getUserCredentials(), rnd );

        performTaskAndRecord( () -> {
            ApiResponse response = new AuthenticatedApiActions( "/api/trackedEntityInstances", user.getUserCredentials() )
                .post( teis, new QueryParamsBuilder().add( "strategy=SYNC" ) );

            return response;
        }, response -> response.extractString( "status" ).equalsIgnoreCase( "ERROR" ) ? false : true );

        waitBetweenTasks();
    }

    private void generateAttributes(Program program, List<TrackedEntityInstance> teis, UserCredentials userCredentials, Randomizer rnd)
        throws Exception
    {
        for ( TrackedEntityAttribute att : program.getGeneratedAttributes() )
        {
            new GenerateAndReserveTrackedEntityAttributeValuesTask( 1, att.getTrackedEntityAttribute(),
                userCredentials, teis.size(), rnd ).executeAndAddAttributes( teis );
        }
    }

}

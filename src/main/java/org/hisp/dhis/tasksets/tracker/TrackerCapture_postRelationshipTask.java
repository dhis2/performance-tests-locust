package org.hisp.dhis.tasksets.tracker;

import org.apache.commons.collections4.CollectionUtils;
import org.hisp.dhis.actions.AuthenticatedApiActions;
import org.hisp.dhis.cache.Program;
import org.hisp.dhis.cache.RelationshipType;
import org.hisp.dhis.cache.TeiType;
import org.hisp.dhis.cache.TrackedEntityAttribute;
import org.hisp.dhis.cache.User;
import org.hisp.dhis.cache.UserCredentials;
import org.hisp.dhis.dxf2.events.trackedentity.TrackedEntityInstances;
import org.hisp.dhis.models.ReserveAttributeValuesException;
import org.hisp.dhis.random.RandomizerContext;
import org.hisp.dhis.random.RelationshipRandomizer;
import org.hisp.dhis.random.TrackedEntityInstanceRandomizer;
import org.hisp.dhis.response.dto.ApiResponse;
import org.hisp.dhis.tasks.tracker.GenerateAndReserveTrackedEntityAttributeValuesTask;
import org.hisp.dhis.tasks.tracker.tei.AddTeiTask;
import org.hisp.dhis.tasksets.DhisAbstractTaskSet;
import org.hisp.dhis.utils.Randomizer;

import java.util.List;
import java.util.Objects;

/**
 * @author Gintare Vilkelyte <vilkelyte.gintare@gmail.com>
 */
public class TrackerCapture_postRelationshipTask
    extends DhisAbstractTaskSet
{

    private static final String NAME = "/api/relationships";

    public TrackerCapture_postRelationshipTask( int weight )
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
        return "POST";
    }

    @Override
    public void execute()
        throws Exception
    {
        Randomizer rnd = getNextRandomizer( getName() );
        User user = getUser(rnd);

        RandomizerContext context = new RandomizerContext();
        context.setOrgUnitUid( rnd.randomElementFromList( user.getOrganisationUnits() ) );
        context.setProgram( rnd.randomElementFromList( entitiesCache.getTrackerPrograms() ) );

        AuthenticatedApiActions actions = new AuthenticatedApiActions( NAME, user.getUserCredentials() );

        RelationshipRandomizer relationshipRandomizer = new RelationshipRandomizer(rnd);
        RelationshipType relationshipType = relationshipRandomizer.randomTeitoTeiRelationshipType(entitiesCache);

        List<String> uids;
        try {
            uids = createTeis( context, relationshipType.getFromConstraint().getTrackedEntityType(), rnd );
        } catch ( ReserveAttributeValuesException e ){
            return;
        }

        if ( CollectionUtils.isEmpty(uids) || uids.stream().anyMatch(Objects::isNull))
        {
            return;
        }

        org.hisp.dhis.dxf2.events.trackedentity.Relationship relationship = relationshipRandomizer
            .create( uids.get( 0 ), uids.get( 1 ), relationshipType );

        performTaskAndRecord( () -> actions.post( relationship ) );

        waitBetweenTasks(rnd);
    }

    private List<String> createTeis(RandomizerContext context, TeiType trackedEntityType, Randomizer rnd)
        throws Exception
    {
        context.setTeiType(trackedEntityType.getId());
        TrackedEntityInstances trackedEntityInstances = new TrackedEntityInstanceRandomizer(rnd).create(
            entitiesCache, context, 2
        );

        generateAttributes( context.getProgram(), trackedEntityInstances, user.getUserCredentials(), rnd);

        ApiResponse body = new AddTeiTask( 1, trackedEntityInstances, user.getUserCredentials(), rnd )
            .executeAndGetResponse();

        return body.extractUids();
    }

    private void generateAttributes(Program program, TrackedEntityInstances teis, UserCredentials userCredentials, Randomizer rnd)
        throws Exception
    {
        for ( TrackedEntityAttribute att : program.getGeneratedAttributes() )
        {
            new GenerateAndReserveTrackedEntityAttributeValuesTask( 1, att.getTrackedEntityAttribute(),
                userCredentials, teis.getTrackedEntityInstances().size(), rnd )
                .executeAndAddAttributes( teis.getTrackedEntityInstances() );

        }

    }

}

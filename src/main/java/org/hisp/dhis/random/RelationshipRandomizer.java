package org.hisp.dhis.random;

import org.hisp.dhis.cache.EntitiesCache;
import org.hisp.dhis.cache.Program;
import org.hisp.dhis.cache.RelationshipConstraint;
import org.hisp.dhis.cache.RelationshipType;
import org.hisp.dhis.cache.Tei;
import org.hisp.dhis.dxf2.events.trackedentity.Relationship;
import org.hisp.dhis.dxf2.events.trackedentity.RelationshipItem;
import org.hisp.dhis.dxf2.events.trackedentity.TrackedEntityInstance;
import org.hisp.dhis.utils.Randomizer;

import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author Gintare Vilkelyte <vilkelyte.gintare@gmail.com>
 */
public class RelationshipRandomizer
    extends AbstractTrackerEntityRandomizer<Relationship>
{
    public RelationshipRandomizer(Randomizer rnd) {
        super( rnd );
    }

    @Override
    public Relationship create( EntitiesCache cache, RandomizerContext randomizerContext )
    {
        RelationshipType relationshipType = rnd.randomElementFromList( cache.getRelationshipTypes().stream().filter(
            p -> p.getFromConstraint().getTrackedEntityType() != null && p.getToConstraint().getTrackedEntityType() != null )
            .collect(
                Collectors.toList() ) );

        Relationship relationship = new Relationship();
        relationship.setRelationshipType( relationshipType.getId() );

        relationship.setFrom( getConstraint( cache, randomizerContext, relationshipType.getFromConstraint() ) );
        relationship.setTo( getConstraint( cache, randomizerContext, relationshipType.getToConstraint() ) );

        return relationship;
    }

    public Relationship create( String from, String to, RelationshipType relationshipType )
    {

        Relationship relationship = new Relationship();
        relationship.setRelationshipType( relationshipType.getId() );
        relationship.setTo( getConstraint( to ) );
        relationship.setFrom( getConstraint( from ) );

        return relationship;
    }

    public Relationship create(EntitiesCache cache, String from, String to )
    {
        Relationship relationship = new Relationship();
        relationship.setRelationshipType( randomTeitoTeiRelationshipType(cache).getId() );
        relationship.setTo( getConstraint( to ) );
        relationship.setFrom( getConstraint( from ) );

        return relationship;
    }

    public RelationshipType randomTeitoTeiRelationshipType( EntitiesCache cache ){

        return rnd.randomElementFromList( cache.getRelationshipTypes().stream().filter(
                        p -> p.getFromConstraint().getTrackedEntityType() != null &&
                                p.getToConstraint().getTrackedEntityType() != null
                                && Objects.equals(p.getFromConstraint().getTrackedEntityType(), p.getToConstraint().getTrackedEntityType()))
                .collect(
                        Collectors.toList() ) );


    }

    private RelationshipItem getConstraint( EntitiesCache cache, RandomizerContext context,
        RelationshipConstraint relationshipConstraint )
    {
        RelationshipItem item = new RelationshipItem();

        // todo implement event and enrollment relationships
        if ( relationshipConstraint.getTrackedEntityType() != null )
        {
            Tei randomTei;
            if ( relationshipConstraint.getProgram() != null )
            {
                randomTei = rnd
                    .randomElementFromList( cache.getTeis().get( relationshipConstraint.getProgram().getId() ) );
            }
            else
            {
                Program randomProgram = cache.getTrackerPrograms()
                    .get( rnd.randomIntInRange( 0, cache.getTeis().size() ) );
                context.setProgram( randomProgram );

                randomTei = rnd.randomElementFromList( cache.getTeis().get( randomProgram.getId() ) );
            }

            TrackedEntityInstance trackedEntityInstance = new TrackedEntityInstance();
            trackedEntityInstance.setTrackedEntityInstance( randomTei.getUid() );
            item.setTrackedEntityInstance( trackedEntityInstance );
        }

        return item;
    }

    private RelationshipItem getConstraint( String teiId )
    {
        RelationshipItem item = new RelationshipItem();

        TrackedEntityInstance trackedEntityInstance = new TrackedEntityInstance();
        trackedEntityInstance.setTrackedEntityInstance( teiId );
        item.setTrackedEntityInstance( trackedEntityInstance );

        return item;
    }
}

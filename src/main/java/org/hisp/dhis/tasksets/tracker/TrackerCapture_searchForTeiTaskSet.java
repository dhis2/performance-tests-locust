package org.hisp.dhis.tasksets.tracker;

import org.hisp.dhis.cache.Program;
import org.hisp.dhis.cache.TrackedEntityAttribute;
import org.hisp.dhis.cache.User;
import org.hisp.dhis.common.ValueType;
import org.hisp.dhis.response.dto.ApiResponse;
import org.hisp.dhis.tasks.tracker.tei.GetTeiTask;
import org.hisp.dhis.tasks.tracker.tei.QueryFilterTeiTask;
import org.hisp.dhis.tasksets.DhisAbstractTaskSet;
import org.hisp.dhis.utils.Randomizer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

/**
 * @author Gintare Vilkelyte <vilkelyte.gintare@gmail.com>
 */
public class TrackerCapture_searchForTeiTaskSet
    extends DhisAbstractTaskSet
{
    private static final String NAME = "TrackerCapture: search for tei";

    HashMap<String, List<TrackedEntityAttribute>> attributes = new HashMap<>();

    public TrackerCapture_searchForTeiTaskSet( int weight )
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
        return "http";
    }

    @Override
    public void execute()
        throws Exception
    {
        Randomizer rnd = getNextRandomizer( getName() );
        Program program = rnd.randomElementFromList( this.entitiesCache.getTrackerPrograms() );
        User user = getUser(rnd);
        String ou = rnd.randomElementFromList( user.getOrganisationUnits() );

        ApiResponse response = new QueryFilterTeiTask( 1, String
            .format( "?ou=%s&ouMode=ACCESSIBLE&program=%s%s", ou, program.getId(),
                getAttributesQuery( program, rnd) ), user.getUserCredentials(), "search by attributes", rnd )
            .executeAndGetResponse();

        List<ArrayList> rows = response.extractList( "rows" );

        if ( rows != null && !rows.isEmpty() )
        {
            ArrayList row =rnd.randomElementFromList( rows );

            String teiId = row.get( 0 ).toString();

            new GetTeiTask( teiId, user.getUserCredentials(), rnd ).execute();
        }

        waitBetweenTasks(rnd);

    }

    private String getAttributesQuery(Program program, Randomizer rnd)
    {
        AtomicReference<String> query = new AtomicReference<>( "" );

        getRandomAttributes( program.getId(), program.getMinAttributesRequiredToSearch(), rnd )
            .forEach( p -> {
                query.set( query +
                    String.format( "&attribute=%s:EQ:%s", p.getTrackedEntityAttribute(), rnd.randomString( 2 ) ) );
            } );

        return query.get();
    }

    private List<TrackedEntityAttribute> getRandomAttributes( String programId, int size, Randomizer rnd )
    {
        if ( attributes.isEmpty() )
        {
            preloadAttributes();
        }

        return rnd.randomElementsFromList( attributes.get( programId ), size );
    }

    private void preloadAttributes()
    {
        for ( Program program : this.entitiesCache.getTrackerPrograms()
        )
        {
            List<TrackedEntityAttribute> searchableAttributes = program
                .getAttributes().stream().filter( a -> a.isSearchable() && a.getValueType().equals( ValueType.TEXT ) )
                .collect( Collectors.toList() );

            if ( searchableAttributes.isEmpty() )
            {
                return;
            }

            attributes.put( program.getId(), searchableAttributes );
        }

    }
}

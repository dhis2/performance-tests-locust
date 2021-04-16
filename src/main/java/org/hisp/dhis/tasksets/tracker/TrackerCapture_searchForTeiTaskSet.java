package org.hisp.dhis.tasksets.tracker;

import org.hisp.dhis.cache.Program;
import org.hisp.dhis.cache.TrackedEntityAttribute;
import org.hisp.dhis.cache.User;
import org.hisp.dhis.common.ValueType;
import org.hisp.dhis.response.dto.ApiResponse;
import org.hisp.dhis.tasks.DhisAbstractTask;
import org.hisp.dhis.tasks.tracker.tei.GetTeiTask;
import org.hisp.dhis.tasks.tracker.tei.QueryFilterTeiTask;
import org.hisp.dhis.utils.DataRandomizer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

/**
 * @author Gintare Vilkelyte <vilkelyte.gintare@gmail.com>
 */
public class TrackerCapture_searchForTeiTaskSet
    extends DhisAbstractTask
{
    HashMap<String, List<TrackedEntityAttribute>> attributes = new HashMap<>();

    public TrackerCapture_searchForTeiTaskSet( int weight )
    {
        super( weight );
    }

    @Override
    public String getName()
    {
        return "TrackerCapture: search for tei";
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
        Program program = DataRandomizer.randomElementFromList( this.entitiesCache.getTrackerPrograms() );
        User user = getUser();
        String ou = DataRandomizer.randomElementFromList( user.getOrganisationUnits() );

        ApiResponse response = new QueryFilterTeiTask( 1, String
            .format( "?ou=%s&ouMode=ACCESSIBLE&program=%s%s", ou, program.getId(),
                getAttributesQuery( program ) ), user.getUserCredentials(), "search by attributes" )
            .executeAndGetResponse();

        List<ArrayList> rows = response.extractList( "rows" );

        if ( rows != null && !rows.isEmpty() )
        {
            ArrayList row = DataRandomizer.randomElementFromList( rows );

            String teiId = row.get( 0 ).toString();

            new GetTeiTask( teiId, user.getUserCredentials() ).execute();
        }

        waitBetweenTasks();

    }

    private String getAttributesQuery( Program program )
    {
        AtomicReference<String> query = new AtomicReference<>( "" );

        getRandomAttributes( program.getId(), program.getMinAttributesRequiredToSearch() )
            .forEach( p -> {
                query.set( query +
                    String.format( "&attribute=%s:EQ:%s", p.getTrackedEntityAttribute(), DataRandomizer.randomString( 2 ) ) );
            } );

        return query.get();
    }

    private List<TrackedEntityAttribute> getRandomAttributes( String programId, int size )
    {
        if ( attributes.isEmpty() )
        {
            preloadAttributes();
        }

        return DataRandomizer.randomElementsFromList( attributes.get( programId ), size );
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

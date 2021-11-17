package org.hisp.dhis.tasksets.tracker.importer;

import org.hisp.dhis.cache.Program;
import org.hisp.dhis.cache.TrackedEntityAttribute;
import org.hisp.dhis.cache.User;
import org.hisp.dhis.common.ValueType;
import org.hisp.dhis.random.UserRandomizer;
import org.hisp.dhis.response.dto.ApiResponse;
import org.hisp.dhis.tasks.DhisAbstractTask;
import org.hisp.dhis.tasks.tracker.importer.GetTrackerTeiTask;
import org.hisp.dhis.tasks.tracker.importer.QueryTrackerTeisTask;
import org.hisp.dhis.utils.DataRandomizer;
import org.springframework.util.CollectionUtils;

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

/**
 * @author Gintare Vilkelyte <vilkelyte.gintare@gmail.com>
 */
public class TrackerCapture_importer_searchForTeiTaskSet
    extends DhisAbstractTask
{
    HashMap<String, List<TrackedEntityAttribute>> attributes = new HashMap<>();

    public TrackerCapture_importer_searchForTeiTaskSet( int weight )
    {
        super( weight );
    }

    @Override
    public String getName()
    {
        return "TrackerCapture: search for tei (importer)";
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
        User user = new UserRandomizer().getRandomUser( this.entitiesCache );
        String ou = DataRandomizer.randomElementFromList( user.getOrganisationUnits() );

        ApiResponse response = new QueryTrackerTeisTask( 1,
            String.format( "?orgUnit=%s&ouMode=ACCESSIBLE&program=%s%s", ou, program.getId(), getAttributesQuery( program ) ),
            user.getUserCredentials() ).executeAndGetResponse();

        List<HashMap> rows = response.extractList( "instances" );

        if ( !CollectionUtils.isEmpty( rows ) )
        {
            HashMap row = DataRandomizer.randomElementFromList( rows );

            String teiId = row.get( "trackedEntity" ).toString();
            new GetTrackerTeiTask( teiId, user.getUserCredentials() ).execute();
        }

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
                .getAttributes().stream().parallel().filter( a -> a.isSearchable() && a.getValueType().equals( ValueType.TEXT ) )
                .collect( Collectors.toList() );

            if ( CollectionUtils.isEmpty( searchableAttributes ) )
            {
                return;
            }

            attributes.put( program.getId(), searchableAttributes );
        }

    }
}
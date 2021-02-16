package org.hisp.dhis.cache;

import lombok.Getter;
import lombok.Setter;
import org.hisp.dhis.cache.builder.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;

/**
 * Cache for DHIS2 entities used to generate random data for the load test
 */
@Setter
@Getter
public class EntitiesCache
{
    private List<Program> programs;

    private List<Program> trackerPrograms = new ArrayList<>();

    private List<Program> eventPrograms = new ArrayList<>();

    private List<TeiType> teiTypes;

    private Map<String, List<Tei>> teis;

    private List<User> users;

    private List<DataSet> dataSets;

    private List<Dashboard> dashboards;

    private List<Visualization> visualizations;

    private List<RelationshipType> relationshipTypes;

    private User defaultUser;

    public static <T> List<T> randomElementsFromList( List<T> list, int elements )
    {
        Collections.shuffle( list );
        if ( elements > list.size())
        {
            elements = list.size();
        }
        return list.subList( 0, elements );
    }

    public void loadAll( )
    {
        new DashboardCacheBuilder().load( this );
        new DataSetsCacheBuilder().load( this );
        new UserCacheBuilder().load( this );
        new TeiTypeCacheBuilder().load( this );
        new ProgramCacheBuilder().load( this );
        new TeiCacheBuilder().load( this );
        new RelationshipTypeCacheBuilder().load( this );

        // remove programs without tei
        this.trackerPrograms = trackerPrograms.stream().filter( p -> teis.containsKey( p.getUid() ) ).collect( toList() );
    }

    public List<Program> getProgramsWithAtLeastOnRepeatableStage()
    {
        List<Program> programs = new ArrayList<>();
        for ( Program program : this.programs )
        {
            for ( ProgramStage ps : program.getStages() )
            {
                if ( ps.isRepeatable() )
                {
                    programs.add( program );
                }
            }
        }
        return programs;
    }
}

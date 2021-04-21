package org.hisp.dhis.cache;

import lombok.Getter;
import lombok.Setter;
import org.hisp.dhis.cache.builder.*;

import java.util.ArrayList;
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
    private static EntitiesCache cache = null;

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

    private OrganisationUnit rootOu;

    private EntitiesCache()
    {
    }

    public static EntitiesCache getInstance()
    {
        if ( cache == null )
        {
            cache = new EntitiesCache();
            cache.loadAll();
        }

        return cache;
    }

    public static void setInstance( EntitiesCache entitiesCache )
    {
        cache = entitiesCache;
    }

    private void loadAll()
    {
        this.rootOu = new OuCacheBuilder().getRootOu();
        new ProgramCacheBuilder().load( this );
        new DashboardCacheBuilder().load( this );
        new DataSetsCacheBuilder().load( this );
        new UserCacheBuilder().load( this );
        new TeiTypeCacheBuilder().load( this );
        new TeiCacheBuilder( this ).load( this );
        new RelationshipTypeCacheBuilder().load( this );

        // remove programs without tei
        this.trackerPrograms = trackerPrograms.stream().filter( p -> teis.containsKey( p.getId() ) ).collect( toList() );
    }

    public List<Program> getProgramsWithAtLeastOnRepeatableStage()
    {
        List<Program> progr = new ArrayList<>();
        for ( Program program : this.programs )
        {
            for ( ProgramStage ps : program.getProgramStages() )
            {
                if ( ps.isRepeatable() )
                {
                    progr.add( program );
                }
            }
        }
        return progr;
    }
}

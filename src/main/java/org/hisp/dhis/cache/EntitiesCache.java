package org.hisp.dhis.cache;

import lombok.Getter;
import lombok.Setter;
import org.hisp.dhis.cache.builder.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

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

    private List<ProgramRuleAction> programRuleActions;

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
        new ProgramRuleActionCacheBuilder().load( this );

        updateProgramAttributes();
        // remove programs without tei
        //this.trackerPrograms = trackerPrograms.stream().filter( p -> teis.containsKey( p.getId() ) ).collect( toList() );
    }

    private void updateProgramAttributes()
    {
        List<String> teiAttributesAssignedByRules = cache.getProgramRuleActions().stream()
            .map( ProgramRuleAction::getTrackedEntityAttribute )
            .filter( Objects::nonNull )
            .collect( Collectors.toList() );

        List<String> dataElementsAssignedByProgramRules = cache.getProgramRuleActions().stream()
            .map( ProgramRuleAction::getDataElement )
            .filter( Objects::nonNull )
            .collect( Collectors.toList() );

        cache.getPrograms().forEach( program -> {
            if ( program.isHasRegistration() )
            {
                List<String> tetAttributes =
                    getTetById( program.getTrackedEntityType() )
                        .getTrackedEntityTypeAttributes().stream().map( TrackedEntityAttribute::getTrackedEntityAttribute )
                        .collect( Collectors.toList() );

                program.getAttributes().forEach( p -> {
                    if ( tetAttributes.contains( p.getTrackedEntityAttribute() ) )
                    {
                        p.setAssignedToTet( true );
                    }

                    if ( teiAttributesAssignedByRules.contains( p.getTrackedEntityAttribute() ) )
                    {
                        p.setGeneratedByProgramRule( true );
                    }
                } );
            }

            program.getProgramStages().forEach( ps -> {
                ps.getProgramStageDataElements().stream().filter( p -> dataElementsAssignedByProgramRules.contains( p.getUid() ) )
                    .forEach( p -> {
                        p.setGeneratedByProgramRules( true );
                    } );
            } );

        } );
    }

    public List<Program> getProgramsWithAtLeastOneRepeatableStage()
    {
        return getTrackerPrograms().stream().filter( Program::hasRepeatableStage ).collect( Collectors.toList());
    }

    private TeiType getTetById( String uid )
    {
        return cache.getTeiTypes().stream().filter( p -> p.getId().equals( uid ) ).findFirst().get();
    }
}

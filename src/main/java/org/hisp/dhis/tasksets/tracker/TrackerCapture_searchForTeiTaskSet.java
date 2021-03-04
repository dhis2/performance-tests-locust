package org.hisp.dhis.tasksets.tracker;

import org.hisp.dhis.cache.*;
import org.hisp.dhis.common.ValueType;
import org.hisp.dhis.response.dto.ApiResponse;
import org.hisp.dhis.tasks.DhisAbstractTask;
import org.hisp.dhis.tasks.tracker.tei.GetTeiTask;
import org.hisp.dhis.tasks.tracker.tei.QueryFilterTeiTask;
import org.hisp.dhis.utils.DataRandomizer;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Gintare Vilkelyte <vilkelyte.gintare@gmail.com>
 */
public class TrackerCapture_searchForTeiTaskSet extends DhisAbstractTask
{
    public TrackerCapture_searchForTeiTaskSet(int weight, EntitiesCache entitiesCache ) {
        this.weight = weight;
        this.entitiesCache = entitiesCache;
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

        TrackedEntityAttribute searchableAttribute = DataRandomizer.randomElementFromList( program
            .getAttributes().stream().filter( a -> a.isSearchable()&& a.getValueType() == ValueType.TEXT).collect( Collectors.toList()));

        ApiResponse response = new QueryFilterTeiTask( 1, String.format( "?ou=%s&ouMode=ACCESSIBLE&trackedEntityType=%s&attribute=%s:LIKE:%s", ou, program.getEntityType(), searchableAttribute
            .getTrackedEntityAttribute(), DataRandomizer.randomString(2)), user.getUserCredentials() ).executeAndGetResponse();

        List<ArrayList> rows = response.extractList( "rows" );

        if (rows != null && rows.size() > 0) {
            ArrayList row = DataRandomizer.randomElementFromList( rows );

            String teiId = row.get( 0 ).toString();

            new GetTeiTask( teiId, user.getUserCredentials() ).execute();
        }

        waitBetweenTasks();

    }
}

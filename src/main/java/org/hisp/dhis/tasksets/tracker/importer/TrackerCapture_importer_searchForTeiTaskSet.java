package org.hisp.dhis.tasksets.tracker.importer;

import org.hisp.dhis.cache.EntitiesCache;
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

import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Gintare Vilkelyte <vilkelyte.gintare@gmail.com>
 */
public class TrackerCapture_importer_searchForTeiTaskSet extends DhisAbstractTask
{
    public TrackerCapture_importer_searchForTeiTaskSet(int weight, EntitiesCache entitiesCache ) {
        this.weight = weight;
        this.entitiesCache = entitiesCache;
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


        List<TrackedEntityAttribute> searchableAttributes = program
            .getAttributes().stream().filter( a -> a.isSearchable()&& a.getValueType() == ValueType.TEXT).collect( Collectors.toList());

        TrackedEntityAttribute randomAttribute = DataRandomizer.randomElementFromList( searchableAttributes );


        ApiResponse response = new QueryTrackerTeisTask( 1, String.format( "?orgUnit=%s&ouMode=ACCESSIBLE&trackedEntityType=%s&attribute=%s:LIKE:%s", ou, program.getEntityType(), randomAttribute
            .getTrackedEntityAttribute(), DataRandomizer.randomString(1)), user.getUserCredentials() ).executeAndGetResponse();

        List<HashMap> rows = response.extractList( "instances" );

        if (rows != null && rows.size() > 0) {
            HashMap row = DataRandomizer.randomElementFromList( rows );

            String teiId = row.get( "trackedEntity" ).toString();
            new GetTrackerTeiTask( teiId, user.getUserCredentials() ).execute();
        }

    }
}
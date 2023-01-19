package org.hisp.dhis.tasksets.tracker.importer;

import org.hisp.dhis.cache.Program;
import org.hisp.dhis.cache.TrackedEntityAttribute;
import org.hisp.dhis.cache.User;
import org.hisp.dhis.common.ValueType;
import org.hisp.dhis.response.dto.ApiResponse;
import org.hisp.dhis.tasks.tracker.oldapi.events.QueryEventsTask;
import org.hisp.dhis.tasks.tracker.GetTeiByIdTask;
import org.hisp.dhis.tasks.tracker.QueryTrackerTeisTask;
import org.hisp.dhis.tasksets.DhisAbstractTaskSet;
import org.hisp.dhis.utils.Randomizer;
import org.springframework.util.CollectionUtils;

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

/**
 * @author Gintare Vilkelyte <vilkelyte.gintare@gmail.com>
 */
public class TrackerCapture_importer_searchForTeiTaskSet
    extends DhisAbstractTaskSet
{
    private static final String NAME = "TrackerCapture: search for tei (importer)";

    HashMap<String, List<TrackedEntityAttribute>> attributes = new HashMap<>();

    public TrackerCapture_importer_searchForTeiTaskSet( int weight )
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

        User user = getRandomUser(rnd);
        String ou = getRandomUserOrgUnit( user, rnd );

        ApiResponse response = new QueryTrackerTeisTask( 1,
            String.format( "?orgUnit=%s&ouMode=ACCESSIBLE&program=%s%s", ou, program.getId(), getAttributesQuery( program, rnd) ),
            user.getUserCredentials(), rnd ).executeAndGetResponse();

        List<HashMap> rows = response.extractList( "instances" );

        if ( !CollectionUtils.isEmpty( rows ) )
        {
            HashMap row = rnd.randomElementFromList( rows );

            String teiId = row.get( "trackedEntity" ).toString();

            rows.stream().filter( p ->  !p.get(
                    "trackedEntity" ).toString().isEmpty()
            ).forEach(  p-> {
                String tei = p.get( "trackedEntity" ).toString();
                new QueryEventsTask( String.format( "?program=%s&programStage=%s&orgUnit=%s&ouMode=DESCENDANTS&trackedEntityInstance=%s&fields=created,eventDate,dataValues[dataElement,value]",
                        program.getId(), program.getProgramStages().get( 0 ).getId(), entitiesCache.getRootOu().getId(), tei),
                        entitiesCache.getDefaultUser().getUserCredentials(), rnd ).execute();
            } );

            new GetTeiByIdTask( teiId, user.getUserCredentials(), rnd ).execute();
        }

        waitBetweenTasks(rnd);

    }

    private String getAttributesQuery(Program program, Randomizer rnd)
    {
        AtomicReference<String> query = new AtomicReference<>( "" );

        getRandomAttributes( program.getId(), program.getMinAttributesRequiredToSearch(), rnd)
            .forEach( p -> {
                query.set( query +
                    String.format( "&attribute=%s:LIKE:%s", p.getTrackedEntityAttribute(), rnd.randomString( 2 ) ) );
            } );

        return query.get();
    }

    private List<TrackedEntityAttribute> getRandomAttributes(String programId, int size, Randomizer rnd )
    {
        if ( attributes.isEmpty() )
        {
            preloadAttributes();
        }

        return rnd.randomElementsFromList( attributes.get( programId ), size );
    }

    private void preloadAttributes()
    {
        for ( Program program : this.entitiesCache.getTrackerPrograms())
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
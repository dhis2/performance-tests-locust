package org.hisp.dhis.tasksets.tracker;

import org.apache.commons.lang3.StringUtils;
import org.hisp.dhis.cache.Program;
import org.hisp.dhis.cache.TrackedEntityAttribute;
import org.hisp.dhis.response.dto.ApiResponse;
import org.hisp.dhis.tasks.DhisAbstractTask;
import org.hisp.dhis.tasks.tracker.importer.GetTrackerTeiTask;
import org.hisp.dhis.tasks.tracker.tei.QueryFilterTeiTask;
import org.hisp.dhis.textpattern.TextPattern;
import org.hisp.dhis.textpattern.TextPatternParser;
import org.hisp.dhis.textpattern.TextPatternSegment;
import org.hisp.dhis.utils.DataRandomizer;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Gintare Vilkelyte <vilkelyte.gintare@gmail.com>
 */
public class TrackerCapture_searchForTeiByUniqueAttributeTaskSet
    extends DhisAbstractTask
{
    HashMap<String, List<TrackedEntityAttribute>> attributes = new HashMap<>();

    public TrackerCapture_searchForTeiByUniqueAttributeTaskSet( int weight )
    {
        super( weight );
    }

    @Override
    public String getName()
    {
        return "Tracker capture: search for tei by unique attribute";
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
        Program program = getProgramWithAttributes();
        user = getUser();

        TrackedEntityAttribute randomAttribute = getRandomAttribute( program.getId() );

        ApiResponse response = new QueryFilterTeiTask( 1,
            String.format( "?ouMode=ACCESSIBLE&program=%s&attribute=%s:EQ:%s", program.getId(), randomAttribute
                .getTrackedEntityAttribute(), getRandomAttributeValue( randomAttribute ) ), user.getUserCredentials(),
            "search by unique attribute" )
            .executeAndGetResponse();

        List<HashMap> rows = response.extractList( "instances" );

        if ( rows != null && !rows.isEmpty() )
        {
            HashMap row = DataRandomizer.randomElementFromList( rows );

            String teiId = row.get( "trackedEntity" ).toString();
            new GetTrackerTeiTask( teiId, user.getUserCredentials() ).execute();
        }

        waitBetweenTasks();

    }

    private Program getProgramWithAttributes()
    {
        if ( attributes.isEmpty() )
        {
            preloadAttributes();
        }

        int index = DataRandomizer.randomIntInRange( 0, attributes.size() );

        String id = attributes.keySet().toArray()[index].toString();
        return entitiesCache.getTrackerPrograms().stream().filter( p -> p.getId().equals( id ) ).findFirst().orElse( null );
    }

    private TrackedEntityAttribute getRandomAttribute( String programId )
    {
        if ( attributes.isEmpty() )
        {
            preloadAttributes();
        }

        return DataRandomizer.randomElementFromList( attributes.get( programId ) );
    }

    private void preloadAttributes()
    {
        for ( Program program : this.entitiesCache.getTrackerPrograms()
        )
        {
            List<TrackedEntityAttribute> searchableAttributes = program
                .getAttributes().stream().filter( a -> a.getLastValue() != null ).collect( Collectors.toList() );

            if ( searchableAttributes.isEmpty() )
            {
                return;
            }

            attributes.put( program.getId(), searchableAttributes );
        }

    }

    private String getRandomAttributeValue( TrackedEntityAttribute entityAttribute )
        throws TextPatternParser.TextPatternParsingException
    {
        TextPattern pattern = TextPatternParser.parse( entityAttribute.getPattern() );
        TextPatternSegment staticSegment = pattern.getSegments().stream().filter( tp -> !tp.getMethod().isGenerated() )
            .findFirst()
            .orElse( null );

        int valueSegmentLength = pattern.getSegments().stream().filter( tp -> tp.getMethod().isGenerated() )
            .findFirst().get().getParameter().length();

        int topValue = Integer.parseInt( entityAttribute.getLastValue().replace( staticSegment.getParameter(), "" ) );

        String value = new DecimalFormat( StringUtils.repeat( "0", valueSegmentLength ) )
            .format( DataRandomizer.randomIntInRange( 0, topValue ) );
        return staticSegment.getParameter() + value;
    }
}

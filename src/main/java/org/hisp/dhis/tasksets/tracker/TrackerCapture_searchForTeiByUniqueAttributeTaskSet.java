package org.hisp.dhis.tasksets.tracker;

import org.apache.commons.lang3.StringUtils;
import org.hisp.dhis.cache.Program;
import org.hisp.dhis.cache.TrackedEntityAttribute;
import org.hisp.dhis.cache.User;
import org.hisp.dhis.response.dto.ApiResponse;
import org.hisp.dhis.tasks.tracker.importer.GetTrackerTeiTask;
import org.hisp.dhis.tasks.tracker.tei.oldapi.QueryFilterTeiTask;
import org.hisp.dhis.tasksets.DhisAbstractTaskSet;
import org.hisp.dhis.textpattern.TextPattern;
import org.hisp.dhis.textpattern.TextPatternParser;
import org.hisp.dhis.textpattern.TextPatternSegment;
import org.hisp.dhis.utils.Randomizer;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Gintare Vilkelyte <vilkelyte.gintare@gmail.com>
 */
public class TrackerCapture_searchForTeiByUniqueAttributeTaskSet
    extends DhisAbstractTaskSet
{
    private static final String NAME = "Tracker capture: search for tei by unique attribute";

    HashMap<String, List<TrackedEntityAttribute>> attributes = new HashMap<>();

    public TrackerCapture_searchForTeiByUniqueAttributeTaskSet( int weight )
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
        Program program = getProgramWithAttributes( rnd );
        User user = getRandomUser( rnd );

        TrackedEntityAttribute randomAttribute = getRandomAttribute( program.getId(), rnd);

        ApiResponse response = new QueryFilterTeiTask( 1,
            String.format( "?ouMode=ACCESSIBLE&program=%s&attribute=%s:EQ:%s", program.getId(), randomAttribute
                .getTrackedEntityAttribute(), getRandomAttributeValue( randomAttribute, rnd) ), user.getUserCredentials(),
            "search by unique attribute", rnd )
            .executeAndGetResponse();

        List<HashMap> rows = response.extractList( "instances" );

        if ( rows != null && !rows.isEmpty() )
        {
            HashMap row = rnd.randomElementFromList( rows );

            String teiId = row.get( "trackedEntity" ).toString();
            new GetTrackerTeiTask( teiId, user.getUserCredentials(), rnd ).execute();
        }

        waitBetweenTasks(rnd);

    }

    private Program getProgramWithAttributes( Randomizer rnd )
    {
        if ( attributes.isEmpty() )
        {
            preloadAttributes();
        }

        int index = rnd.randomIntInRange( 0, attributes.size() );


        String id = attributes.keySet().stream().toArray()[index].toString();
        return entitiesCache.getTrackerPrograms().stream().filter( p -> p.getId().equals( id ) ).findFirst().orElse( null );
    }

    private TrackedEntityAttribute getRandomAttribute(String programId, Randomizer rnd)
    {
        if ( attributes.isEmpty() )
        {
            preloadAttributes();
        }

        return rnd.randomElementFromList( attributes.get( programId ) );
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

    private String getRandomAttributeValue(TrackedEntityAttribute entityAttribute, Randomizer rnd)
        throws TextPatternParser.TextPatternParsingException
    {
        TextPattern pattern = TextPatternParser.parse( entityAttribute.getPattern() );
        TextPatternSegment staticSegment = pattern.getSegments().stream().filter( tp -> !tp.getMethod().isGenerated() )
            .findFirst()
            .orElse( new TextPatternSegment() );

        TextPatternSegment dynamicSegment =  pattern.getSegments().stream().filter( tp -> tp.getMethod().isGenerated() )
            .findFirst().orElse( null );


        // some patterns don't have fixed strings, but generate random letters
        int numberOfLetters = StringUtils.countMatches( dynamicSegment.getParameter(), "X" );

        int valueSegmentLength = dynamicSegment.getParameter().replace( "X", "" ).length();

        int topValue = Integer.parseInt( entityAttribute.getLastValue()
            .replace( staticSegment.getParameter(), "" )
            .replaceAll( "[a-zA-Z]", "" ));

        String value = new DecimalFormat( StringUtils.repeat( "0", valueSegmentLength ) )
            .format( rnd.randomIntInRange( 0, topValue ) );
        return staticSegment.getParameter() + rnd.randomLongText( numberOfLetters ) + value;
    }
}

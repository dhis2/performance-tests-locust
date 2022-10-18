package org.hisp.dhis.random;

import org.hisp.dhis.cache.TrackedEntityAttribute;
import org.hisp.dhis.common.ValueType;
import org.hisp.dhis.dxf2.events.trackedentity.Attribute;
import org.hisp.dhis.textpattern.TextPattern;
import org.hisp.dhis.textpattern.TextPatternMethod;
import org.hisp.dhis.textpattern.TextPatternMethodUtils;
import org.hisp.dhis.textpattern.TextPatternParser;
import org.hisp.dhis.textpattern.TextPatternSegment;
import org.hisp.dhis.utils.Randomizer;
import org.springframework.util.StringUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author Gintare Vilkelyte <vilkelyte.gintare@gmail.com>
 */
public class TrackedEntityAttributeRandomizer
{
    private final Randomizer rnd;

    public TrackedEntityAttributeRandomizer(Randomizer rnd) {
        this.rnd = rnd;
    }

    private List<Attribute> create(List<TrackedEntityAttribute> trackedEntityAttributes )
    {
        return trackedEntityAttributes.stream()
            .filter( p -> p.getValueType() != ValueType.FILE_RESOURCE )
            .map( att -> {
            if ( !StringUtils.isEmpty( att.getPattern() ) )
            {
                try
                {
                    String patternValue = withPattern( TextPatternParser.parse( att.getPattern() ) );
                    if ( att.getValueType().isNumeric() && patternValue.startsWith( "0" ) )
                    {
                        // Numeric type should not start with a 0
                        patternValue = patternValue.replace( "0",
                            String.valueOf( rnd.randomIntInRange( 1, 9 ) ) );
                    }
                    return new Attribute( att.getTrackedEntityAttribute(), att.getValueType(), patternValue );
                }
                catch ( TextPatternParser.TextPatternParsingException e )
                {
                    e.printStackTrace();
                    return null;
                }
            }
            else
            {
                if ( att.getOptions() == null || att.getOptions().isEmpty() )
                {
                    if ( Arrays.asList( "firstname", "first name", "given name" ).stream()
                        .anyMatch( att.getDisplayName().toLowerCase( )::contains ) )
                    {
                        return new Attribute( att.getTrackedEntityAttribute(), att.getValueType(),
                            this.rnd.randomFirstName() );
                    }

                    if ( att.getDisplayName().toLowerCase().contains( "surname" ) ||
                        att.getDisplayName().toLowerCase().contains( "given name" ) )
                    {
                        return new Attribute( att.getTrackedEntityAttribute(), att.getValueType(),
                            this.rnd.randomLastName() );
                    }

                    if ( att.getValueType().isDate() && att.getDisplayName().toLowerCase().contains( "of birth" ) )
                    {
                        return new Attribute( att.getTrackedEntityAttribute(), att.getValueType(),
                            new SimpleDateFormat("yyyy-MM-dd").format( this.rnd.randomAdultBirthday() ) );
                    }

                    if ( att.getDisplayName().toLowerCase().contains( "address" ) )
                    {
                        return new Attribute( att.getTrackedEntityAttribute(), att.getValueType(),
                            this.rnd.randomAddress() );
                    }

                    if ( att.getDisplayName().toLowerCase().contains( "national id" ) )
                    {
                        return new Attribute( att.getTrackedEntityAttribute(), att.getValueType(),
                            this.rnd.randomNationalId() );
                    }

                    return new Attribute( att.getTrackedEntityAttribute(), att.getValueType(),
                        rndValueFrom( att.getValueType() ) );
                }

                else
                {
                    return new Attribute( att.getTrackedEntityAttribute(), att.getValueType(),
                        rnd.randomElementFromList( att.getOptions() ) );
                }

            }
        } ).filter( Objects::nonNull ).collect( Collectors.toList() );
    }

    private String withPattern( TextPattern textPattern )
    {
        return generateValue( textPattern );
    }

    private String generateValue( TextPattern textPattern )
    {
        TextPatternSegment segment = getGeneratedSegment( textPattern );

        String value = "";
        if ( segment.getMethod().equals( TextPatternMethod.SEQUENTIAL ) )
        {
            value = String.format( "%0" + segment.getParameter().length() + "d", this.rnd.randomInt( Integer.MAX_VALUE ) );
        }
        else if ( segment.getMethod().equals( TextPatternMethod.RANDOM ) )
        {
            value = TextPatternMethodUtils.generateRandom( this.rnd.getRandom(), segment.getParameter() );
        }
        else
        {
            value = "";
        }

        if ( getValueSegment( textPattern ) != null )
        {
            value = getValueSegment( textPattern ).getParameter() + value;
        }

        value = "";
        return value;
    }

    public List<Attribute> create( RandomizerContext randomizerContext, boolean skipTetAttributes, boolean skipProgramAttributes )
    {
        List<TrackedEntityAttribute> attributes = new ArrayList<>();
        List<TrackedEntityAttribute> tetAttributes = randomizerContext.getProgram().getAttributes().stream()
            .filter( TrackedEntityAttribute::isAssignedToTet ).collect(
                Collectors.toList() );

        if ( !skipTetAttributes )
        {
            attributes.addAll( tetAttributes );
        }

        if ( !skipProgramAttributes )
        {
            attributes.addAll( randomizerContext.getProgram().getAttributesNotAssignedToTet() );
        }

        if ( randomizerContext.isSkipGenerationWhenAssignedByProgramRules() )
        {
            attributes.removeIf( TrackedEntityAttribute::isGeneratedByProgramRule );
        }

        return this.create( attributes.stream().filter( Objects::nonNull ).collect( Collectors.toList() ) );
    }

    private TextPatternSegment getGeneratedSegment( TextPattern textPattern )
    {
        return textPattern.getSegments().stream().filter( tp -> tp.getMethod().isGenerated() ).findFirst()
            .orElse( null );
    }

    private TextPatternSegment getValueSegment( TextPattern textPattern )
    {
        return textPattern.getSegments().stream().filter( tp -> !tp.getMethod().isGenerated() ).findFirst()
            .orElse( null );
    }

    private String rndValueFrom( ValueType valueType )
    {
        return new DataValueRandomizer(rnd).rndValueFrom( valueType );
    }

}


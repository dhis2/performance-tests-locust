package org.hisp.dhis.random;

import com.github.javafaker.Faker;
import org.hisp.dhis.cache.TrackedEntityAttribute;
import org.hisp.dhis.common.ValueType;
import org.hisp.dhis.dxf2.events.trackedentity.Attribute;
import org.hisp.dhis.textpattern.*;
import org.hisp.dhis.utils.DataRandomizer;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Gintare Vilkelyte <vilkelyte.gintare@gmail.com>
 */
public class TrackedEntityAttributeRandomizer
{
    private List<Attribute> create( List<TrackedEntityAttribute> trackedEntityAttributes )
    {
        return trackedEntityAttributes.stream().map( att -> {
            if ( !StringUtils.isEmpty( att.getPattern() ) )
            {
                try
                {
                    String patternValue = withPattern( TextPatternParser.parse( att.getPattern() ) );
                    if ( att.getValueType().isNumeric() && patternValue.startsWith( "0" ) )
                    {
                        // Numeric type should not start with a 0
                        patternValue = patternValue.replace( "0",
                            String.valueOf( DataRandomizer.randomIntInRange( 1, 9 ) ) );
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
                            Faker.instance().name().firstName() );
                    }

                    if ( att.getDisplayName().toLowerCase().contains( "surname" ) ||
                        att.getDisplayName().toLowerCase().contains( "given name" ) )
                    {
                        return new Attribute( att.getTrackedEntityAttribute(), att.getValueType(),
                            Faker.instance().name().lastName() );
                    }

                    if ( att.getValueType().isDate() && att.getDisplayName().equalsIgnoreCase( "of birth" ) )
                    {
                        return new Attribute( att.getTrackedEntityAttribute(), att.getValueType(),
                            Faker.instance().date().birthday( 16, 80 ).toInstant().toString() );
                    }

                    if ( att.getDisplayName().toLowerCase().contains( "address" ) )
                    {
                        return new Attribute( att.getTrackedEntityAttribute(), att.getValueType(),
                            Faker.instance().address().fullAddress() );
                    }

                    if ( att.getDisplayName().toLowerCase().contains( "national id" ) )
                    {
                        return new Attribute( att.getTrackedEntityAttribute(), att.getValueType(),
                            Faker.instance().idNumber().valid() );
                    }

                    return new Attribute( att.getTrackedEntityAttribute(), att.getValueType(),
                        rndValueFrom( att.getValueType() ) );
                }

                else
                {
                    return new Attribute( att.getTrackedEntityAttribute(), att.getValueType(),
                        DataRandomizer.randomElementFromList( att.getOptions() ) );
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
            value = String.format( "%0" + segment.getParameter().length() + "d", DataRandomizer.randomInt() );
        }
        else if ( segment.getMethod().equals( TextPatternMethod.RANDOM ) )
        {
            value = TextPatternMethodUtils.generateRandom( new Random(), segment.getParameter() );
        }
        else
        {
            value = "";
        }

        if ( getValueSegment( textPattern ) != null )
        {
            value = getValueSegment( textPattern ).getParameter() + value;
        }

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
        return new DataValueRandomizer().rndValueFrom( valueType );
    }

}


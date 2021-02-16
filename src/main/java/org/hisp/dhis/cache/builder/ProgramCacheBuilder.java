package org.hisp.dhis.cache.builder;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.google.gson.JsonObject;
import org.hisp.dhis.actions.RestApiActions;
import org.hisp.dhis.cache.*;
import org.hisp.dhis.common.ValueType;
import org.hisp.dhis.response.dto.ApiResponse;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static java.util.stream.Collectors.toList;

/**
 * @author Gintare Vilkelyte <vilkelyte.gintare@gmail.com>
 */
public class ProgramCacheBuilder
    implements CacheBuilder<Program>
{
    private transient LoadingCache<String, ApiResponse> programCache = Caffeine.newBuilder().maximumSize( 100 )
        .expireAfterWrite( 1, TimeUnit.HOURS ).build( this::getProgram );

    @Override
    public void load( EntitiesCache cache )
    {
        List<String> programUids = getPayload( "/api/programs" ).extractList( "programs.id" );

        List<Program> programs = new ArrayList<>();

        programs = programUids.parallelStream()
            //.filter( this::hasProgramRegistration )
            .map( ( String uid ) -> new Program( uid, getOrgUnitsFromProgram( uid ),
                getStagesFromProgram( uid ).parallelStream()
                    .map( psUid -> new ProgramStage( psUid, getDataElementsFromStage( psUid ),
                        getStageInstanceRepeatableStatus( psUid ) ) )
                    .collect( toList() ),
                getTrackerAttributesFromProgram( uid ), getTrackedEntityTypeUid( uid ), hasProgramRegistration( uid ) ) )
            .collect( toList() );

        cache.setPrograms( programs );
        cache.setTrackerPrograms( programs.stream().filter( Program::isHasRegistration ).collect(toList()) );
        cache.setEventPrograms( programs.stream().filter( program -> !program.isHasRegistration() ).collect(toList()) );

        System.out.println( "Programs loaded in cache. Size: " + cache.getPrograms().size() );

        programCache = null;
    }

    private List<String> getStagesFromProgram( String programUid )
    {
        ApiResponse response = programCache.get( programUid );

        return response.extractList( "programStages.id" );
    }

    private List<DataElement> getDataElementsFromStage( String programStageUid )
    {
        return getPayload( "/api/programStages/" + programStageUid ).extractList( "programStageDataElements.dataElement.id" )
            .parallelStream()
            .map( uid -> getDataElement( (String) uid ) ).collect( toList() );
    }

    private List<String> getOrgUnitsFromProgram( String programUid )
    {
        ApiResponse response = programCache.get( programUid );
        return response.extractList( "organisationUnits.id" );
    }

    private boolean getStageInstanceRepeatableStatus( String programStageUid )
    {
        ApiResponse response = getPayload( "/api/programStages/" + programStageUid );

        return Boolean.parseBoolean( response.extractString( "repeatable" ) );
    }

    private ApiResponse getPayload( String url )
    {
        return new RestApiActions( "" ).get( url );
    }

    private List<TrackedEntityAttribute> getTrackerAttributesFromProgram( String programUid )
    {
        ApiResponse response = programCache.get( programUid );
        List<TrackedEntityAttribute> programAttributes = new ArrayList<>();
        List<Map<String, Object>> atts = response.extractList( "programTrackedEntityAttributes" );

        for ( Map<String, Object> att : atts )
        {
            ApiResponse trackedEntityAttribute = getAttributeUniqueness(
                (String) ((Map) att.get( "trackedEntityAttribute" )).get( "id" ) );
            programAttributes
                .add( new TrackedEntityAttribute( (String) att.get( "id" ), ValueType.valueOf( (String) att.get( "valueType" ) ),
                    (String) ((Map) att.get( "trackedEntityAttribute" )).get( "id" ),
                    trackedEntityAttribute.extractObject( "generated", Boolean.class ),
                    trackedEntityAttribute.extractObject( "unique", Boolean.class ),
                    trackedEntityAttribute.extractString( "pattern" ),
                    getProgramAttributeOptionValues( trackedEntityAttribute ),
                    (Boolean) att.get( "searchable" ) ) );
        }
        return programAttributes;

    }

    private List<String> getProgramAttributeOptionValues( ApiResponse trackedEntityAttribute )
    {
        String optionSetUid = null;
        JsonObject optionSet = trackedEntityAttribute.extractJsonObject( "optionSet" );
        if ( optionSet != null )
        {
            optionSetUid = optionSet.get( "id" ).getAsString();
        }
        if ( !StringUtils.isEmpty( optionSetUid ) )
        {
            // TODO fill the array list with values from option sets
            return new ArrayList<>();

        }
        return null;
    }

    private ApiResponse getAttributeUniqueness( String trackerAttributeUid )
    {
        return getPayload( "/api/trackedEntityAttributes/" + trackerAttributeUid );
    }

    private ApiResponse getProgram( String programUid )
    {
        return getPayload( "/api/programs/" + programUid );
    }

    private String getTrackedEntityTypeUid( String programUid )
    {
        Map map = programCache.get( programUid ).extractObject( "trackedEntityType", Map.class );
        if ( map != null )
        {
            return (String) map.get( "id" );
        }
        return null;
    }

    private boolean hasProgramRegistration( String programUid )
    {
        ApiResponse response = programCache.get( programUid );

        return response.extractObject( "registration", Boolean.class );
    }

    private DataElement getDataElement( String dataElementUid )
    {
        ApiResponse response = getPayload( "/api/dataElements/" + dataElementUid );

        return new DataElement( dataElementUid, ValueType.valueOf( response.extractString( "valueType" ) ),
            dataElementHasOptionSet( response )
                ? getValuesFromOptionSet( response.extractString( "optionSet.id" ) )
                : null );
    }

    private boolean dataElementHasOptionSet( ApiResponse response )
    {
        Object optionSet = response.extract( "optionSetValue" );
        if ( optionSet != null )
        {
            return (Boolean) optionSet;
        }
        return false;
    }

    private List<String> getValuesFromOptionSet( String optionSetUid )
    {
        List<String> optionValues = new ArrayList<>();
        ApiResponse response = getPayload( "/api/optionSets/" + optionSetUid );
        List<Map> options = response.extractList( "options" );
        for ( Map optionMap : options )
        {
            optionValues.add( getOptionSetValue( (String) optionMap.get( "id" ) ) );
        }

        return optionValues;
    }

    private String getOptionSetValue( String optionSetValueId )
    {
        ApiResponse response = getPayload( "/api/options/" + optionSetValueId );
        return response.extractString( "displayName" );
    }

}

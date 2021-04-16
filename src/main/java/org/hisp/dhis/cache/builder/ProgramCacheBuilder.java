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
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import static java.util.stream.Collectors.toList;

/**
 * @author Gintare Vilkelyte <vilkelyte.gintare@gmail.com>
 */
public class ProgramCacheBuilder
    implements CacheBuilder<Program>
{
    private Logger logger = Logger.getLogger( this.getClass().getName() );

    private transient LoadingCache<String, ApiResponse> programCache = Caffeine.newBuilder().maximumSize( 100 )
        .expireAfterWrite( 1, TimeUnit.HOURS ).build( this::getProgram );

    @Override
    public void load( EntitiesCache cache )
    {
        List<Program> programs = get();
        cache.setPrograms( programs );
        cache.setTrackerPrograms( programs.stream().filter( Program::isHasRegistration ).collect( toList() ) );
        cache.setEventPrograms( programs.stream().filter( program -> !program.isHasRegistration() ).collect( toList() ) );

        logger.info( "Programs loaded in cache. Size: " + cache.getPrograms().size() );

        programCache = null;
    }

    @Override
    public List<Program> get()
    {
        List<String> programUids = getPayload( "/api/programs?fields=id" ).extractList( "programs.id" );

        return programUids.stream()
            .map( ( String uid ) -> buildProgram( uid ) )
            .collect( toList() );
    }

    private Program buildProgram( String uid )
    {
        ApiResponse response = programCache.get( uid );

        Program program = response.extractObject( "", Program.class );

        program.setAttributes( getTrackerAttributesFromProgram( uid ) );
        program.setProgramStages( getProgramStages( uid ) );

        return program;
    }

    private List<ProgramStage> getProgramStages( String uid )
    {
        ApiResponse response = programCache.get( uid );
        List<ProgramStage> programStages = new ArrayList<>();

        List<JsonObject> stages = response.extractList( "programStages", JsonObject.class );

        stages.forEach( stage -> {
            ProgramStage ps = new ProgramStage( stage.get( "id" ).getAsString(), getDataElementsFromStage( stage ),
                stage.get( "repeatable" ).getAsBoolean() );

            programStages.add( ps );
        } );

        return programStages;
    }

    private List<DataElement> getDataElementsFromStage( JsonObject programStage )
    {
        List<DataElement> dataElements = new ArrayList<>();

        programStage.get( "programStageDataElements" ).getAsJsonArray()
            .forEach( ( p ) -> {
                JsonObject de = p.getAsJsonObject().get( "dataElement" ).getAsJsonObject();
                DataElement element = new DataElement(
                    de.get( "id" ).getAsString(),
                    ValueType.valueOf( de.get( "valueType" ).getAsString() ),
                    dataElementHasOptionSet( de )
                        ? getOptionValuesFromOptionSet( de.get( "optionSet" ).getAsJsonObject().get( "id" ).getAsString() )
                        : null );

                dataElements.add( element );
            } );

        return dataElements;
    }

    private ApiResponse getPayload( String url )
    {
        return new RestApiActions( "" ).get( url );
    }

    private List<TrackedEntityAttribute> getTrackerAttributesFromProgram( String programUid )
    {
        ApiResponse response = programCache.get( programUid );

        List<TrackedEntityAttribute> programAttributes = response
            .extractList( "programTrackedEntityAttributes", TrackedEntityAttribute.class );

        programAttributes.forEach( p -> {
            JsonObject object = response.extractObject( String
                .format( "programTrackedEntityAttributes.trackedEntityAttribute.find{it.id == '%s'}",
                    p.getTrackedEntityAttribute() ), JsonObject.class );

            p.setOptions( getProgramAttributeOptionValues( object ) );
            p.setGenerated( object.get( "generated" ).getAsBoolean() );
            p.setUnique( object.get( "unique" ).getAsBoolean() );
            p.setPattern( object.get( "pattern" ).getAsString() );
            if ( p.isUnique() )
            {
                p.setSearchable( true ); // unique attributes are always searchable
            }
            if ( p.getPattern() != null && !p.getPattern().isEmpty() )
            {
                p.setLastValue( getAttributeLastValue( p.getTrackedEntityAttribute() ) );
            }
        } );

        return programAttributes;
    }

    private List<String> getProgramAttributeOptionValues( JsonObject trackedEntityAttribute )
    {
        String optionSetUid = null;
        JsonObject optionSet = trackedEntityAttribute.getAsJsonObject( "optionSet" );
        if ( optionSet != null )
        {
            optionSetUid = optionSet.get( "id" ).getAsString();
        }
        if ( !StringUtils.isEmpty( optionSetUid ) )
        {
            return getOptionValuesFromOptionSet( optionSetUid );
        }

        return null;
    }

    private String getAttributeLastValue( String attributeId )
    {
        return getPayload( "/api/trackedEntityAttributes/" + attributeId + "/generate" ).extractString( "value" );
    }

    private ApiResponse getProgram( String programUid )
    {
        return getPayload( "/api/programs/" + programUid +
            "?fields=*,organisationUnits~pluck,programStages[*,programStageDataElements[id,dataElement[*]]],programTrackedEntityAttributes[*,trackedEntityAttribute[*]],registration~rename(hasRegistration)" );
    }

    private boolean dataElementHasOptionSet( JsonObject dataElement )
    {
        Boolean optionSet = dataElement.get( "optionSetValue" ).getAsBoolean();
        if ( optionSet != null )
        {
            return optionSet;
        }

        return false;
    }

    private List<String> getOptionValuesFromOptionSet( String optionSetUid )
    {
        ApiResponse response = getPayload( String.format( "/api/optionSets/%s?fields=options[code,id]", optionSetUid ) );

        return response.extractList( "options.code" );
    }

}

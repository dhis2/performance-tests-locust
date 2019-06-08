package org.hisp.dhis.cache;

import static io.restassured.RestAssured.given;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.hisp.dhis.common.ValueType;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;

import io.restassured.http.ContentType;
import io.restassured.response.Response;

/**
 * Cache for DHIS2 entities used to generate random data for the load test
 */
public class EntitiesCache
{
    // Holds the rest-assured Response object containing the full Program object
    // payload

    private transient LoadingCache<String, Response> programCache = Caffeine.newBuilder().maximumSize( 100 )
        .expireAfterWrite( 1, TimeUnit.HOURS ).build( this::getProgram );

    private List<Program> programs;

    private List<TeiType> teiTypes;

    /**
     * Load all the DHIS2 programs from the target endpoint and builds a graph
     * containing
     * 
     * program
     *   |
     *   |__stages
     *        |
     *        |__data elements
     *                  |
     *                  |__option sets
     */
    public void loadProgramCache()
    {
        List<String> programUids = getPayload( "/api/programs" ).jsonPath().getList( "programs.id" );

        long t = System.nanoTime();

        programs = programUids.parallelStream().filter( this::hasProgramRegistration )
            .map( ( String uid ) -> new Program( uid, getOrgUnitsFromProgram( uid ),
                getStagesFromProgram( uid ).parallelStream()
                    .map( psUid -> new ProgramStage( psUid, getDataElementsFromStage( psUid ) ) )
                    .collect( Collectors.toList() ) ) )
            .collect( Collectors.toList() );

        System.out.println( (double) (System.nanoTime() - t) / 1_000_000_000 );
        // free memory
        programCache = null;
    }

    public void loadTeiTypeCache()
    {
        this.teiTypes = new ArrayList<>();

        List<Map> payload = getPayload( "/api/trackedEntityTypes" ).jsonPath().getList( "trackedEntityTypes" );

        for ( Map map : payload )
        {
            teiTypes.add( new TeiType( (String) map.get( "id" ), (String) map.get( "displayName" ) ) );
        }
    }

    private List<DataElement> getDataElementsFromStage( String programStageUid )
    {
        return getPayload( "/api/programStages/" + programStageUid ).jsonPath()
            .getList( "programStageDataElements.dataElement.id" ).parallelStream()
            .map( uid -> getDataElement( (String) uid ) ).collect( Collectors.toList() );
    }

    private DataElement getDataElement( String dataElementUid )
    {
        Response response = getPayload( "/api/dataElements/" + dataElementUid );

        return new DataElement( dataElementUid, ValueType.valueOf( response.jsonPath().get( "valueType" ) ),
            dataElementHasOptionSet( response )
                ? getValuesFromOptionSet( response.jsonPath().getString( "optionSet.id" ) )
                : null );
    }

    private List<String> getValuesFromOptionSet( String optionSetUid )
    {
        List<String> optionValues = new ArrayList<>();
        Response response = getPayload( "/api/optionSets/" + optionSetUid );
        List<Map> options = response.jsonPath().getList( "options" );
        for ( Map optionMap : options )
        {
            optionValues.add( getOptionSetValue( (String) optionMap.get( "id" ) ) );
        }

        return optionValues;
    }

    private String getOptionSetValue( String optionSetValueId )
    {
        Response response = getPayload( "/api/options/" + optionSetValueId );
        return response.jsonPath().getString( "displayName" );
    }

    private List<String> getStagesFromProgram( String programUid )
    {
        Response response = programCache.get( programUid );

        return response.jsonPath().getList( "programStages.id" );
    }

    private boolean hasProgramRegistration( String programUid )
    {
        Response response = programCache.get( programUid );

        return response.jsonPath().getBoolean( "registration" );
    }

    private List<String> getOrgUnitsFromProgram( String programUid )
    {
        Response response = programCache.get( programUid );
        return response.jsonPath().getList( "organisationUnits.id" );
    }

    private Response getProgram( String programUid )
    {
        return getPayload( "/api/programs/" + programUid );
    }

    private boolean dataElementHasOptionSet( Response response )
    {
        Object optionSet = response.jsonPath().get( "optionSetValue" );
        if ( optionSet != null )
        {
            return (Boolean) optionSet;
        }
        return false;
    }

    private Response getPayload( String uri )
    {
        return given().contentType( ContentType.JSON ).when().get( uri );
    }

    public TeiType getTeiType( String name )
    {
        return this.teiTypes.stream().filter( t -> t.getName().equalsIgnoreCase( name ) ).findFirst().orElse( null );
    }

    public List<Program> getPrograms()
    {
        return this.programs;
    }

    public Program getProgram( int index )
    {
        return this.programs.get( index );
    }
}

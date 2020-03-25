package org.hisp.dhis.cache;

import static io.restassured.RestAssured.given;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.hisp.dhis.common.ValueType;
import org.springframework.util.StringUtils;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;

import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;
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

    private Map<String, List<Tei>> teis;

    /**
     * Load all the DHIS2 programs from the target endpoint and builds a graph
     * containing
     * <p>
     * program -> program attributes
     * |
     * |__stages
     * |
     * |__data elements
     * |
     * |__option sets
     */
    public void loadProgramCache()
    {
        List<String> programUids = getPayload( "/api/programs" ).jsonPath().getList( "programs.id" );

        // Load Tracker-only programs + stages + data elements + program attributes
        programs = programUids.parallelStream().filter( this::hasProgramRegistration )
            .map( ( String uid ) -> new Program( uid, getOrgUnitsFromProgram( uid ),
                getStagesFromProgram( uid ).parallelStream()
                    .map( psUid -> new ProgramStage( psUid, getDataElementsFromStage( psUid ), getStageInstanceRepeatableStatus( psUid ) ) )
                    .collect( Collectors.toList() ),
                getTrackerAttributesFromProgram( uid ), getTrackedEntityTypeUid( uid ) ) )
            .collect( Collectors.toList() );

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

    public void loadTeiCache()
    {
        this.teis = new ConcurrentHashMap<>();
        for ( Program program : this.programs )
        {
            List<String> orgUnits = program.getOrgUnits();
            orgUnits.parallelStream().forEach( ou -> {
                // -- fetch a list of teis for each program + ou
                List<Map> payload = getPayload(
                    "/api/trackedEntityInstances?ou=" + ou + "&program=" + program.getUid() ).jsonPath()
                        .getList( "trackedEntityInstances" );
                int count = 0;
                List<Tei> teisFromProgram = new ArrayList<>();
                for ( Map map : payload )
                {
                    if ( count <= 10 ) // -- only add 10 teis for each ou
                    {
                        teisFromProgram.add( new Tei( (String) map.get( "trackedEntityInstance" ), program.getUid() ) );
                        count++;
                    } else {
                        break;
                    }
                }
                List<Tei> teis = this.teis.get( program.getUid() );
                if ( teis != null && teis.size() != 0 )
                {
                    teis.addAll( teisFromProgram );
                    this.teis.replace( program.getUid(), teis );
                }
                else
                {
                    this.teis.put( program.getUid(), teisFromProgram );
                }
            } );
        }
        //int teiSize = teis.values().stream().map(List::size).mapToInt(Integer::intValue).sum();
        System.out.println( "teis by program" );
        for ( String s : teis.keySet() )
        {
            System.out.println( "program: " + s + " - teis: " + teis.get( s ).size() );
        }

    }

    public void loadAll()
    {
        this.loadTeiTypeCache();
        System.out.println("Tracked Entity Types loaded in cache [" + this.teiTypes.size() + "]");
        this.loadProgramCache();
        System.out.println("Programs loaded in cache [" + this.programs.size() + "]");
        this.loadTeiCache();
        System.out.println("Tracked Entity Instances loaded in cache [" + this.teis.size() + "]");

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

    private boolean getStageInstanceRepeatableStatus( String programStageUid )
    {
        Response response = getPayload( "/api/programStages/" + programStageUid );

        return Boolean.parseBoolean( response.jsonPath().getString( "repeatable" ) );
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

    private List<ProgramAttribute> getTrackerAttributesFromProgram( String programUid )
    {
        Response response = programCache.get( programUid );
        List<ProgramAttribute> programAttributes = new ArrayList<>();
        List<Map<String, Object>> atts = response.jsonPath().getList( "programTrackedEntityAttributes" );

        for ( Map<String, Object> att : atts )
        {
            JsonPath trackedEntityAttribute = getAttributeUniqueness(
                (String) ((Map) att.get( "trackedEntityAttribute" )).get( "id" ) );
            programAttributes
                .add( new ProgramAttribute( (String) att.get( "id" ), ValueType.valueOf( (String) att.get( "valueType" ) ),
                    (String) ((Map) att.get( "trackedEntityAttribute" )).get( "id" ),
                    trackedEntityAttribute.getBoolean( "unique" ),
                    trackedEntityAttribute.getString( "pattern" ),
                    getProgramAttributeOptionValues( trackedEntityAttribute ) ) );
        }
        return programAttributes;

    }

    private List<String> getProgramAttributeOptionValues( JsonPath trackedEntityAttribute )
    {
        String optionSetUid = null;
        Map optionSet = trackedEntityAttribute.get( "optionSet" );
        if ( optionSet != null )
        {
            optionSetUid = (String) optionSet.get( "id" );
        }
        if ( !StringUtils.isEmpty( optionSetUid ) )
        {
            // TODO fill the array list with values from option sets
            return new ArrayList<>();

        }
        return null;

    }

    private JsonPath getAttributeUniqueness( String trackerAttributeUid )
    {
        return getPayload( "/api/trackedEntityAttributes/" + trackerAttributeUid ).jsonPath();
    }

    private String getTrackedEntityTypeUid( String programUid )
    {
        Map map = programCache.get( programUid ).jsonPath().getMap( "trackedEntityType" );
        if ( map != null )
        {
            return (String) map.get( "id" );
        }
        return null;
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

    public List<Program> getProgramsWithAtLeastOnRepeatableStage()
    {
        List<Program> programs = new ArrayList<>();
        for ( Program program : this.programs )
        {
            for ( ProgramStage ps : program.getStages() )
            {
                if ( ps.isRepeatable() )
                {
                    programs.add( program );
                }
            }
        }
        return programs;
    }

    public Map<String, List<Tei>> getTeis()
    {
        return teis;
    }
}

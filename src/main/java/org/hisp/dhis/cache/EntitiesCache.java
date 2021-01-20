package org.hisp.dhis.cache;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.google.common.collect.Lists;
import com.google.gson.JsonObject;
import org.aeonbits.owner.ConfigFactory;
import org.hisp.dhis.actions.RestApiActions;
import org.hisp.dhis.common.ValueType;
import org.hisp.dhis.locust.LocustConfig;
import org.hisp.dhis.response.dto.ApiResponse;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Cache for DHIS2 entities used to generate random data for the load test
 */
public class EntitiesCache
{
    // Holds the rest-assured Response object containing the full Program object
    // payload

    private transient LoadingCache<String, ApiResponse> programCache = Caffeine.newBuilder().maximumSize( 100 )
        .expireAfterWrite( 1, TimeUnit.HOURS ).build( this::getProgram );

    private List<Program> programs;

    private List<Program> trackerPrograms = new ArrayList<>();

    private List<Program> eventPrograms = new ArrayList<>();

    private List<TeiType> teiTypes;

    private Map<String, List<Tei>> teis;

    private List<User> users;

    private List<DataSet> dataSets;

    public static <T> List<T> randomElementsFromList( List<T> list, int elements )
    {
        Collections.shuffle( list );
        if ( elements > list.size() - 1 )
        {
            elements = list.size() - 1;
        }
        return list.subList( 0, elements );
    }

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
        List<String> programUids = getPayload( "/api/programs" ).extractList( "programs.id" );

        // Load Tracker-only programs + stages + data elements + program attributes
        programs = programUids.parallelStream()
            //.filter( this::hasProgramRegistration )
            .map( ( String uid ) -> new Program( uid, getOrgUnitsFromProgram( uid ),
                getStagesFromProgram( uid ).parallelStream()
                    .map( psUid -> new ProgramStage( psUid, getDataElementsFromStage( psUid ),
                        getStageInstanceRepeatableStatus( psUid ) ) )
                    .collect( Collectors.toList() ),
                getTrackerAttributesFromProgram( uid ), getTrackedEntityTypeUid( uid ), hasProgramRegistration( uid ) ) )
            .collect( Collectors.toList() );

        programs.parallelStream().forEach( p -> {
            if ( p.isHasRegistration() )
            {
                trackerPrograms.add( p );
            }
            else
            {
                eventPrograms.add( p );
            }
        } );

        // free memory
        programCache = null;
    }

    public void loadUserCache()
    {
        users = new ArrayList<>();
        users = getPayload(
            "/api/users?filter=organisationUnits.level:eq:5&filter=displayName:like:uio&fields=id,organisationUnits~pluck,userCredentials[username]&pageSize=10" )
            .extractList( "users", User.class );

        // if there are no dummy users, use only default specified in locust conf
        if ( users.isEmpty() )
        {
            System.out.println( "No dummy users, only default user will be added to the cache" );
            LocustConfig config = ConfigFactory.create( LocustConfig.class );
            users = getPayload( String.format(
                "/api/users?filter=userCredentials.username:eq:%s&fields=id,organisationUnits~pluck,userCredentials[username]",
                config.adminUsername() ) ).extractList( "users", User.class );
            users.get( 0 ).getUserCredentials().setPassword( config.adminPassword() );
        }
    }

    public void loadDataSetsCache()
    {
        dataSets = new ArrayList<>();
        List<JsonObject> sets = getPayload( "/api/dataSets?fields=periodType,dataSetElements[dataElement[*]],id" )
            .extractList( "dataSets", JsonObject.class );

        // filter out data sets without monthly period, since generation is not yet supported
        sets.parallelStream().filter( p -> {
            return p.get( "periodType" ).getAsString().equalsIgnoreCase( "Monthly" );
        } ).forEach( set -> {
            JsonObject obj = set.getAsJsonObject();

            List<DataElement> dataElements = new ArrayList<>();
            obj.get( "dataSetElements" ).getAsJsonArray().forEach( p -> {
                JsonObject de = p.getAsJsonObject().get( "dataElement" ).getAsJsonObject();

                List<String> optionSets = new ArrayList<>();

                if ( de.get( "optionSet" ) != null )
                {
                    optionSets.add( de.get( "optionSet" ).getAsJsonObject().get( "id" ).getAsString() );
                }

                dataElements.add( new DataElement( de.get( "id" ).getAsString(),
                    ValueType.valueOf( de.get( "valueType" ).getAsString() ),
                    optionSets ) );
            } );

            dataSets.add( new DataSet( obj.get( "id" ).getAsString(), dataElements ) );
        } );

    }

    public void loadTeiTypeCache()
    {
        this.teiTypes = new ArrayList<>();

        List<Map> payload = getPayload( "/api/trackedEntityTypes" ).extractList( "trackedEntityTypes" );

        for ( Map map : payload )
        {
            teiTypes.add( new TeiType( (String) map.get( "id" ), (String) map.get( "displayName" ) ) );
        }
    }

    /**
     * Create a map where [key] -> Program UID, [value] -> List of Tei
     */
    public void loadTeiCache()
    {
        this.teis = new HashMap<>();

        Map<String, List<Tei>> tempMap = new HashMap<>();

        for ( Program program : this.programs )
        {
            //List<List<String>> partitions = Lists.partition( program.getOrgUnits(), 500);
            if ( program.getOrgUnits().size() == 0 )
            {
                System.out.println( String.format( "Program %s doesn't have any org units", program.getUid() ) );
                return;
            }

            List<String> orgUnits = randomElementsFromList( program.getOrgUnits(), 1000 );
            List<List<String>> partitions = Lists.partition( orgUnits, 250 );

            partitions.forEach( p -> {
                final String ous = String.join( ";", p );
                List<Map> payload = getPayload(
                    "/api/trackedEntityInstances?ou=" + ous + "&pageSize=50&program=" + program.getUid() )
                    .extractList( "trackedEntityInstances" );

                // -- create a List of Tei for the current Program and OU
                List<Tei> teisFromProgram = new ArrayList<>();

                for ( Map map : payload )
                {
                    teisFromProgram.add( new Tei( (String) map.get( "trackedEntityInstance" ), program.getUid() ) );
                }

                if ( teis.containsKey( program.getUid() ) )
                {
                    List<Tei> teis = this.teis.get( program.getUid() );
                    teis.addAll( teisFromProgram );
                }
                else
                {
                    teis.put( program.getUid(), teisFromProgram );
                }
            });
        }
    }

    public void loadAll()
    {
        this.loadDataSetsCache();
        System.out.println( "Data sets cache loaded" );

        this.loadUserCache();
        System.out.println( "User cache loaded" );

        this.loadTeiTypeCache();
        System.out.println( "TEI type cache loaded" );

        this.loadProgramCache();
        System.out.println( "Program cache loaded" );

        this.loadTeiCache();
        System.out.println( "Tei cache loaded" );

        // remove programs without tei
        this.trackerPrograms = trackerPrograms.stream().filter( p -> teis.containsKey( p.getUid() ) ).collect( Collectors.toList() );

        System.out.println( "Tracked Entity Types loaded in cache [" + this.teiTypes.size() + "]" );
        System.out.println( "Programs loaded in cache [" + this.programs.size() + "]" );
        System.out.println( "Tracked Entity Instances loaded in cache ["
            + this.teis.values().stream().mapToInt( Collection::size ).sum() + "]" );
        System.out.println( "Data sets loaded in cache [" + this.dataSets.size() + "]" );
        System.out.println( "Users loaded in cache [" + this.users.size() + "]" );
    }

    private List<DataElement> getDataElementsFromStage( String programStageUid )
    {
        return getPayload( "/api/programStages/" + programStageUid ).extractList( "programStageDataElements.dataElement.id" )
            .parallelStream()
            .map( uid -> getDataElement( (String) uid ) ).collect( Collectors.toList() );
    }

    private DataElement getDataElement( String dataElementUid )
    {
        ApiResponse response = getPayload( "/api/dataElements/" + dataElementUid );

        return new DataElement( dataElementUid, ValueType.valueOf( response.extractString( "valueType" ) ),
            dataElementHasOptionSet( response )
                ? getValuesFromOptionSet( response.extractString( "optionSet.id" ) )
                : null );
    }

    private boolean getStageInstanceRepeatableStatus( String programStageUid )
    {
        ApiResponse response = getPayload( "/api/programStages/" + programStageUid );

        return Boolean.parseBoolean( response.extractString( "repeatable" ) );
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

    private List<String> getStagesFromProgram( String programUid )
    {
        ApiResponse response = programCache.get( programUid );

        return response.extractList( "programStages.id" );
    }

    private List<ProgramAttribute> getTrackerAttributesFromProgram( String programUid )
    {
        ApiResponse response = programCache.get( programUid );
        List<ProgramAttribute> programAttributes = new ArrayList<>();
        List<Map<String, Object>> atts = response.extractList( "programTrackedEntityAttributes" );

        for ( Map<String, Object> att : atts )
        {
            ApiResponse trackedEntityAttribute = getAttributeUniqueness(
                (String) ((Map) att.get( "trackedEntityAttribute" )).get( "id" ) );
            programAttributes
                .add( new ProgramAttribute( (String) att.get( "id" ), ValueType.valueOf( (String) att.get( "valueType" ) ),
                    (String) ((Map) att.get( "trackedEntityAttribute" )).get( "id" ),
                    trackedEntityAttribute.extractObject( "generated", Boolean.class ),
                    trackedEntityAttribute.extractObject( "unique", Boolean.class ),
                    trackedEntityAttribute.extractString( "pattern" ),
                    getProgramAttributeOptionValues( trackedEntityAttribute ) ) );
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

    private List<String> getOrgUnitsFromProgram( String programUid )
    {
        ApiResponse response = programCache.get( programUid );
        return response.extractList( "organisationUnits.id" );
    }

    private ApiResponse getProgram( String programUid )
    {
        return getPayload( "/api/programs/" + programUid );
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

    private ApiResponse getPayload( String uri )
    {
        return new RestApiActions( "" ).get( uri );
    }

    public TeiType getTeiType( String name )
    {
        return this.teiTypes.stream().filter( t -> t.getName().equalsIgnoreCase( name ) ).findFirst().orElse( null );
    }

    public List<Program> getPrograms()
    {
        return this.programs;
    }

    public List<Program> getTrackerPrograms() {
        return this.trackerPrograms;
    }

    public List<Program> getEventPrograms() {
        return this.eventPrograms;
    }

    public List<User> getUsers()
    {
        return this.users;
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

    public List<DataSet> getDataSets()
    {
        return dataSets;
    }
}

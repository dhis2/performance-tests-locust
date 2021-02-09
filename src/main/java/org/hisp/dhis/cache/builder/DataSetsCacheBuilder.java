package org.hisp.dhis.cache.builder;

import com.google.gson.JsonObject;
import org.hisp.dhis.actions.RestApiActions;
import org.hisp.dhis.cache.DataElement;
import org.hisp.dhis.cache.DataSet;
import org.hisp.dhis.cache.EntitiesCache;
import org.hisp.dhis.common.ValueType;
import org.hisp.dhis.response.dto.ApiResponse;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Gintare Vilkelyte <vilkelyte.gintare@gmail.com>
 */
public class DataSetsCacheBuilder
    implements CacheBuilder<DataSet>
{
    @Override
    public void load( EntitiesCache cache )
    {
        List<DataSet> dataSets = new ArrayList<>();
        List<JsonObject> sets = getPayload( "/api/dataSets?fields=periodType,dataSetElements[dataElement[*]],id" )
            .extractList( "dataSets", JsonObject.class );

        // filter out data sets without monthly period, since generation is not yet supported
        sets.parallelStream().filter( p -> Arrays
            .asList( "yearly", "monthly", "daily" ).contains( p.get( "periodType" ).getAsString().toLowerCase() ) )
            .forEach( set -> {
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

                dataSets.add( new DataSet( obj.get( "id" ).getAsString(), dataElements, obj.get( "periodType" ).getAsString() ) );
            } );

        cache.setDataSets( dataSets) ;
        System.out.println( "Data sets loaded in cache. Size: " + dataSets.size() );
    }

    private ApiResponse getPayload( String url )
    {
        return new RestApiActions( "" ).get( url );
    }
}

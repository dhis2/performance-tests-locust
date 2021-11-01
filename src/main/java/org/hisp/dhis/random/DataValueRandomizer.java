package org.hisp.dhis.random;

import org.hisp.dhis.cache.DataElement;
import org.hisp.dhis.cache.DataSet;
import org.hisp.dhis.cache.EntitiesCache;
import org.hisp.dhis.common.ValueType;
import org.hisp.dhis.dxf2.datavalue.DataValue;
import org.hisp.dhis.dxf2.datavalueset.DataValueSet;
import org.hisp.dhis.utils.DataRandomizer;

import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * @author Gintare Vilkelyte <vilkelyte.gintare@gmail.com>
 */
public class DataValueRandomizer
{
    public DataValue create( String ou, EntitiesCache entitiesCache )
    {
        DataSet dataSet = DataRandomizer.randomElementFromList( entitiesCache.getDataSets() );

        DataElement dataElement = DataRandomizer.randomElementFromList( dataSet.getDataElements() );

        DataValue dv = new DataValue();
        dv.setDataElement( dataElement.getUid() );
        dv.setOrgUnit( ou );
        dv.setPeriod( randomPeriod( dataSet.getPeriodType() ) );
        dv.setValue( rndValueFrom( dataElement.getValueType() ) );
        dv.setCategoryOptionCombo( "" );

        return dv;
    }

    public DataValueSet create( String ou, EntitiesCache entitiesCache, int numberOfValues )
    {
        List<DataValue> values = new ArrayList<>();

        for ( int i = 0; i < numberOfValues; i++ )
        {
            values.add( create( ou, entitiesCache ) );
        }

        DataValueSet set = new DataValueSet();
        set.setDataValues( values );
        return set;
    }

    public DataValueSet create( String ou, EntitiesCache entitiesCache, int min, int max )
    {
        int numberOfValues = DataRandomizer.randomIntInRange( min, max );

        return create( ou, entitiesCache, numberOfValues );
    }

    public String randomPeriod( String periodType )
    {
        Calendar calendar = new Calendar.Builder().build();
        calendar.setTime( DataRandomizer.randomPastDate() );

        if ( periodType.equalsIgnoreCase( "yearly" ) )
        {
            return "" + calendar.get( Calendar.YEAR );
        }

        if ( periodType.equalsIgnoreCase( "monthly" ) )
        {
            return new SimpleDateFormat( "yyyyMM" ).format( calendar.getTime() );
        }

        if ( periodType.equalsIgnoreCase( "daily" ) )
        {
            return new SimpleDateFormat( "yyyyMMdd" ).format( calendar.getTime() );
        }

        return "NOT_SUPPORTED_PERIOD_TYPE";
    }

    protected String rndValueFrom( ValueType valueType )
    {
        switch ( valueType )
        {
        case TEXT:
            return DataRandomizer.randomString( 8 );
        case LONG_TEXT:
            return DataRandomizer.faker().lorem().sentence( 50 );
        case LETTER:
            return DataRandomizer.randomString( 1 );
        case PHONE_NUMBER:
            return DataRandomizer.faker().phoneNumber().cellPhone();
        case EMAIL:
            return DataRandomizer.faker().name().username() + "@dhis2.org";
        case BOOLEAN:
            return String.valueOf( DataRandomizer.randomBoolean() );
        case TRUE_ONLY:
            return "true";
        case DATE:
        case DATETIME:
            return DataRandomizer.randomPastDate( DateTimeFormatter.ISO_LOCAL_DATE );
        case NUMBER:
        case UNIT_INTERVAL:
            return String.valueOf( DataRandomizer.randomDoubleInRange( 100, 1000, 1 ) );
        case PERCENTAGE:
            return String.valueOf( DataRandomizer.randomIntInRange( 1, 100 ) );
        case INTEGER:
        case INTEGER_POSITIVE:
        case INTEGER_ZERO_OR_POSITIVE:
            return String.valueOf( DataRandomizer.randomIntInRange( 1, 100000 ) );

        case AGE:
            return String.valueOf( DataRandomizer.randomIntInRange( 1, 80 ) );
        default:
            return "";
        }
    }
}

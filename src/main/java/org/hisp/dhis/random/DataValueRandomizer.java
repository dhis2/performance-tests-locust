package org.hisp.dhis.random;

import org.apache.commons.collections4.CollectionUtils;
import org.hisp.dhis.cache.DataElement;
import org.hisp.dhis.cache.DataSet;
import org.hisp.dhis.cache.EntitiesCache;
import org.hisp.dhis.common.ValueType;
import org.hisp.dhis.dxf2.datavalue.DataValue;
import org.hisp.dhis.dxf2.datavalueset.DataValueSet;
import org.hisp.dhis.utils.Randomizer;

import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Gintare Vilkelyte <vilkelyte.gintare@gmail.com>
 */
public class DataValueRandomizer
{

    private final Randomizer rnd;

    public DataValueRandomizer( Randomizer rnd ) {
        this.rnd = rnd;
    }

    public DataValue create(String ou, EntitiesCache entitiesCache )
    {
        DataSet dataSet = rnd.randomElementFromList( entitiesCache.getDataSets() );

        DataElement dataElement = rnd.randomElementFromList( dataSet.getDataElements().stream().filter( p ->
            p.getValueType() != ValueType.FILE_RESOURCE
        ).collect( Collectors.toList() ) );

        DataValue dv = new DataValue();
        dv.setDataElement( dataElement.getUid() );
        dv.setOrgUnit( ou );
        dv.setPeriod( randomPeriod( dataSet.getPeriodType(), dataSet.getOpenFuturePeriods() ) );

        if ( !CollectionUtils.isEmpty( dataElement.getOptionSet() ) )
        {
            dv.setValue( rnd.randomElementFromList( dataElement.getOptionSet() ) );
        }

        else
        {
            dv.setValue( rndValueFrom( dataElement.getValueType() ) );
        }

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
        int numberOfValues = rnd.randomIntInRange( min, max );

        return create( ou, entitiesCache, numberOfValues );
    }

    public String randomPeriod( String periodType, int openFuturePeriods )
    {
        Calendar calendar = new Calendar.Builder().build();

        int min = 0;
        String pattern = "unknown";

        switch ( periodType.trim().toLowerCase() )
        {
        case "yearly":
        {
            pattern = "yyyy";
            min = openFuturePeriods == 0 ? 365 : 0;
            break;
        }

        case "monthly":
        {
            pattern = "yyyyMM";
            min = openFuturePeriods == 0 ? 365 : 0;
            break;
        }
        case "daily":
        {
            pattern = "yyyyMMdd";
            min = openFuturePeriods == 0 ? 1 : 0;
            break;
        }
        default:
            return "UNSUPPORTED_PERIOD_TYPE";
        }

        calendar.setTime( rnd.randomPastDate() );

        return new SimpleDateFormat( pattern ).format( calendar.getTime() );
    }

    protected String rndValueFrom( ValueType valueType )
    {
        switch ( valueType )
        {
        case TEXT:
            return rnd.randomString( 8 );
        case LONG_TEXT:
            return rnd.randomLongText( 50 );
        case LETTER:
            return rnd.randomString( 1 );
        case PHONE_NUMBER:
            return rnd.randomPhoneNumber();
        case EMAIL:
            return rnd.randomUsername() + "@dhis2.org";
        case BOOLEAN:
            return String.valueOf( rnd.randomBoolean() );
        case TRUE_ONLY:
            return "true";
        case DATE:
        case DATETIME:
            return rnd.randomPastDate( DateTimeFormatter.ISO_LOCAL_DATE );
        case NUMBER:
        case UNIT_INTERVAL:
            return String.valueOf( rnd.randomDoubleInRange( 100, 1000, 1 ) );
        case PERCENTAGE:
            return String.valueOf( rnd.randomIntInRange( 1, 100 ) );
        case INTEGER:
        case INTEGER_POSITIVE:
        case INTEGER_ZERO_OR_POSITIVE:
            return String.valueOf( rnd.randomIntInRange( 1, 100000 ) );
        case TIME:
            return "05:00";
        case AGE:
            return String.valueOf( rnd.randomIntInRange( 1, 80 ) );
        default:
            return null;
        }
    }
}

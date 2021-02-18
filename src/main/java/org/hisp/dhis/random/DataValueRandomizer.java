package org.hisp.dhis.random;

import com.github.javafaker.Faker;
import org.hisp.dhis.cache.DataElement;
import org.hisp.dhis.cache.DataSet;
import org.hisp.dhis.cache.EntitiesCache;
import org.hisp.dhis.common.ValueType;
import org.hisp.dhis.dxf2.datavalue.DataValue;
import org.hisp.dhis.dxf2.datavalueset.DataValueSet;
import org.hisp.dhis.utils.DataRandomizer;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * @author Gintare Vilkelyte <vilkelyte.gintare@gmail.com>
 */
public class DataValueRandomizer
{
    public DataValue create( String ou, EntitiesCache entitiesCache ) {
        DataSet dataSet = DataRandomizer.randomElementFromList( entitiesCache.getDataSets());

        DataElement dataElement = DataRandomizer.randomElementFromList( dataSet.getDataElements() );

        DataValue dv = new DataValue();
        dv.setDataElement( dataElement.getUid() );
        dv.setOrgUnit( ou );
        dv.setPeriod( randomPeriod(dataSet.getPeriodType()) );
        dv.setValue( rndValueFrom( dataElement.getValueType() ) );
        dv.setCategoryOptionCombo( "" );

        return dv;
    }

    public DataValueSet create( String ou, EntitiesCache entitiesCache, int numberOfValues) {
        List<DataValue> values = new ArrayList<>();

        for ( int i = 0; i < numberOfValues; i++ ) {
            values.add( create( ou, entitiesCache ));
        }

        DataValueSet set = new DataValueSet();
        set.setDataValues( values );
        return set;
    }

    public DataValueSet create( String ou, EntitiesCache entitiesCache, int min, int max) {
        List<DataValue> values = new ArrayList<>();

        int numberOfValues = DataRandomizer.randomIntInRange( min, max );

        return create( ou,entitiesCache,numberOfValues );
    }


    public String randomPeriod(String periodType) {
        Calendar calendar = new Calendar.Builder().build();
        calendar.setTime( DataRandomizer.randomPastDate() );

        if (periodType.equalsIgnoreCase( "yearly" )) {
            return "" + calendar.get( Calendar.YEAR );
        }

        if (periodType.equalsIgnoreCase( "monthly" )) {
            return new SimpleDateFormat("yyyyMM").format( calendar.getTime() );
        }

        if (periodType.equalsIgnoreCase( "daily" )) {
            return new SimpleDateFormat("yyyyMMdd").format( calendar.getTime() );
        }

        return "NOT_SUPPORTED_PERIOD_TYPE";
    }

    protected String rndValueFrom( ValueType valueType )
    {
        String val = null;

        if ( valueType.equals( ValueType.BOOLEAN ) )
        {
            val = String.valueOf( DataRandomizer.randomBoolean() );
        }
        else if ( valueType.equals( ValueType.PHONE_NUMBER )) {
            val = Faker.instance().phoneNumber().cellPhone();
            return val;
        }

        else if ( valueType.equals( ValueType.TRUE_ONLY ) )
        {
            return "true";
        }
        else if ( valueType.isDate() )
        {
            val = DataRandomizer.randomPastDate( DateTimeFormatter.ISO_LOCAL_DATE );
        }
        else if ( valueType.equals( ValueType.PERCENTAGE ) )
        {
            val = String.valueOf( DataRandomizer.randomIntInRange( 1, 100 ) );
        }
        else if ( valueType.isNumeric() )
        {
            val = String.valueOf( DataRandomizer.randomIntInRange( 1, 100000 ) );
        }
        else if ( valueType.isDecimal() )
        {
            val = String.valueOf( DataRandomizer.randomDoubleInRange( 100, 1000, 1 ) );
        }
        else if ( valueType.isText() )
        {
            val = DataRandomizer.randomString();
        }
        else if ( valueType.isOrganisationUnit() )
        {
            val = ""; // TODO
        }
        else if ( valueType.isGeo() )
        {
//            Point p = createRandomPoint();
//            val = p.getY() + ", " + p.getY();
            val = ""; // TODO
        }

        return val;
    }
}

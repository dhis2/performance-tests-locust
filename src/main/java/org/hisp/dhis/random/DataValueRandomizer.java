package org.hisp.dhis.random;

import com.github.javafaker.Faker;
import org.hisp.dhis.cache.AggregateDataValue;
import org.hisp.dhis.cache.DataElement;
import org.hisp.dhis.cache.DataSet;
import org.hisp.dhis.cache.EntitiesCache;
import org.hisp.dhis.common.AggregatedValue;
import org.hisp.dhis.common.ValueType;
import org.hisp.dhis.utils.DataRandomizer;

import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * @author Gintare Vilkelyte <vilkelyte.gintare@gmail.com>
 */
public class DataValueRandomizer
{
    public AggregateDataValue create( String ou, EntitiesCache entitiesCache ) {
        DataSet dataSet = DataRandomizer.randomElementFromList( entitiesCache.getDataSets());

        DataElement dataElement = DataRandomizer.randomElementFromList( dataSet.getDataElements() );

        return new AggregateDataValue( dataElement.getUid(),"", dataSet.getId(), ou, randomPeriod(), rndValueFrom( dataElement.getValueType() ) );
    }


    public String randomPeriod() {
        Calendar calendar = new Calendar.Builder().build();
        calendar.setTime( DataRandomizer.randomPastDate() );

        int randomYear = calendar.get( Calendar.YEAR );
        String month = "";
        if (calendar.get( Calendar.MONTH ) + 1 < 10) {
            month = "0" + (calendar.get( Calendar.MONTH ) + 1);
        }

        return String.format( "%s%s", randomYear, month);
    }

    protected String rndValueFrom( ValueType valueType )
    {
        String val = null;

        if ( valueType.equals( ValueType.BOOLEAN ) )
        {
            val = String.valueOf( DataRandomizer.randomBoolean() );
        }
        else if ( valueType.equals( ValueType.TRUE_ONLY ) )
        {
            return "true";
        }
        else if ( valueType.isDate() )
        {
            val = DataRandomizer.randomDate( DateTimeFormatter.ISO_LOCAL_DATE );
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

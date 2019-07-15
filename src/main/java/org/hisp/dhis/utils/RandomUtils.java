package org.hisp.dhis.utils;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.Random;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import org.apache.commons.math3.random.RandomDataGenerator;

public class RandomUtils
{
    public static int getRandomNumberInRange( int min, int max )
    {
        Random r = new Random();
        return r.ints( min, (max + 1) ).findFirst().getAsInt();

    }

    public static boolean getRandomBoolean()
    {
        return Math.random() < 0.5;
    }

    public static Date getRandomDate()
    {

        Random random = new Random();
        int minDay = (int) LocalDate.of( 1900, 1, 1 ).toEpochDay();
        int maxDay = (int) LocalDate.of( 2015, 1, 1 ).toEpochDay();
        long randomDay = minDay + random.nextInt( maxDay - minDay );

        return java.util.Date
                .from( LocalDate.ofEpochDay( randomDay ).atStartOfDay().atZone( ZoneId.systemDefault() ).toInstant() );
    }

    public static long getRandomLong( long min, long max )
    {
        return new RandomDataGenerator().nextLong( min, max );
    }

    public static double getRandomDecimal()
    {
        return Math.random();
    }

    public static Point getRandomPoint()
    {
        double latitude = (Math.random() * 180.0) - 90.0;
        double longitude = (Math.random() * 360.0) - 180.0;
        GeometryFactory geometryFactory = new GeometryFactory();
        /* Longitude (= x coord) first ! */
        return geometryFactory.createPoint( new Coordinate( longitude, latitude ) );
    }

}
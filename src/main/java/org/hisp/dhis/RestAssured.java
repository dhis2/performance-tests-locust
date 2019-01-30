package org.hisp.dhis;

/**
 * @author Gintare Vilkelyte <vilkelyte.gintare@gmail.com>
 */
public class RestAssured
{
    private static ThreadLocal<io.restassured.RestAssured> restAssuredThreadLocal = new ThreadLocal<io.restassured.RestAssured>();

    public static io.restassured.RestAssured getRestAssured()
    {
        return RestAssured.restAssuredThreadLocal.get();
    }
}

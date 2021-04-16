package org.hisp.dhis;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializer;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.config.DecoderConfig;
import io.restassured.config.EncoderConfig;
import io.restassured.config.ObjectMapperConfig;
import io.restassured.mapper.ObjectMapperType;
import io.restassured.specification.RequestSpecification;
import org.hisp.dhis.utils.AuthFilter;

import java.time.Instant;

import static io.restassured.config.RestAssuredConfig.config;
import static org.aeonbits.owner.ConfigFactory.create;

/**
 * @author Gintare Vilkelyte <vilkelyte.gintare@gmail.com>
 */
public class RestAssuredConfig
{
    private static final TestConfig cfg = create( TestConfig.class );

    private RestAssuredConfig()
    {
    }

    public static void init()
    {
        io.restassured.RestAssured.config = config().
            decoderConfig(
                new DecoderConfig( "UTF-8" )
            ).encoderConfig(
            new EncoderConfig( "UTF-8", "UTF-8" )
        ).objectMapperConfig(
            new ObjectMapperConfig()
                .defaultObjectMapperType( ObjectMapperType.GSON )
                .gsonObjectMapperFactory( ( type, s ) ->
                    new GsonBuilder().setDateFormat( "yyyy-MM-dd" )
                        .registerTypeAdapter( Instant.class,
                            (JsonSerializer<Instant>) ( src, typeOfSrc, context ) -> new JsonPrimitive( src.toString() ) )
                        .create() )
        );

        io.restassured.RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
        io.restassured.RestAssured.requestSpecification = defaultRequestSpecification();
        io.restassured.RestAssured.baseURI = cfg.targetUri();
    }

    private static RequestSpecification defaultRequestSpecification()
    {
        RequestSpecBuilder requestSpecification = new RequestSpecBuilder();
        requestSpecification.addFilter( new AuthFilter() );
        requestSpecification.setBaseUri( cfg.targetUri() );

        return requestSpecification.build();
    }
}

package org.hisp.dhis.cache;

import com.google.gson.annotations.JsonAdapter;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hisp.dhis.cache.serializer.ObjectIdDeserializer;

/**
 * @author Gintare Vilkelyte <vilkelyte.gintare@gmail.com>
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ProgramRuleAction
{
    @JsonAdapter( ObjectIdDeserializer.class )
    private String dataElement;

    @JsonAdapter( ObjectIdDeserializer.class )
    private String trackedEntityAttribute;

}

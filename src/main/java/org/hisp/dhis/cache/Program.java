package org.hisp.dhis.cache;

import com.google.gson.annotations.JsonAdapter;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.hisp.dhis.cache.serializer.ObjectIdDeserializer;

import java.util.List;

@Getter
@AllArgsConstructor
public class Program
{
    private String id;

    private List<String> organisationUnits;

    @Setter
    private List<ProgramStage> programStages;

    @Setter
    private List<TrackedEntityAttribute> attributes;

    @Setter
    @JsonAdapter( ObjectIdDeserializer.class )
    private String trackedEntityType;

    private boolean hasRegistration;

    private int minAttributesRequiredToSearch;

    public Program()
    {
    }

    public String getOrgUnit( int index )
    {
        return this.organisationUnits.get( index );
    }
}

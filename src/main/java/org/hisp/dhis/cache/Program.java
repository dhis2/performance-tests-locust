package org.hisp.dhis.cache;

import com.google.gson.annotations.JsonAdapter;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hisp.dhis.cache.serializer.ObjectIdDeserializer;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@AllArgsConstructor
@NoArgsConstructor
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

    public List<TrackedEntityAttribute> getAttributesNotAssignedToTet()
    {
        return attributes.stream().filter( p -> !p.isAssignedToTet() ).collect( Collectors.toList() );
    }

    public List<TrackedEntityAttribute> getAttributesNotAssignedByProgramRule()
    {
        return attributes.stream().filter( p -> !p.isGeneratedByProgramRule() ).collect( Collectors.toList() );
    }

    public boolean hasRepeatableStage() {
        return programStages.stream().anyMatch( ProgramStage::isRepeatable );
    }
}

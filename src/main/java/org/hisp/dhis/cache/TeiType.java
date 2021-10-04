package org.hisp.dhis.cache;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
@EqualsAndHashCode
public class TeiType
{
    private String id;

    private String name;

    private List<TrackedEntityAttribute> trackedEntityTypeAttributes;

    public TeiType()
    {
    }
}

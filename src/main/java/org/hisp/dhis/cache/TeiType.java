package org.hisp.dhis.cache;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@AllArgsConstructor
@EqualsAndHashCode
public class TeiType
{
    private String uid;

    private String name;

    public TeiType()
    {
    }
}

package org.hisp.dhis.cache;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@AllArgsConstructor
@EqualsAndHashCode
public class CategoryOptionCombo
{
    private String uid;

    private String displayName;

    public CategoryOptionCombo()
    {
    }
}

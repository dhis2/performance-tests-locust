package org.hisp.dhis.cache;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.hisp.dhis.common.ValueType;

import java.util.List;

@Getter
@AllArgsConstructor
public class DataElement
{
    private String uid;

    private ValueType valueType;

    private List<String> optionSet;

    public DataElement()
    {
    }
}

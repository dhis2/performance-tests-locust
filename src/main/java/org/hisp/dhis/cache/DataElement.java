package org.hisp.dhis.cache;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hisp.dhis.common.ValueType;

import java.util.List;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class DataElement
{
    private String uid;

    private ValueType valueType;

    private List<String> optionSet;

    @Setter
    private boolean generatedByProgramRules;
}

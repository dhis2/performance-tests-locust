package org.hisp.dhis.cache;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class ProgramStage
{
    private String uid;

    private List<DataElement> dataElements;

    public ProgramStage()
    {
    }
}

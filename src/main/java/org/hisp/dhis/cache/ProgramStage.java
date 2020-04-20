package org.hisp.dhis.cache;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ProgramStage
{
    private String uid;

    private List<DataElement> dataElements;

    private boolean repeatable;

    public ProgramStage()
    {
    }
    
    public ProgramStage( String uid )
    {
        this.uid = uid;
    }
}

package org.hisp.dhis.cache;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class ProgramStage
{
    private String id;

    private List<DataElement> programStageDataElements;

    private boolean repeatable;

    public ProgramStage()
    {
    }

    public ProgramStage( String id )
    {
        this.id = id;
    }
}

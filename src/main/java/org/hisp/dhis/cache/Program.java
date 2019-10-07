package org.hisp.dhis.cache;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class Program
{
    private String uid;

    private List<String> orgUnits;

    private List<ProgramStage> stages;

    private List<ProgramAttribute> attributes;

    public String getOrgUnit( int index )
    {

        return this.orgUnits.get( index );
    }

    public Program() {
    }
}

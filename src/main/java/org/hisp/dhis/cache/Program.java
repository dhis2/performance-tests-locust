package org.hisp.dhis.cache;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class Program
{
    private String uid;

    private List<String> orgUnits;

    private List<ProgramStage> stages;

    private List<TrackedEntityAttribute> attributes;

    private String entityType;

    private boolean hasRegistration;

    public Program()
    {
    }

    public String getOrgUnit( int index )
    {
        return this.orgUnits.get( index );
    }
}

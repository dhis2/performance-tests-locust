package org.hisp.dhis.cache;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hisp.dhis.period.PeriodType;

import java.util.List;

/**
 * @author Gintare Vilkelyte <vilkelyte.gintare@gmail.com>
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class DataSet
{
    private String id;

    private List<DataElement> dataElements;

    private String periodType;
}

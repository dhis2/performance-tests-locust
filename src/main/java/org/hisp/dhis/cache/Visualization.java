package org.hisp.dhis.cache;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * @author Gintare Vilkelyte <vilkelyte.gintare@gmail.com>
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Setter
public class Visualization
{
    private String id;

    private List<String> periods;

    private List<String> filterDimensions;

    private List<String> columnDimensions;

    private List<String> rowDimensions;

    private List<String> dataDimensionItems;
}

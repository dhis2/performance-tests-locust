package org.hisp.dhis.cache;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * @author Gintare Vilkelyte <vilkelyte.gintare@gmail.com>
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class DashboardItem
{
    private String id;
    private Visualization visualization;

}

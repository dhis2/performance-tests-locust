package org.hisp.dhis.models;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author Gintare Vilkelyte <vilkelyte.gintare@gmail.com>
 */
@Getter
@AllArgsConstructor
public class AggregateDataValue
{
    private String de;

    private String co;

    private String ds;

    private String ou;

    private String pe;

    private String value;
}

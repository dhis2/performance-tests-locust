package org.hisp.dhis.cache;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @author Gintare Vilkelyte <vilkelyte.gintare@gmail.com>
 */
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class RelationshipType
{
    private String id;

    private String name;

    private RelationshipConstraint fromConstraint;

    private RelationshipConstraint toConstraint;

    private boolean bidirectional;

}

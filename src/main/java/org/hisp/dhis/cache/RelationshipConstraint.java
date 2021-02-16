package org.hisp.dhis.cache;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @author Gintare Vilkelyte <vilkelyte.gintare@gmail.com>
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RelationshipConstraint {
    private String relationshipEntity;
    private Program program;
    private TeiType trackedEntityType;
}

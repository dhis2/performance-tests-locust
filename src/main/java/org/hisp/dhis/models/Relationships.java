package org.hisp.dhis.models;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.hisp.dhis.tracker.domain.Relationship;

import java.util.List;

/**
 * @author Gintare Vilkelyte <vilkelyte.gintare@gmail.com>
 */
@Getter
@Setter
@Builder
public class Relationships
{
    public List<Relationship> relationships;
}

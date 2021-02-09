package org.hisp.dhis.models;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.hisp.dhis.tracker.domain.TrackedEntity;

import java.util.List;

/**
 * @author Gintare Vilkelyte <vilkelyte.gintare@gmail.com>
 */
@Getter
@Builder
@Setter
public class TrackedEntities
{
    public List<TrackedEntity> trackedEntities;
}

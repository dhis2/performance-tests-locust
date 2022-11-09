package org.hisp.dhis.models;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.hisp.dhis.tracker.domain.Enrollment;

import java.util.List;

/**
 * @author Gintare Vilkelyte <vilkelyte.gintare@gmail.com>
 */
@Getter
@Setter
@Builder
public class Enrollments
{
    private List<Enrollment> enrollments;
}

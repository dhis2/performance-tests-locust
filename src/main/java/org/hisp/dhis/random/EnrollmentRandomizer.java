package org.hisp.dhis.random;

import net.andreinc.mockneat.unit.objects.From;
import org.hisp.dhis.cache.EntitiesCache;
import org.hisp.dhis.cache.Program;
import org.hisp.dhis.tracker.bundle.TrackerBundleParams;
import org.hisp.dhis.tracker.domain.Enrollment;
import org.hisp.dhis.tracker.domain.EnrollmentStatus;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class EnrollmentRandomizer
{
    private DateFormat df = new SimpleDateFormat( "yyyy-MM-dd" );

    public Enrollment createEnrollment( String teiUid, String eId, Program program, String ou )
    {
        Enrollment enrollment = new Enrollment();
        enrollment.setEnrollment( eId );
        enrollment.setProgram( program.getUid() );
        enrollment.setTrackedEntity( teiUid );
        enrollment.setOrgUnit( ou );
        enrollment.setCreatedAt( df.format( new Date() ) );
        enrollment.setOccurredAt( df.format( new Date() ) );
        enrollment.setStatus( EnrollmentStatus.ACTIVE );
        enrollment.setFollowUp( false );
        enrollment.setDeleted( false );
        enrollment.setEnrolledAt( df.format( new Date() ) );

        return enrollment;
    }

    public TrackerBundleParams createBundle( Map<String, String> idMap, Program program, EntitiesCache cache )
    {
        String ou = getRandomOrgUnitFromProgram( program );
        List<Enrollment> list = new ArrayList<>();
        Set<Map.Entry<String, String>> entries = idMap.entrySet();
        for ( Map.Entry<String, String> entry : entries )
        {
            list.add( createEnrollment( entry.getKey(), entry.getValue(), program, ou ) );
        }

        TrackerBundleParams params = new TrackerBundleParams();
        params.setEnrollments( list );
        return params;
    }

    private String getRandomOrgUnitFromProgram( Program program )
    {
        List<String> orgUnits = program.getOrgUnits();
        return From.from( orgUnits ).get();
    }

}

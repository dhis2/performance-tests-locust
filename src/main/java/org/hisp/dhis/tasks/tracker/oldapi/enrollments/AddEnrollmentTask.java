package org.hisp.dhis.tasks.tracker.oldapi.enrollments;

import org.hisp.dhis.actions.AuthenticatedApiActions;
import org.hisp.dhis.cache.UserCredentials;
import org.hisp.dhis.dxf2.events.enrollment.Enrollments;
import org.hisp.dhis.response.dto.ApiResponse;
import org.hisp.dhis.tasks.DhisAbstractTask;
import org.hisp.dhis.utils.Randomizer;

/**
 * @author Gintare Vilkelyte <vilkelyte.gintare@gmail.com>
 */
public class AddEnrollmentTask
    extends DhisAbstractTask
{
    private String endpoint = "/api/enrollments";

    private Enrollments enrollments;

    private ApiResponse response;

    public AddEnrollmentTask( int weight, Enrollments enrollments, UserCredentials userCredentials, Randomizer randomizer )
    {
        super( weight,randomizer );
        this.enrollments = enrollments;
        this.userCredentials = userCredentials;
    }

    @Override
    public String getName()
    {
        return endpoint;
    }

    @Override
    public String getType()
    {
        return "POST";
    }

    @Override
    public void execute()
        throws Exception
    {
        AuthenticatedApiActions authenticatedApiActions = new AuthenticatedApiActions( endpoint, this.userCredentials );

        response = performTaskAndRecord( () -> authenticatedApiActions.post( this.enrollments ), 201 );
    }

    public ApiResponse executeAndGetBody()
        throws Exception
    {
        this.execute();
        return response;
    }
}

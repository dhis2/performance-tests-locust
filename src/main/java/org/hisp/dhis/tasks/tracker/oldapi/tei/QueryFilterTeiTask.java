package org.hisp.dhis.tasks.tracker.oldapi.tei;

/*
 * Copyright (c) 2004-2019, University of Oslo
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * Neither the name of the HISP project nor the names of its contributors may
 * be used to endorse or promote products derived from this software without
 * specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

import org.hisp.dhis.actions.AuthenticatedApiActions;
import org.hisp.dhis.cache.UserCredentials;
import org.hisp.dhis.response.dto.ApiResponse;
import org.hisp.dhis.tasks.DhisAbstractTask;
import org.hisp.dhis.utils.Randomizer;

/**
 * @author David Katuscak (katuscak.d@gmail.com)
 */
public class QueryFilterTeiTask
    extends
    DhisAbstractTask
{
    private String endpoint = "/api/trackedEntityInstances/query";

    private String identifier = "";

    private String query;

    private ApiResponse response;

    public QueryFilterTeiTask( int weight, String query, UserCredentials userCredentials,
                               String customIdentifier, Randomizer randomizer )
    {
        super( weight,randomizer );
        this.query = query;
        this.userCredentials = userCredentials;
        this.identifier = String.format( " ( %s )", customIdentifier );
    }

    @Override
    public String getName()
    {
        return endpoint + identifier;
    }

    @Override
    public String getType()
    {
        return "GET";
    }

    @Override
    public void execute()
    {
        this.response = new AuthenticatedApiActions( this.endpoint, this.userCredentials ).get( this.query );

        record( response.getRaw() );
    }

    public ApiResponse executeAndGetResponse()
    {
        this.execute();
        return this.response;
    }
}

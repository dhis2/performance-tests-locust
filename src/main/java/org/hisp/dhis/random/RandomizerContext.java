package org.hisp.dhis.random;

/*
 * Copyright (c) 2004-2020, University of Oslo
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

import lombok.Getter;
import lombok.Setter;
import org.hisp.dhis.cache.Program;
import org.hisp.dhis.cache.ProgramStage;

/**
 * @author Luciano Fiandesio
 */
@Getter
@Setter
public class RandomizerContext
{
    private Program program;

    private ProgramStage programStage;

    private String orgUnitUid;

    private String teiId;

    private String enrollmentId;

    private String teiType;

    /**
     * If true, tracked entity attributes assigned to program will be created in enrollment.
     * Can be used when importing TEIs with NTI.
     */
    private boolean programAttributesInEnrollment;

    /**
     * If true, TrackedEntityInstances, Enrollments and Events will have generated ids.
     */
    private boolean generateIds;
    /**
     * If true, a TEI is generated without attributes that would be assigned by program rule engine.
     * Events are generated without data elements assigned by program rules
     */
    private boolean skipGenerationWhenAssignedByProgramRules;

    /**
     * if true, a TEI is not generated when creating a new random event
     */
    private boolean skipTeiInEvent;

    /**
     * if true, a TEI reference is not used when creating a new ranndom enrollment
     */
    private boolean skipTeiInEnrollment;
}
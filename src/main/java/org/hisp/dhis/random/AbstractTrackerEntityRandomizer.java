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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;

import com.github.javafaker.Faker;
import org.hisp.dhis.cache.EntitiesCache;
import org.hisp.dhis.cache.Program;
import org.hisp.dhis.cache.ProgramStage;
import org.hisp.dhis.common.ValueType;
import org.hisp.dhis.utils.DataRandomizer;

/**
 * @author Luciano Fiandesio
 */
public abstract class AbstractTrackerEntityRandomizer<T>
    implements
    DhisEntityRandomizer<T>
{
    protected DateFormat DEFAULT_DATEFORMAT = new SimpleDateFormat( "yyyy-MM-dd" );

    protected Program getProgramFromContextOrRnd( RandomizerContext ctx, EntitiesCache cache )
    {
        Program program;
        if ( ctx.getProgram() == null )
        {
            program = getRandomProgram( cache );
            ctx.setProgram( program );
        }
        else
        {
            program = ctx.getProgram();
        }
        return program;
    }

    protected String getOrgUnitFromContextOrRndFromProgram( RandomizerContext ctx, Program program ) {
        if (ctx.getOrgUnitUid() == null) {
            return getRandomOrgUnitFromProgram( program );
        }

        String uid = ctx.getOrgUnitUid();
        ctx.setOrgUnitUid( uid );

        return uid;
    }

    private Program getRandomProgram( EntitiesCache cache )
    {
        return DataRandomizer.randomElementFromList( cache.getTrackerPrograms() );
    }

    protected String getRandomOrgUnitFromProgram( Program program )
    {
        return DataRandomizer.randomElementFromList( program.getOrgUnits() );
    }

    protected ProgramStage getProgramStageFromProgram(Program program )
    {
        return DataRandomizer.randomElementFromList( program.getStages() );
    }

    protected String rndValueFrom( ValueType valueType )
    {
       return new DataValueRandomizer().rndValueFrom( valueType );
    }

}

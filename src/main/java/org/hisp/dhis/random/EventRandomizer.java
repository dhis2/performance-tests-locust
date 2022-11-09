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

import org.hisp.dhis.cache.EntitiesCache;
import org.hisp.dhis.cache.Program;
import org.hisp.dhis.cache.ProgramStage;
import org.hisp.dhis.dxf2.events.event.Event;
import org.hisp.dhis.event.EventStatus;
import org.hisp.dhis.utils.Randomizer;
import org.hisp.dhis.utils.UidGenerator;

import java.util.Date;

/**
 * @author Luciano Fiandesio
 */
public class EventRandomizer
    extends
    AbstractTrackerEntityRandomizer<Event>
{
    public EventRandomizer(Randomizer rnd) {
        super( rnd );
    }

    public Event createWithoutDataValues(EntitiesCache cache, RandomizerContext ctx )
    {
        if ( ctx.getProgram() == null )
        {
            ctx.setProgram( rnd.randomElementFromList( cache.getProgramsWithAtLeastOneRepeatableStage() ) );
        }

        if ( ctx.getProgramStage() == null )
        {
            ctx.setProgramStage( getRandomProgramStageFromProgram( ctx.getProgram() ) );
        }

        String orgUnitUid = getOrgUnitFromContextOrRndFromProgram( ctx, ctx.getProgram() );

        Event event = new Event();
        if ( ctx.isGenerateIds() ) {
            event.setEvent( UidGenerator.generateUid() );
        }
        event.setStoredBy( "performance-test" );
        event.setEnrollment( ctx.getEnrollmentId() );
        event.setDueDate( simpleDateFormat.format( new Date() ) );
        event.setProgram( ctx.getProgram().getId() );
        event.setProgramStage( ctx.getProgramStage().getId() );
        event.setOrgUnit( orgUnitUid );
        event.setStatus( EventStatus.ACTIVE );
        event.setEventDate( simpleDateFormat.format( new Date() ) );
        event.setFollowup( false );
        event.setDeleted( false );
        event.setAttributeOptionCombo( "" ); // TODO

        if ( !ctx.isSkipTeiInEvent() && ctx.getProgram().isHasRegistration() )
        {
            if ( ctx.getTeiId() != null )
            {
                event.setTrackedEntityInstance( ctx.getTeiId() );
                return event;
            }

            if ( cache.getTeis().get( ctx.getProgram().getId() ) != null )
            {
                String teiUid = rnd.randomElementFromList( cache.getTeis().get( ctx.getProgram().getId() ) )
                    .getUid();
                event.setTrackedEntityInstance( teiUid );
            }

            else
            {
                return null;
            }
        }

        return event;
    }

    @Override
    public Event create( EntitiesCache cache, RandomizerContext ctx )
    {
        Event event = createWithoutDataValues( cache, ctx );

        event.setDataValues( new EventDataValueRandomizer(rnd).create( cache, ctx ) );

        return event;
    }

    private ProgramStage getRandomProgramStageFromProgram( Program program )
    {
        return rnd.randomElementFromList(
            program.getProgramStages() );
    }
}

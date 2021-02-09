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

import org.apache.commons.collections.set.ListOrderedSet;
import org.hisp.dhis.cache.DataElement;
import org.hisp.dhis.cache.EntitiesCache;
import org.hisp.dhis.cache.Program;
import org.hisp.dhis.cache.ProgramStage;
import org.hisp.dhis.dxf2.events.event.DataValue;
import org.hisp.dhis.dxf2.events.event.Event;
import org.hisp.dhis.event.EventStatus;
import org.hisp.dhis.utils.DataRandomizer;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Luciano Fiandesio
 */
public class EventRandomizer
    extends
    AbstractTrackerEntityRandomizer<Event>
{
    public Event createWithoutDataValues( EntitiesCache cache, RandomizerContext ctx )
    {
        Program program = ctx.getProgram();
        if ( program == null )
        {
            program = DataRandomizer.randomElementFromList( cache.getProgramsWithAtLeastOnRepeatableStage() );
        }

        ProgramStage programStage = ctx.getProgramStage();
        if ( programStage == null )
        {
            programStage = getRepeatableRandomProgramStageFromProgram( program );
        }

        String orgUnitUid = getOrgUnitFromContextOrRndFromProgram( ctx, program );

        Event event = new Event();
        event.setEnrollment( ctx.getEnrollmentId() );
        event.setDueDate( DEFAULT_DATEFORMAT.format( new Date() ) );
        event.setProgram( program.getUid() );
        event.setProgramStage( programStage.getUid() );
        event.setOrgUnit( orgUnitUid );
        event.setStatus( EventStatus.ACTIVE );
        event.setEventDate( DEFAULT_DATEFORMAT.format( new Date() ) );
        event.setFollowup( false );
        event.setDeleted( false );
        event.setAttributeOptionCombo( "" ); // TODO

        if ( !ctx.isSkipTeiInEvent() && program.isHasRegistration() )
        {
            if ( ctx.getTeiId() != null )
            {
                event.setTrackedEntityInstance( ctx.getTeiId() );
                return event;
            }
            try
            {
                String teiUid = DataRandomizer.randomElementFromList( cache.getTeis().get( program.getUid() ) )
                    .getUid();
                event.setTrackedEntityInstance( teiUid );
            }
            catch ( Exception e )
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
        ProgramStage programStage = ctx.getProgramStage();

        event.setDataValues( createDataValues( programStage, 1, 8 ) );

        return event;
    }

    public ListOrderedSet createDataValues( ProgramStage programStage, int min, int max )
    {
        ListOrderedSet dataValues = new ListOrderedSet();
        int numberOfDataValuesToCreate = DataRandomizer.randomIntInRange( min, max );
        List<Integer> indexes = DataRandomizer.randomSequence( programStage.getDataElements().size(),
            numberOfDataValuesToCreate + 1 );

        for ( Integer index : indexes )
        {
            dataValues.add( withRandomValue( programStage.getDataElements().get( index ) ) );
        }

        return dataValues;
    }

    private DataValue withRandomValue( DataElement dataElement )
    {
        DataValue dataValue = new DataValue();
        dataValue.setDataElement( dataElement.getUid() );
        dataValue.setProvidedElsewhere( false );
        String val;
        if ( dataElement.getOptionSet() != null && !dataElement.getOptionSet().isEmpty() )
        {
            val = DataRandomizer.randomElementFromList( dataElement.getOptionSet() );
        }
        else
        {
            val = rndValueFrom( dataElement.getValueType() );
        }

        dataValue.setValue( val );
        return dataValue;
    }

    private ProgramStage getRepeatableRandomProgramStageFromProgram( Program program )
    {
        return DataRandomizer.randomElementFromList(
            program.getStages().stream().filter( ProgramStage::isRepeatable ).collect( Collectors.toList() ) );
    }
}

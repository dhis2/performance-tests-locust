package org.hisp.dhis.models;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.hisp.dhis.tracker.domain.Event;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Gintare Vilkelyte <vilkelyte.gintare@gmail.com>
 */
@Getter
@Setter
@Builder
public class Events
{
    private List<Event> events;

    public Events addEvent( Event event )
    {
        if ( this.events == null )
        {
            this.events = new ArrayList<>();
        }

        this.events.add( event );
        return this;
    }
}

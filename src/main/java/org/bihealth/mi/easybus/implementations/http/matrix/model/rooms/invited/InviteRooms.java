
package org.bihealth.mi.easybus.implementations.http.matrix.model.rooms.invited;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * A model for invited rooms in matrix
 * 
 * @author Felix Wirth
 *
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "events"
})

public class InviteRooms {

    @JsonProperty("events")
    private List<EventInvited> events = null;

    /**
     * No args constructor for use in serialization
     * 
     */
    public InviteRooms() {
    }

    /**
     * 
     * @param events
     */
    public InviteRooms(List<EventInvited> events) {
        super();
        this.events = events;
    }

    @JsonProperty("events")
    public List<EventInvited> getEvents() {
        return events;
    }

    @JsonProperty("events")
    public void setEvents(List<EventInvited> events) {
        this.events = events;
    }

}

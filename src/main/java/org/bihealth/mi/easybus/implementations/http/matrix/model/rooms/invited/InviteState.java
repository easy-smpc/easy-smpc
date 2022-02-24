package org.bihealth.mi.easybus.implementations.http.matrix.model.rooms.invited;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * A model for an invite state in matrix
 * 
 * @author Felix Wirth
 *
 */
@JsonPropertyOrder({ "events" })
public class InviteState {

    @JsonProperty("events")
    private List<EventInvited> events = null;

    /**
     * No args constructor for use in serialization
     *
     */
    public InviteState() {
    }

    /**
     *
     * @param events
     */
    public InviteState(List<EventInvited> events) {
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

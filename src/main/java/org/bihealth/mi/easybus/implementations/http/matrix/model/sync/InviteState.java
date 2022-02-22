package org.bihealth.mi.easybus.implementations.http.matrix.model.sync;

import java.util.List;

import org.bihealth.mi.easybus.implementations.http.matrix.model.RoomEvent;

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
    private List<RoomEvent> events = null;

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
    public InviteState(List<RoomEvent> events) {
        super();
        this.events = events;
    }

    @JsonProperty("events")
    public List<RoomEvent> getEvents() {
        return events;
    }

    @JsonProperty("events")
    public void setEvents(List<RoomEvent> events) {
        this.events = events;
    }

}

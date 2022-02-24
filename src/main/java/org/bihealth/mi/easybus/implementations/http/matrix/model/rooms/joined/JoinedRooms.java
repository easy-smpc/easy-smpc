package org.bihealth.mi.easybus.implementations.http.matrix.model.rooms.joined;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "events"
})

public class JoinedRooms {

    @JsonProperty("events")
    private List<EventJoined> events = null;

    /**
     * No args constructor for use in serialization
     * 
     */
    public JoinedRooms() {
    }

    /**
     * 
     * @param events
     */
    public JoinedRooms(List<EventJoined> events) {
        super();
        this.events = events;
    }

    @JsonProperty("events")
    public List<EventJoined> getEvents() {
        return events;
    }

    @JsonProperty("events")
    public void setEvents(List<EventJoined> events) {
        this.events = events;
    }

}

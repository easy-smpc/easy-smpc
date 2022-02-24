package org.bihealth.mi.easybus.implementations.http.matrix.model.rooms.joined;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * A model for a timeline in matrix
 * 
 * @author Felix Wirth
 *
 */
@JsonPropertyOrder({ "events" })
public class Timeline {
    
    @JsonProperty("events")
    private List<EventJoined> events = null;
    
    /**
     * No args constructor for use in serialization
     *
     */
    public Timeline() {
    }
    
    /**
    *
    * @param events
    */
   public Timeline(List<EventJoined> events) {
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

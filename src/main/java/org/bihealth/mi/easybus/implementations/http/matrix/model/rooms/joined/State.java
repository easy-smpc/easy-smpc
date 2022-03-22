package org.bihealth.mi.easybus.implementations.http.matrix.model.rooms.joined;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * A model for a state in matrix
 * 
 * @author Felix Wirth
 *
 */
public class State {
    
    
    @JsonProperty("events")
    private List<EventJoined> events = null;
    
    /**
     * No args constructor for use in serialization
     *
     */
    public State() {
    }
    
    /**
    *
    * @param events
    */
   public State(List<EventJoined> events) {
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

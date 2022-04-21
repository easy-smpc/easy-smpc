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
@JsonPropertyOrder({ "events", "prev_batch", "limited"})
public class Timeline {
    
    @JsonProperty("events")
    private List<EventJoined> events    = null;
    @JsonProperty("prev_batch")
    private String            prevBatch = null;
    @JsonProperty("limited")
    private boolean           limited;
    
    /**
     * No args constructor for use in serialization
     *
     */
    public Timeline() {
    }
    
    /**
    * Creates a new instance
    *
    * @param events
    * @param prevBatch
    * @param limited
    */
   public Timeline(List<EventJoined> events, String prevBatch, boolean limited) {
       super();
       this.events = events;
       this.prevBatch = prevBatch;
       this.limited = limited;
   }

   @JsonProperty("events")
   public List<EventJoined> getEvents() {
       return events;
   }

   @JsonProperty("events")
   public void setEvents(List<EventJoined> events) {
       this.events = events;
   }
   
   @JsonProperty("prev_batch")
   public String getPrevBatch() {
       return prevBatch;
   }

   @JsonProperty("prev_batch")
   public void setPrevBatch(String prevBatch) {
       this.prevBatch = prevBatch;
   }

   @JsonProperty("limited")
   public boolean getLimited() {
       return this.limited;
   }

   @JsonProperty("limited")
   public void setLimited(boolean limited) {
       this.limited = limited;
   }
}

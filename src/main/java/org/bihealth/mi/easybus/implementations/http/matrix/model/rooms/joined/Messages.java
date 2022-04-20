package org.bihealth.mi.easybus.implementations.http.matrix.model.rooms.joined;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * A model for messages in matrix
 * 
 * @author Felix Wirth
 *
 */
@JsonPropertyOrder({ "chunk","end"})
public class Messages {
    
    // TODO Arrange model packages by API endpoints and business objects
    
    @JsonProperty("chunk")
    private List<EventJoined> chunk = null;
    @JsonProperty("end")
    private String end = null;
    
    /**
     * No args constructor for use in serialization
     *
     */
    public Messages() {
    }
    
    /**
     * Create a new instance
     *
     * @param chunk
     * @param end
     */
   public Messages(List<EventJoined> chunk, String end) {
       super();
       this.chunk = chunk;
       this.end = end;
   }

   @JsonProperty("chunk")
   public List<EventJoined> getChunk() {
       return chunk;
   }

   @JsonProperty("chunk")
   public void setChunk(List<EventJoined> chunk) {
       this.chunk = chunk;
   }
   
   @JsonProperty("end")
   public String getEnd() {
       return end;
   }

   @JsonProperty("end")
   public void setEnd(String end) {
       this.end = end;
   }
}

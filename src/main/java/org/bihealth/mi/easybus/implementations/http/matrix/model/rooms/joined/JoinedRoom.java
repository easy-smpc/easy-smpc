package org.bihealth.mi.easybus.implementations.http.matrix.model.rooms.joined;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * A model for joined rooms in matrix
 * 
 * @author Felix Wirth
 *
 */
@JsonPropertyOrder({ "timeline" })
public class JoinedRoom {

    @JsonProperty("timeline")
    private Timeline timeline;    
    
    @JsonProperty("timeline")
    public Timeline getTimeline() {
        return timeline;
    }

    @JsonProperty("timeline")
    public void setTimeline(Timeline timeline) {
        this.timeline = timeline;
    }
}
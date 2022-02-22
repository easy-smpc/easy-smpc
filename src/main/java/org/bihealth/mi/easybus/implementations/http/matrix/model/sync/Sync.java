package org.bihealth.mi.easybus.implementations.http.matrix.model.sync;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * @author Felix Wirth
 *
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "next_batch" })
public class Sync {

    @JsonProperty("next_batch")
    private String nextBatch;
    @JsonProperty("rooms")
    private Rooms rooms;


    /**
     * No args constructor for use in serialization
     *
     */
    public Sync() {
    }

    /**
     *
     * @param nextBatch
     */
    public Sync(String nextBatch) {
        super();
        this.nextBatch = nextBatch;
    }

    @JsonProperty("next_batch")
    public String getNextBatch() {
        return nextBatch;
    }

    @JsonProperty("next_batch")
    public void setNextBatch(String nextBatch) {
        this.nextBatch = nextBatch;
    }
    
    @JsonProperty("rooms")
    public Rooms getRooms() {
    return rooms;
    }

    @JsonProperty("rooms")
    public void setRooms(Rooms rooms) {
    this.rooms = rooms;
    }
}

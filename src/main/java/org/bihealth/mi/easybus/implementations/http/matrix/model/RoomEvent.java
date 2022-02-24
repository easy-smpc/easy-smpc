package org.bihealth.mi.easybus.implementations.http.matrix.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * A model for a room event in matrix
 * 
 * @author Felix Wirth
 *
 */
@JsonPropertyOrder({ "type",
                     "state_key",
                     "content",
                     "sender",
                     "origin_server_ts",
                     "unsigned",
                     "event_id" })
public class RoomEvent {

    @JsonProperty("type")
    private String   type;
    @JsonProperty("state_key")
    private String   stateKey;
    @JsonProperty("content")
    private Content  content;
    @JsonProperty("sender")
    private String   sender;
    @JsonProperty("origin_server_ts")
    private Long     originServerTs;
    @JsonProperty("event_id")
    private String   eventId;
    @JsonProperty("unsigned")
    private Unsigned unsigned;

    /**
    * No args constructor for use in serialization
    *
    */
    public RoomEvent() {
    }

    /**
    *
    * @param originServerTs
    * @param eventId
    * @param sender
    * @param unsigned
    * @param type
    * @param content
    * @param stateKey
    */
    public RoomEvent(String type, String stateKey, Content content, String sender, Long originServerTs, Unsigned unsigned, String eventId) {
        super();
        this.type = type;
        this.stateKey = stateKey;
        this.content = content;
        this.sender = sender;
        this.originServerTs = originServerTs;
        this.unsigned = unsigned;
        this.eventId = eventId;
        }

    @JsonProperty("type")
    public String getType() {
        System.out.println("gettype!");        

            System.out.println("type:" + type);

        return type;
    }

    @JsonProperty("type")
    public void setType(String type) {
        System.out.println("settype!");
        this.type = type;
    }

    @JsonProperty("state_key")
    public String getStateKey() {
        System.out.println("state_key!");
        return stateKey;
    }

    @JsonProperty("state_key")
    public void setStateKey(String stateKey) {
        System.out.println("state_key!");
        this.stateKey = stateKey;
    }

    @JsonProperty("content")
    public Content getContent() {
        System.out.println("content!");
        return content;
    }

    @JsonProperty("content")
    public void setContent(Content content) {
        System.out.println("content!");
        this.content = content;
    }

    @JsonProperty("sender")
    public String getSender() {
        System.out.println("sender!");
        return sender;
    }

    @JsonProperty("sender")
    public void setSender(String sender) {
        System.out.println("sender!");
        this.sender = sender;
    }

    @JsonProperty("origin_server_ts")
    public Long getOriginServerTs() {
        System.out.println("origin_server_ts!");
        return originServerTs;
    }

    @JsonProperty("origin_server_ts")
    public void setOriginServerTs(Long originServerTs) {
        System.out.println("origin_server_ts!");
        this.originServerTs = originServerTs;
    }


    @JsonProperty("event_id")
    public String getEventId() {
        System.out.println("event_id!");
        return eventId;
    }

    @JsonProperty("event_id")
    public void setEventId(String eventId) {
        System.out.println("event_id!");
        this.eventId = eventId;
    }
}

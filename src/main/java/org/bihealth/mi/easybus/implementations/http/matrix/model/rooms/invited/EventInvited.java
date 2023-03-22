
package org.bihealth.mi.easybus.implementations.http.matrix.model.rooms.invited;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 *  model to for an event of an invited room in matrix
 * 
 * @author Felix Wirth
 *
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "type",
    "state_key",
    "content",
    "sender",
    "origin_server_ts",
    "unsigned",
    "event_id"
})

public class EventInvited {

    @JsonProperty("type")
    private String type;
    @JsonProperty("state_key")
    private String stateKey;
    @JsonProperty("content")
    private Content content;
    @JsonProperty("sender")
    private String sender;
    @JsonProperty("origin_server_ts")
    private Long originServerTs;
    @JsonProperty("unsigned")
    private Unsigned unsigned;
    @JsonProperty("event_id")
    private String eventId;

    /**
     * No args constructor for use in serialization
     * 
     */
    public EventInvited() {
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
    public EventInvited(String type, String stateKey, Content content, String sender, Long originServerTs, Unsigned unsigned, String eventId) {
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
        return type;
    }

    @JsonProperty("type")
    public void setType(String type) {
        this.type = type;
    }

    @JsonProperty("state_key")
    public String getStateKey() {
        return stateKey;
    }

    @JsonProperty("state_key")
    public void setStateKey(String stateKey) {
        this.stateKey = stateKey;
    }

    @JsonProperty("content")
    public Content getContent() {
        return content;
    }

    @JsonProperty("content")
    public void setContent(Content content) {
        this.content = content;
    }

    @JsonProperty("sender")
    public String getSender() {
        return sender;
    }

    @JsonProperty("sender")
    public void setSender(String sender) {
        this.sender = sender;
    }

    @JsonProperty("origin_server_ts")
    public Long getOriginServerTs() {
        return originServerTs;
    }

    @JsonProperty("origin_server_ts")
    public void setOriginServerTs(Long originServerTs) {
        this.originServerTs = originServerTs;
    }

    @JsonProperty("unsigned")
    public Unsigned getUnsigned() {
        return unsigned;
    }

    @JsonProperty("unsigned")
    public void setUnsigned(Unsigned unsigned) {
        this.unsigned = unsigned;
    }

    @JsonProperty("event_id")
    public String getEventId() {
        return eventId;
    }

    @JsonProperty("event_id")
    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

}

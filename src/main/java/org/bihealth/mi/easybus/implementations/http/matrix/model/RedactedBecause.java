package org.bihealth.mi.easybus.implementations.http.matrix.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({ "content",
                     "origin_server_ts",
                     "redacts",
                     "sender",
                     "type",
                     "unsigned",
                     "event_id" })
public class RedactedBecause {

    @JsonProperty("content")
    private Content  content;
    @JsonProperty("origin_server_ts")
    private Long     originServerTs;
    @JsonProperty("redacts")
    private String   redacts;
    @JsonProperty("sender")
    private String   sender;
    @JsonProperty("type")
    private String   type;
    @JsonProperty("unsigned")
    private Unsigned unsigned;
    @JsonProperty("event_id")
    private String   eventId;

    /**
     * No args constructor for use in serialization
     *
     */
    public RedactedBecause() {
    }

    /**
     *
     * @param originServerTs
     * @param eventId
     * @param sender
     * @param unsigned
     * @param redacts
     * @param type
     * @param content
     */
    public RedactedBecause(Content content,
                           Long originServerTs,
                           String redacts,
                           String sender,
                           String type,
                           Unsigned unsigned,
                           String eventId) {
        super();
        this.content = content;
        this.originServerTs = originServerTs;
        this.redacts = redacts;
        this.sender = sender;
        this.type = type;
        this.unsigned = unsigned;
        this.eventId = eventId;
    }

    @JsonProperty("content")
    public Content getContent() {
        return content;
    }

    @JsonProperty("content")
    public void setContent(Content content) {
        this.content = content;
    }

    @JsonProperty("origin_server_ts")
    public Long getOriginServerTs() {
        return originServerTs;
    }

    @JsonProperty("origin_server_ts")
    public void setOriginServerTs(Long originServerTs) {
        this.originServerTs = originServerTs;
    }

    @JsonProperty("redacts")
    public String getRedacts() {
        return redacts;
    }

    @JsonProperty("redacts")
    public void setRedacts(String redacts) {
        this.redacts = redacts;
    }

    @JsonProperty("sender")
    public String getSender() {
        return sender;
    }

    @JsonProperty("sender")
    public void setSender(String sender) {
        this.sender = sender;
    }

    @JsonProperty("type")
    public String getType() {
        return type;
    }

    @JsonProperty("type")
    public void setType(String type) {
        this.type = type;
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

package org.bihealth.mi.easybus.implementations.http.matrix.model.rooms.joined;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * A model to create a room in matrix
 * 
 * @author Felix Wirth
 *
 */
public class CreateMessage {
    
    /** Enum for access presets (see matrix documentation) */
    public enum MESSAGE_TYPE {
                        @JsonProperty("m.text")
                        M_TEXT
    };
    
    /** Type of message, so far only m.text is supported */
    @JsonProperty("msgtype")
    private final MESSAGE_TYPE msgtype = MESSAGE_TYPE.M_TEXT;
    /** Preset of room */
    @JsonProperty("body")
    private String body;
    @JsonProperty("org.bihealth.mi.scope")
    private String scope;
    
    /**
     * @return the body
     */
    @JsonProperty("body")
    public String getBody() {
        return body;
    }
    /**
     * @param body the body to set
     * @return 
     */
    @JsonProperty("body")
    public CreateMessage setBody(String body) {
        this.body = body;
        return this;
    }
    
    /**
     * @return the msgtype
     */
    @JsonProperty("msgtype")
    public MESSAGE_TYPE getMsgtype() {
        return msgtype;
    }
    
    /**
     * @return the scope
     */
    @JsonProperty("org.bihealth.mi.scope")
    public String getScope() {
        return scope;
    }

    /**
     * @param scope the scope to set
     * @return 
     */
    @JsonProperty("org.bihealth.mi.scope")
    public CreateMessage setScope(String scope) {
        this.scope = scope;
        return this;
    }    
}

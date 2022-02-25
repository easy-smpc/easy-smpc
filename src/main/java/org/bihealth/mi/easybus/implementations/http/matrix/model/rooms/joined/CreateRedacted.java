
package org.bihealth.mi.easybus.implementations.http.matrix.model.rooms.joined;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * A model to reduct an even in a matrix model
 * 
 * @author Felix Wirth
 *
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "reason"
})

public class CreateRedacted {

    @JsonProperty("reason")
    private String reason;

    /**
     * No args constructor for use in serialization
     * 
     */
    public CreateRedacted() {
    }

    /**
     * 
     * @param reason
     */
    public CreateRedacted(String reason) {
        super();
        this.reason = reason;
    }

    @JsonProperty("reason")
    public String getReason() {
        return reason;
    }

    @JsonProperty("reason")
    public void setReason(String reason) {
        this.reason = reason;
    }

}

package org.bihealth.mi.easybus.implementations.http.matrix.model.sync;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * A model for invites in matrix
 * 
 * @author Felix Wirth
 *
 */
@JsonPropertyOrder({ "invites" })
public class Invite {

    @JsonProperty("invites")
    private Map<String, Invitation> invites;

    /**
     * No args constructor for use in serialization
     *
     */
    public Invite() {
    }

    /**
     * @return the invites
     */
    public Map<String, Invitation> getInvites() {
        return invites;
    }

    /**
     * @param invites
     *            the invites to set
     */
    public void setInvites(Map<String, Invitation> invites) {
        this.invites = invites;
    }

}

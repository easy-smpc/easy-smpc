package org.bihealth.mi.easybus.implementations.http.matrix.model.sync;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * A model for rooms in matrix
 * 
 * @author Felix Wirth
 *
 */
@JsonPropertyOrder({ "invite" })
public class Rooms {

    @JsonProperty("invite")
    private Invite invite;

    /**
     * No args constructor for use in serialization
     *
     */
    public Rooms() {
    }

    /**
     *
     * @param invite
     */
    public Rooms(Invite invite) {
        super();
        this.invite = invite;
    }

    @JsonProperty("invite")
    public Invite getInvite() {
        return invite;
    }

    @JsonProperty("invite")
    public void setInvite(Invite invite) {
        this.invite = invite;
    }

}

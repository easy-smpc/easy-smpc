package org.bihealth.mi.easybus.implementations.http.matrix.model.rooms.invited;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * A model for a room invitation in matrix
 * 
 * @author Felix Wirth
 *
 */
@JsonPropertyOrder({ "invite_state" })
public class Invitation {

    @JsonProperty("invite_state")
    private InviteState inviteState;

    @JsonProperty("invite_state")
    public InviteState getInviteState() {
        return inviteState;
    }

    @JsonProperty("invite_state")
    public void setInviteState(InviteState inviteState) {
        this.inviteState = inviteState;
    }
}

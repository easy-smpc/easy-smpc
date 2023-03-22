package org.bihealth.mi.easybus.implementations.http.matrix.model;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * A model to create a room in matrix
 * 
 * @author Felix Wirth
 *
 */
public class CreateRoom {
    
    /** Enum for access presets (see matrix documentation) */
    public enum PRESET {
                        @JsonProperty("private_chat")
                        PRIVATE_CHAT,
                        @JsonProperty("trusted_private_chat")
                        TRUSTED_PRIVATE_CHAT,
                        @JsonProperty("public_chat")
                        PUBLIC_CHAT
    };

    /** Enum for access presets (see matrix documentation) */
    public enum VISIBILITY {
                            @JsonProperty("private")
                            PRIVATE,
                            @JsonProperty("public")
                            PUBLIC
    };
    
    /** Name of room */
    @JsonProperty("name")
    private String name;
    /** Preset of room */
    @JsonProperty("preset")
    private PRESET preset = PRESET.TRUSTED_PRIVATE_CHAT;
    /** Visibility of room */
    @JsonProperty("visibility")
    private VISIBILITY visibility = VISIBILITY.PRIVATE;
    /** Invited users*/
    @JsonProperty("invite")
    private List<String> invite = new ArrayList<>();
    
    /**
     * @return the name
     */
    @JsonProperty("name")
    public String getName() {
        return name;
    }
    /**
     * @param name the name to set
     */
    @JsonProperty("name")
    public CreateRoom setName(String name) {
        this.name = name;
        return this;
    }
    /**
     * @return the preset
     */
    @JsonProperty("preset")
    public PRESET getPreset() {
        return preset;
    }
    /**
     * @param preset the preset to set
     */
    @JsonProperty("preset")
    public CreateRoom setPreset(PRESET preset) {
        this.preset = preset;
        return this;
    }
    /**
     * @return the visibility
     */
    @JsonProperty("visibility")
    public VISIBILITY getVisibility() {
        return visibility;
    }
    /**
     * @param visibility the visibility to set
     * @return 
     */
    @JsonProperty("visibility")
    public CreateRoom setVisibility(VISIBILITY visibility) {
        this.visibility = visibility;
        return this;
    }
    /**
     * @return the invite
     */
    @JsonProperty("invite")
    public List<String> getInvite() {
        return invite;
    }
    /**
     * @param invite the invite to set
     * @return 
     */
    @JsonProperty("invite")
    public CreateRoom setInvite(List<String> invite) {
        this.invite = invite;
        return this;
    }
    
    /**
     * Adds a an mxid to the invite list
     * 
     * @param inviteMXID
     * @return
     */
    public CreateRoom addInvite(String inviteMXID) {
        this.invite.add(inviteMXID);
        return this;
    }
}

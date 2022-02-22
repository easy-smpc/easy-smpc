package org.bihealth.mi.easybus.implementations.http.matrix.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * A model for a room events's content in matrix
 * 
 * @author Felix Wirth
 *
 */
@JsonPropertyOrder({ "creator",
                     "room_version",
                     "algorithm",
                     "join_rule",
                     "displayname",
                     "membership",
                     "name" })
public class Content {

    @JsonProperty("creator")
    private String creator;
    @JsonProperty("room_version")
    private String roomVersion;
    @JsonProperty("algorithm")
    private String algorithm;
    @JsonProperty("join_rule")
    private String joinRule;
    @JsonProperty("displayname")
    private String displayname;
    @JsonProperty("membership")
    private String membership;
    @JsonProperty("name")
    private String name;

    /**
     * No args constructor for use in serialization
     *
     */
    public Content() {
    }

    /**
     *
     * @param creator
     * @param roomVersion
     * @param joinRule
     * @param displayname
     * @param name
     * @param membership
     * @param algorithm
     */
    public Content(String creator,
                   String roomVersion,
                   String algorithm,
                   String joinRule,
                   String displayname,
                   String membership,
                   String name) {
        super();
        this.creator = creator;
        this.roomVersion = roomVersion;
        this.algorithm = algorithm;
        this.joinRule = joinRule;
        this.displayname = displayname;
        this.membership = membership;
        this.name = name;
    }

    @JsonProperty("creator")
    public String getCreator() {
        return creator;
    }

    @JsonProperty("creator")
    public void setCreator(String creator) {
        this.creator = creator;
    }

    @JsonProperty("room_version")
    public String getRoomVersion() {
        return roomVersion;
    }

    @JsonProperty("room_version")
    public void setRoomVersion(String roomVersion) {
        this.roomVersion = roomVersion;
    }

    @JsonProperty("algorithm")
    public String getAlgorithm() {
        return algorithm;
    }

    @JsonProperty("algorithm")
    public void setAlgorithm(String algorithm) {
        this.algorithm = algorithm;
    }

    @JsonProperty("join_rule")
    public String getJoinRule() {
        return joinRule;
    }

    @JsonProperty("join_rule")
    public void setJoinRule(String joinRule) {
        this.joinRule = joinRule;
    }

    @JsonProperty("displayname")
    public String getDisplayname() {
        return displayname;
    }

    @JsonProperty("displayname")
    public void setDisplayname(String displayname) {
        this.displayname = displayname;
    }

    @JsonProperty("membership")
    public String getMembership() {
        return membership;
    }

    @JsonProperty("membership")
    public void setMembership(String membership) {
        this.membership = membership;
    }

    @JsonProperty("name")
    public String getName() {
        return name;
    }

    @JsonProperty("name")
    public void setName(String name) {
        this.name = name;
    }
}

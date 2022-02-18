package org.bihealth.mi.easybus.implementations.http.matrix.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "user_id", "access_token", "home_server", "device_id", "well_known" })
public class LoggedIn {

    @JsonProperty("user_id")
    private String    userId;
    @JsonProperty("access_token")
    private String    accessToken;
    @JsonProperty("home_server")
    private String    homeServer;
    @JsonProperty("device_id")
    private String    deviceId;
    @JsonProperty("well_known")
    private WellKnown wellKnown;

    /**
     * No args constructor for use in serialization
     *
     */
    public LoggedIn() {
    }

    /**
     *
     * @param wellKnown
     * @param accessToken
     * @param homeServer
     * @param userId
     * @param deviceId
     */
    public LoggedIn(String userId,
                    String accessToken,
                    String homeServer,
                    String deviceId,
                    WellKnown wellKnown) {
        super();
        this.userId = userId;
        this.accessToken = accessToken;
        this.homeServer = homeServer;
        this.deviceId = deviceId;
        this.wellKnown = wellKnown;
    }

    @JsonProperty("user_id")
    public String getUserId() {
        return userId;
    }

    @JsonProperty("user_id")
    public void setUserId(String userId) {
        this.userId = userId;
    }

    @JsonProperty("access_token")
    public String getAccessToken() {
        return accessToken;
    }

    @JsonProperty("access_token")
    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    @JsonProperty("home_server")
    public String getHomeServer() {
        return homeServer;
    }

    @JsonProperty("home_server")
    public void setHomeServer(String homeServer) {
        this.homeServer = homeServer;
    }

    @JsonProperty("device_id")
    public String getDeviceId() {
        return deviceId;
    }

    @JsonProperty("device_id")
    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    @JsonProperty("well_known")
    public WellKnown getWellKnown() {
        return wellKnown;
    }

    @JsonProperty("well_known")
    public void setWellKnown(WellKnown wellKnown) {
        this.wellKnown = wellKnown;
    }

}

package org.bihealth.mi.easybus.implementations.http.matrix.model;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * A model for the well-known data in matrix
 * 
 * @author Felix Wirth
 *
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "m.homeserver", "m.identity_server", "org.example.custom.property" })
public class WellKnown {

    @JsonProperty("m.homeserver")
    private MHomeserver              mHomeserver;
    @JsonProperty("m.identity_server")
    private MIdentityServer          mIdentityServer;
    @JsonProperty("org.example.custom.property")
    private OrgExampleCustomProperty orgExampleCustomProperty;
    @JsonIgnore
    private Map<String, Object>      additionalProperties = new HashMap<String, Object>();

    /**
     * No args constructor for use in serialization
     *
     */
    public WellKnown() {
    }

    /**
     *
     * @param mIdentityServer
     * @param orgExampleCustomProperty
     * @param mHomeserver
     */
    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    public WellKnown(@JsonProperty("m.homeserver") MHomeserver mHomeserver,
                     @JsonProperty("m.identity_server") MIdentityServer mIdentityServer,
                     @JsonProperty("org.example.custom.property") OrgExampleCustomProperty orgExampleCustomProperty) {
        super();
        this.mHomeserver = mHomeserver;
        this.mIdentityServer = mIdentityServer;
        this.orgExampleCustomProperty = orgExampleCustomProperty;
    }

    @JsonProperty("m.homeserver")
    public MHomeserver getmHomeserver() {
        return mHomeserver;
    }

    @JsonProperty("m.identity_server")
    public MIdentityServer getmIdentityServer() {
        return mIdentityServer;
    }

    @JsonProperty("org.example.custom.property")
    public OrgExampleCustomProperty getOrgExampleCustomProperty() {
        return orgExampleCustomProperty;
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonPropertyOrder({ "base_url" })
    public static class MHomeserver {

        @JsonProperty("base_url")
        private String              baseUrl;
        @JsonIgnore
        private Map<String, Object> additionalProperties = new HashMap<String, Object>();

        /**
         * No args constructor for use in serialization
         *
         */
        public MHomeserver() {
        }

        /**
         *
         * @param baseUrl
         */
        public MHomeserver(String baseUrl) {
            super();
            this.baseUrl = baseUrl;
        }

        @JsonProperty("base_url")
        public String getBaseUrl() {
            return baseUrl;
        }

        @JsonProperty("base_url")
        public void setBaseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
        }

        @JsonAnyGetter
        public Map<String, Object> getAdditionalProperties() {
            return this.additionalProperties;
        }

        @JsonAnySetter
        public void setAdditionalProperty(String name, Object value) {
            this.additionalProperties.put(name, value);
        }

    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonPropertyOrder({ "base_url" })
    public static class MIdentityServer {

        @JsonProperty("base_url")
        private String              baseUrl;
        @JsonIgnore
        private Map<String, Object> additionalProperties = new HashMap<String, Object>();

        /**
         * No args constructor for use in serialization
         *
         */
        public MIdentityServer() {
        }

        /**
         *
         * @param baseUrl
         */
        public MIdentityServer(String baseUrl) {
            super();
            this.baseUrl = baseUrl;
        }

        @JsonProperty("base_url")
        public String getBaseUrl() {
            return baseUrl;
        }

        @JsonProperty("base_url")
        public void setBaseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
        }

        @JsonAnyGetter
        public Map<String, Object> getAdditionalProperties() {
            return this.additionalProperties;
        }

        @JsonAnySetter
        public void setAdditionalProperty(String name, Object value) {
            this.additionalProperties.put(name, value);
        }

    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonPropertyOrder({ "app_url" })
    public static class OrgExampleCustomProperty {

        @JsonProperty("app_url")
        private String              appUrl;
        @JsonIgnore
        private Map<String, Object> additionalProperties = new HashMap<String, Object>();

        /**
         * No args constructor for use in serialization
         *
         */
        public OrgExampleCustomProperty() {
        }

        /**
         *
         * @param appUrl
         */
        public OrgExampleCustomProperty(String appUrl) {
            super();
            this.appUrl = appUrl;
        }

        @JsonProperty("app_url")
        public String getAppUrl() {
            return appUrl;
        }

        @JsonProperty("app_url")
        public void setAppUrl(String appUrl) {
            this.appUrl = appUrl;
        }

        @JsonAnyGetter
        public Map<String, Object> getAdditionalProperties() {
            return this.additionalProperties;
        }
    }
}

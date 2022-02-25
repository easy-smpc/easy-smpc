package org.bihealth.mi.easybus.implementations.http.matrix.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Authentification model
 * 
 * @author Felix Wirth
 *
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "type", "identifier", "password" })
public class AuthentificationUserPassword {
    
    @JsonProperty("type")
    private final String     type = "m.login.password";
    @JsonProperty("identifier")
    private final Identifier identifier;
    @JsonProperty("password")
    private final String     password;

    /**
     *
     * @param identifier
     * @param password
     * @param type
     */
    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    public AuthentificationUserPassword(@JsonProperty("identifier") Identifier identifier,
                                        @JsonProperty("password") String password) {
        super();
        this.identifier = identifier;
        this.password = password;
    }

    @JsonProperty("type")
    public String getType() {
        return type;
    }

    @JsonProperty("identifier")
    public Identifier getIdentifier() {
        return identifier;
    }

    @JsonProperty("password")
    public String getPassword() {
        return password;
    }

    /**
     * Identifier model
     * 
     * @author Felix Wirth
     *
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonPropertyOrder({ "type", "user" })
    public static class Identifier {

        @JsonProperty("type")
        private final String type = "m.id.user";
        @JsonProperty("user")
        private final String user;
        
        /**
         *
         * @param type
         * @param user
         */
        @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
        public Identifier(@JsonProperty("user") String user) {
            super();
            this.user = user;
        }

        @JsonProperty("type")
        public String getType() {
            return type;
        }

        @JsonProperty("user")
        public String getUser() {
            return user;
        }
    }
}

package org.bihealth.mi.easybus.implementations.http.matrix;

import java.net.URI;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;

import org.bihealth.mi.easybus.BusException;
import org.bihealth.mi.easybus.Participant;
import org.bihealth.mi.easybus.implementations.http.matrix.model.AuthentificationUserPassword;
import org.bihealth.mi.easybus.implementations.http.matrix.model.AuthentificationUserPassword.Identifier;
import org.bihealth.mi.easybus.implementations.http.matrix.model.Error;
import org.bihealth.mi.easybus.implementations.http.matrix.model.LoggedIn;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.Invocation.Builder;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;;

/**
 * Connection to matrix server
 * 
 * @author Felix Wirth
 *
 */
public class ConnectionMatrix implements AuthHandler {
    
    /**
     * Default error handler for matrix connections
     */
    public static Function<Response, String> DEFAULT_ERROR_HANDLER = new Function<>() {

        @Override
        public String apply(Response response) {
            // Init
            String body = null;
            String result = String.format("HTTP return code %s", response.getStatus());

            // Try to get body text raw
            try {
                body = response.readEntity(String.class);
                result = String.format("HTTP return code %s and body %s", response.getStatus(), body);
            } catch (Exception e) {
                // Body can not be added
                return result;
            }
            
            // Try to get object from body text
            if (body != null) {
                try {
                    Error error = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false).readValue(body, Error.class);
                    result = String.format("HTTP return code %s with matrix code %s and message %s", response.getStatus(), error.getErrcode(), error.getError());
                } catch (Exception e) {
                    // Ignore
            } }

            // Return
            return result;
        }        
    };
    
    /** Path to authorize */
    private static final String              AUTHENTICATE_PATH = "_matrix/client/r0/login";
    /** Own participant */
    private final Participant                self;
    /** REST client */
    private final Client                     client;
    /** URI of server */
    private final URI                        server;
    /** Auth data */
    private AuthentificationUserPassword     auth;
    /** Logged in data */
    private LoggedIn                         loggedIn;

    /**
     * Creates a new instance
     * 
     * @param server - server URI
     * @param self - participant of own identity. Participants identifier will also be the user name for the matrix server
     * @param password
     */
    public ConnectionMatrix(URI server, Participant self, String password) throws IllegalStateException {
        // Check
        if (server == null || self == null || password == null) {
            throw new IllegalArgumentException("All parameters must not be null!");
        }        
        
        // Store
        this.self = self;
        this.server = server;
        this.client = ClientBuilder.newClient();
        this.auth = new AuthentificationUserPassword(new Identifier(getSelf().getIdentifier()), password);
        
        // Check config and throw exception if wrong
        try {
            authenticate(null);
        } catch (BusException e) {
            throw new IllegalStateException("Unable to authenticate!", e);
        }            
    }

    /**
     * Returns own participant
     * 
     * @return
     */
    public Participant getSelf() {
        return self;
    }


    /**
     * Returns a new builder for the path provided
     * 
     * @param pathParameters - only path and query part will be used  
     * @return
     */
    public Builder getBuilder(String path) {
        return getBuilder(path, null);
    }
    
    /**
     * Returns a new builder for path and query provided
     * 
     * @param path
     * @param parameters
     * @return
     */
    public Builder getBuilder(String path, Map<String, String> parameters) {
        
        // Set path
        WebTarget target = client.target(server).path(path);

        // Set parameters
        if (parameters != null && !parameters.isEmpty()) {
            for (Entry<String, String> parameter : parameters.entrySet()) {
                target = target.queryParam(parameter.getKey(), parameter.getValue());
            }
        }       

        // Set auth and return
        return target.request().header("Authorization", String.format("Bearer %s", this.loggedIn.getAccessToken()));
    }

    @Override
    public Builder authenticate(Builder builder) throws BusException {
        // Prepare
        WebTarget target = client.target(server).path(AUTHENTICATE_PATH);
        ObjectMapper mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        
        // Try to authenticate and obtain data
        try {
            // Execute request
            Response response =  target.request().post(Entity.entity(mapper.writeValueAsString(auth), MediaType.TEXT_PLAIN));
            
            // Check code
            if(response.getStatus() != 200) {
                throw new BusException(DEFAULT_ERROR_HANDLER.apply(response));
            }
            
            // Make and store object out of string
            setloggedIn(mapper.readValue(response.readEntity(String.class), LoggedIn.class));           
                        
        } catch (Exception e) {
            throw new BusException("Unable to execute authentification request", e);
        }
        
        // Remove old authorization, add new and return
        return builder == null ? null
                : builder.header("Authorization", null)
                         .header("Authorization",
                                 String.format("Bearer %s", this.loggedIn.getAccessToken()));
    }
    
    /**
     * Set loggedIn object
     * 
     * @param loggedIn
     */
    private synchronized void setloggedIn(LoggedIn loggedIn) {
        this.loggedIn = loggedIn;
    }
}

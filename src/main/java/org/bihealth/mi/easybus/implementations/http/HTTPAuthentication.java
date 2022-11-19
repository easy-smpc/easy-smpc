package org.bihealth.mi.easybus.implementations.http;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import org.bihealth.mi.easybus.implementations.http.easybackend.ConnectionSettingsEasyBackend;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.Form;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

/**
 * Information needed for authentication
 * @author Fabian Prasser
 * @author Felix Wirth
 */
public class HTTPAuthentication {
    
    /** Path to authorize */
    private static final String AUTHENTICATE_TEMPLATE = "/auth/realms/%s/protocol/openid-connect/token";
    /** Parameter*/
    protected URI authEndpoint;
    /** Parameter*/
    protected String username;
    /** Parameter*/
    protected String password;
    /** Parameter*/
    protected String clientId;
    /** Parameter*/
    protected String clientSecret;
    /** Parameter*/
    protected String scope;
    /** Parameter*/
    protected String grantType;
    
    /**
     * Creates a new instance
     */
    public HTTPAuthentication() {
        // Empty by design
    }
    
    /**
     * Creates a new instance 
     * @param settings
     */
    public HTTPAuthentication(ConnectionSettingsEasyBackend settings) {
        
        try {
            this.authEndpoint = new URL(settings.getAuthServer().getProtocol(),
                                        settings.getAuthServer().getHost(),
                                        settings.getAuthServer().getPort(),
                                        String.format(AUTHENTICATE_TEMPLATE, settings.getRealm()))
                                                                                                  .toURI();
        } catch (URISyntaxException | MalformedURLException e) {
            throw new IllegalStateException("Auth server URI is incorrect");
        }
        
        this.username = settings.getSelf().getEmailAddress();
        this.clientId = settings.getClientId();
        this.clientSecret = settings.getClientSecret();
        this.grantType = "password";
        this.password = settings.getPassword();
        this.scope = "openid";
    }
    
    /**
     * @param authEndpoint the authEndpoint to set
     */
    public HTTPAuthentication setAuthEndpoint(URI authEndpoint) {
        this.authEndpoint = authEndpoint;
        return this;
    }
    /**
     * @param username the username to set
     */
    public HTTPAuthentication setUsername(String username) {
        this.username = username;
        return this;
    }
    /**
     * @param password the password to set
     */
    public HTTPAuthentication setPassword(String password) {
        this.password = password;
        return this;
    }
    /**
     * @param clientId the clientId to set
     */
    public HTTPAuthentication setClientId(String clientId) {
        this.clientId = clientId;
        return this;
    }
    /**
     * @param clientSecret the clientSecret to set
     */
    public HTTPAuthentication setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
        return this;
    }
    /**
     * @param scope the scope to set
     */
    public HTTPAuthentication setScope(String scope) {
        this.scope = scope;
        return this;
    }
    /**
     * @param grantType the grantType to set
     */
    public HTTPAuthentication setGrantType(String grantType) {
        this.grantType = grantType;
        return this;
    }
    
    /**
     * Returns an authentication token
     * @return
     */
    public String authenticate() throws HTTPException {
        
        // Check
        if (authEndpoint == null || username == null || password == null || clientId == null ||
            scope == null || grantType == null) {
            throw new NullPointerException("All parameters but client secret must not be null!");
        }        
        
        
        // Create config and recorder
        Form authForm = new Form().param("username", username)
                                  .param("password", password)
                                  .param("client_id", clientId)
                                  .param("scope", scope)
                                  .param("grant_type", grantType);
        if(clientSecret != null) {
            authForm = authForm.param("client_secret", clientSecret);
        }
        
        // Prepare
        WebTarget target = ClientBuilder.newClient().target(authEndpoint);
        
        // Try to authenticate and obtain data
        try {
            // Execute request
            Response response =  target.request(MediaType.APPLICATION_FORM_URLENCODED).post(Entity.form(authForm));
            
            // Check code
            if(response.getStatus() != 200) {
                HTTPUtil.raiseException(response);
            }
            
            // Store access token
            return new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false).reader().readTree(response.readEntity(String.class)).get("access_token").asText();
            
        } catch (Exception e) {
            throw new HTTPException("Unable to execute authentification request", e);
        }
    }
}

/* 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.bihealth.mi.easybus.implementations.http.easybackend;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URI;
import java.net.URL;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;

import org.bihealth.mi.easybus.BusException;
import org.bihealth.mi.easybus.Participant;
import org.bihealth.mi.easybus.implementations.http.AuthHandler;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.HttpUrlConnectorProvider;
import org.glassfish.jersey.client.HttpUrlConnectorProvider.ConnectionFactory;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.Invocation.Builder;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.Form;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

/**
 *  Connection to backend
 * 
 * @author Felix Wirth
 *
 */
public class ConnectionEasybackend  implements AuthHandler {
    
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

            // Return
            return result;
        }        
    };
    
    /** Path to authorize */
    private static final String              AUTHENTICATE_TEMPLATE = "auth/realms/%s/protocol/openid-connect/token";
    /** REST client */
    private final Client                     client;
    /** URI of the Keycloak auth server */
    private final URI                        authServer;
    /** URI of the actual backend server */
    private final URI                        apiServer;
    /** Auth data */
    private final Form                       auth                  = new Form();
    /** Jackson object mapper */
    private final ObjectMapper               mapper                = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    /** Path to authenticate at Keycloak */
    private final String                     authenticatePath;
    /** Authentification bearer token */
    private String                           bearer;
    /** Own participant */
    private final Participant                self;
    
    @Override
    public Builder authenticate(Builder builder) throws BusException {
        
        // Prepare
        WebTarget target = client.target(authServer).path(authenticatePath);
        target.request(MediaType.APPLICATION_FORM_URLENCODED);
        
        // Try to authenticate and obtain data
        try {
            // Execute request
            Response response =  target.request().post(Entity.form(auth));
            
            // Check code
            if(response.getStatus() != 200) {
                throw new BusException(DEFAULT_ERROR_HANDLER.apply(response));
            }
            
            // Make and store object out of string            
            setBearer(mapper.reader().readTree(response.readEntity(String.class)).get("access_token").textValue());
                        
        } catch (Exception e) {
            throw new BusException("Unable to execute authentification request", e);
        }
        
        // Remove old authorization, add new and return
        return builder == null ? null
                : builder.header("Authorization", null)
                         .header("Authorization",
                                 String.format("Bearer %s", getBearer()));
        
    }
    
    /**
     * Creates a new instance
     * 
     * @param authServer
     * @param apiServer
     * @param realm
     * @param clientId
     * @param clientSecret
     * @param self
     * @param password
     * @param proxy
     * @throws IllegalStateException
     */
    public ConnectionEasybackend(URI authServer,
                                 URI apiServer,
                                 String realm,
                                 String clientId,
                                 String clientSecret,
                                 Participant self,
                                 String password,
                                 Proxy proxy) throws IllegalStateException {
        // Check
        if (authServer == null || apiServer == null || realm == null || clientId == null || clientSecret == null || self == null || password == null) {
            throw new IllegalArgumentException("All parameters but proxy must not be null!");
        }        
        
        // Store
        this.authServer = authServer;
        this.authenticatePath = String.format(AUTHENTICATE_TEMPLATE, realm);
        this.apiServer = apiServer;
        this.self = self;
        
        // Set auth parameter
        this.auth.param("client_id", clientId);
        this.auth.param("client_secret", clientSecret);
        this.auth.param("scope", "openid");
        this.auth.param("grant_type", "password");
        this.auth.param("username", self.getName());
        this.auth.param("password", password);
        
        // Store proxy
        if (proxy == null) {
            this.client = ClientBuilder.newClient();
        } else {
            client = ClientBuilder.newClient(new ClientConfig().connectorProvider(new HttpUrlConnectorProvider().connectionFactory(new ConnectionFactory() {
                
                @Override
                public HttpURLConnection getConnection(URL url) throws IOException {
                    return (HttpURLConnection) url.openConnection(proxy);
                }
            })));
        }       
        
        // Check config and throw exception if wrong
        try {
            authenticate(null);
        } catch (BusException e) {
            throw new IllegalStateException("Unable to authenticate!", e);
        }            
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
        WebTarget target = client.target(apiServer).path(path);

        // Set parameters
        if (parameters != null && !parameters.isEmpty()) {
            for (Entry<String, String> parameter : parameters.entrySet()) {
                target = target.queryParam(parameter.getKey(), parameter.getValue());
            }
        }       

        // Set auth and return
        return target.request().header("Authorization", String.format("Bearer %s", getBearer()));
    }
    
    /**
     * Set bearer token object
     * 
     * @param String bearer
     */
    private synchronized void setBearer(String bearer) {
        this.bearer = bearer;
    }
    
    /**
     * Get bearer token object
     * 
     * @return the bearer
     */
    private synchronized String getBearer() {
        return this.bearer;
    }
    
    /**
     * Get self
     * @return 
     */
    public Participant getSelf() {
        return this.self;
    }
}

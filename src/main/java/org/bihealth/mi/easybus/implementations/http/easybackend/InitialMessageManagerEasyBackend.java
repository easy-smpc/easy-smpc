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
import java.math.BigInteger;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;

import org.bihealth.mi.easybus.BusException;
import org.bihealth.mi.easybus.BusMessage;
import org.bihealth.mi.easybus.BusMessageFragment;
import org.bihealth.mi.easybus.InitialMessageManager;
import org.bihealth.mi.easybus.MessageManager;
import org.bihealth.mi.easybus.implementations.http.HTTPAuthentication;
import org.bihealth.mi.easybus.implementations.http.HTTPException;
import org.bihealth.mi.easybus.implementations.http.HTTPRequest;
import org.bihealth.mi.easybus.implementations.http.HTTPRequest.HTTPRequestType;
import org.bihealth.mi.easysmpc.resources.Resources;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Message manager for EasyBackend
 * 
 * @author Felix Wirth
 *
 */
public class InitialMessageManagerEasyBackend extends InitialMessageManager {

    /** Path to list initial messages */
    private static final String      PATH_LIST_INITIAL_MESSAGES  = "api/easybackend/list/_round0";
    /** Delete message */
    private static final String      PATH_DELETE_MESSAGE_PATTERN = "api/easybackend/message/%s";
    /** Bearer auth scheme */
    private static final String      AUTH_SCHEME                 = "Bearer %s";
    /** HTTP auth */
    private final HTTPAuthentication auth;
    /** Server */
    private final URI                server;
    /** Access token */
    private String                   token;
    /** Jackson object mapper */
    private ObjectMapper             mapper                      = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    /** Message manager */
    private final MessageManager     messageManager              = new MessageManager(1024);

    /**
     * Creates a new instance
     * 
     * @param actionUpdateMessage
     * @param actionError
     * @param settings
     * @param checkInterval
     */
    public InitialMessageManagerEasyBackend(Consumer<List<BusMessage>> updateMessage,
                                            Consumer<Exception> actionError,
                                            ConnectionSettingsEasyBackend settings,
                                            int checkInterval) {
        super(updateMessage, actionError, checkInterval);
        // Store
        this.auth = new HTTPAuthentication(settings);
        try {
            this.server = settings.getAPIServer().toURI();
        } catch (URISyntaxException e) {
            throw new IllegalStateException("API server URI is incorrect");
        }
    }

    @Override
    public List<BusMessage> retrieveMessages() throws IllegalStateException {

        // Prepare
        String resultString = null;
        Iterator<JsonNode> messages = null;
        Exception exception = null;
        List<BusMessage> result = new ArrayList<>();

        // Get messages as plain string
        try {
            resultString = new HTTPRequest(server, PATH_LIST_INITIAL_MESSAGES, HTTPRequestType.GET, String.format(AUTH_SCHEME, getToken()), null).execute();
        } catch (HTTPException e) {

            // If error reason was unauthenticated, re-authenticate and retry
            if (e.getStatusCode() == 401) {

                // Re-authenticate
                renewToken();
                try {
                    // Retry
                    resultString = new HTTPRequest(server, PATH_LIST_INITIAL_MESSAGES, HTTPRequestType.GET, String.format(AUTH_SCHEME, getToken()), null).execute();
                    exception = null;
                } catch (Exception e1) {
                    // Error still exists
                    exception = e1;
                }
            }
            // Exception reason was not unauthenticated
            exception = e;
        }

        // Was there an exception?
        if (exception != null) {
            processError(exception);
        }

        // Map result string to JSON
        try {
            messages = mapper.reader().readTree(resultString).elements();
        } catch (JsonProcessingException e) {
            processError(e);
        }

        // Loop over messages in JSON node
        while (messages.hasNext()) {

            // Recreate message
            BusMessage message = recreateMessage(messages);

            // Process with message manager
            BusMessage messageComplete = null;
            try {
                messageComplete = messageManager.mergeMessage(message, false);
            } catch (BusException e) {
                processError(e);
            }

            // Add to list if actually complete
            if (messageComplete != null) {
                result.add(messageComplete);
            }
        }

        // Return
        return result;
    }

    /**
     * Recreate a message from a JSON node
     * 
     * @param messages
     */
    private BusMessage recreateMessage(Iterator<JsonNode> messages) {
        // Prepare
        JsonNode messagesNode = messages.next();
        
        // Check
        if (messagesNode.path("id") == null || messagesNode.path("id").isMissingNode() || messagesNode.path("content") == null || messagesNode.path("content").isMissingNode()) {
            processError(new IllegalStateException(Resources.getString("DialogMessagePicker.5")));
        }
        
        BigInteger id = messagesNode.path("id").bigIntegerValue();
        Object o = null;
        try {
            o = BusEasyBackend.deserializeMessage(messagesNode.path("content").textValue());
        } catch (ClassNotFoundException | IOException e) {
            processError(e);
        }
        
        // Create object
        if (o instanceof BusMessageFragment) {
            return new BusMessageFragment((BusMessageFragment) o) {
                /** SVUID */
                private static final long serialVersionUID = -2294147052362533378L;
                
                @Override
                public void delete() throws BusException {
                    deleteMessage(id);
                }
                @Override
                public void expunge() throws BusException {
                    // Empty by design
                }
            };
        } else {
            return new BusMessage((BusMessage) o) {
                /** SVUID */
                private static final long serialVersionUID = -2294147098332533758L;
                @Override
                public void delete() throws BusException {
                    deleteMessage(id);
                }
                @Override
                public void expunge() throws BusException {
                    // Empty by design
                }
            };
        }
    }

    /**
     * Get token
     */
    private String getToken() {
        if (token == null) {
            renewToken();
        }
        return token;
    }

    /**
     * Renew auth token
     */
    private synchronized void renewToken() {
        token = auth.authenticate();
    }

    /**
     * Deletes a message
     * 
     * @param id
     */
    protected void deleteMessage(BigInteger id) {
        
        // Prepare
        Exception exception = null;
        
        try {
            // Delete message
             new HTTPRequest(server, String.format(PATH_DELETE_MESSAGE_PATTERN, id), HTTPRequestType.DELETE, String.format(AUTH_SCHEME, getToken()), null).execute();
        } catch (HTTPException e) {
    
            // If error reason was unauthenticated, re-authenticate and retry
            if(e.getStatusCode() == 401) {
                
                // Re-authenticate
                renewToken();
                try {
                    // Re-try
                    new HTTPRequest(server, String.format(PATH_DELETE_MESSAGE_PATTERN, id), HTTPRequestType.DELETE, String.format(AUTH_SCHEME, getToken()), null).execute();
                    exception = null;
                } catch (Exception e1) {
                    // Still exception
                    exception = e1;
                }
            }
            // Exception reason was not unauthenticated
            exception = e;
        }
        
        // Was there an exception?
        if(exception != null) {
            processError(exception);
        }
    }
}
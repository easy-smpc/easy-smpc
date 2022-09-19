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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.math.BigInteger;
import java.util.Base64;
import java.util.Iterator;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bihealth.mi.easybus.Bus;
import org.bihealth.mi.easybus.BusException;
import org.bihealth.mi.easybus.BusMessage;
import org.bihealth.mi.easybus.BusMessageFragment;
import org.bihealth.mi.easybus.MessageFilter;
import org.bihealth.mi.easybus.MessageManager;
import org.bihealth.mi.easybus.Participant;
import org.bihealth.mi.easybus.Scope;
import org.bihealth.mi.easybus.implementations.http.ExecuteHTTPRequest;
import org.bihealth.mi.easysmpc.resources.Resources;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.ws.rs.client.Invocation.Builder;
import jakarta.ws.rs.core.Response;

/**
 * Bus implementation with easybackend
 * 
 * @author Felix Wirth
 *
 */
public class BusEasybackend extends Bus {
    
    /** Path to send messages */
    private static final String   PATH_SEND_MESSAGE_PATTERN   = "api/easybackend/send/%s/%s";
    /** Path to receive messages */
    private static final String   PATH_GET_MESSAGES_PATTERN   = "api/easybackend/receive/%s";
    /** Delete message */
    private static final String   PATH_DELETE_MESSAGE_PATTERN = "api/easybackend/message/%s";
    /** Logger */
    private Logger                LOGGER                      = LogManager.getLogger(BusEasybackend.class);
    /** Connection to backend */
    private ConnectionEasybackend connection;
    /** Thread */
    private Thread                thread;
    /** Message manager */
    private MessageManager        messageManager;
    /** Stop flag */
    boolean                       stop                        = false;
    /** Jackson object mapper */
    private ObjectMapper          mapper                      = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    /**
     * Creates a new instance
     * 
     * @param sizeThreadpool
     * @param millis
     * @param connection
     * @param maxMessageSize
     */
    public BusEasybackend(int sizeThreadpool, int millis, ConnectionEasybackend connection, int maxMessageSize) {
        // Super
        super(sizeThreadpool);
        
       
        // Store
        this.connection = connection;
        messageManager = new MessageManager(maxMessageSize);
        
        // TODO Parts of this implementation might be pulled up to Bus, when making receive() abstract in Bus. Do so?
        // Create thread
        this.thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    while (!stop) {
                        try {
                            receive();
                        } catch (BusException e) {
                            // Log exception
                            LOGGER.error("Error receiving messages", e);
                        }
                        Thread.sleep(millis);
                    }
                } catch (InterruptedException e) {
                    // Die silently
                }
            }
        });
        
        // Start thread
        thread.setDaemon(true);
        thread.start();
    }

    /**
     * Receives messages
     * 
     * @throws BusException
     * @throws InterruptedException 
     */
    protected void receive() throws BusException, InterruptedException {
        
        // Log
        LOGGER.debug("Started receiving");

        // Prepare
        String resultString;
        Builder request;
        
        // Loop over scopes
        for (String scope : getScopesForParticipant(this.connection.getSelf())) {
            request = this.connection.getBuilder(String.format(PATH_GET_MESSAGES_PATTERN, scope));

            // Create task to get sync
            FutureTask<String> future = new ExecuteHTTPRequest<String>(request,
                                                                      ExecuteHTTPRequest.REST_TYPE.GET,
                                                                      new Supplier<ExecutorService>() {

                                                                          @Override
                                                                          public ExecutorService
                                                                                 get() {
                                                                              return getExecutor();
                                                                          }
                                                                      },
                                                                      null,
                                                                      new Function<Response, String>() {

                                                                          @Override
                                                                          public String
                                                                                 apply(Response response) {
                                                                              return response.readEntity(String.class);
                                                                          }
                                                                      },
                                                                      ConnectionEasybackend.DEFAULT_ERROR_HANDLER,
                                                                      this.connection,
                                                                      Resources.RETRY_EASYBACKEND_NUMBER_RETRY,
                                                                      Resources.RETRY_EASYBACKEND_WAIT_TIME_RETRY).execute();

            // Wait for task end or exception
            // TODO Parallelize reading messages?
            try {
                resultString = future.get(Resources.TIMEOUT_EASYBACKEND, TimeUnit.MILLISECONDS);
            } catch (InterruptedException | ExecutionException | TimeoutException e) {
               LOGGER.error("Error while executing HTTP request to get messages!", e);
               continue;
            }
            
            // Check for interrupt
            if (Thread.interrupted()) { 
                throw new InterruptedException();
            }
            
            // Understand result
            Iterator<JsonNode> messages;
            try {
                messages = mapper.reader().readTree(resultString).elements();
            } catch (JsonProcessingException e) {
                LOGGER.error("Error deserializing sync string!", e);
                continue;
            }
            
            // Loop over messages in json node
            while(messages.hasNext()) {
                
                // Check for interrupt
                if (Thread.interrupted()) { 
                    throw new InterruptedException();
                }
                
                // Prepare
                JsonNode messagesNode = messages.next();
                final BusMessage message;
                
                // Recreate message
                try {
                    message = recreateMessage(messagesNode);
                } catch (BusException e) {
                    LOGGER.error("Unable to recreate message!", e);
                    continue;
                }

                // Process with message manager
                BusMessage messageComplete = messageManager.mergeMessage(message);

                // Send to scope and participant
                if (messageComplete != null) {
                    receiveInternal(messageComplete);
                }
            }
        }
    }

    /**
     * Recreate message
     * 
     * @param messagesNode
     * @return
     * @throws BusException 
     */
    private BusMessage recreateMessage(JsonNode messagesNode) throws BusException {
        // Check
        if (messagesNode.path("id") == null || messagesNode.path("id").isMissingNode() || messagesNode.path("content") == null || messagesNode.path("content").isMissingNode()) {
            throw new BusException("Node contains unsufficient data fields!");
        }
        
        // Set values
        BigInteger id = messagesNode.path("id").bigIntegerValue();
        Object o;
        try {
            o = deserializeMessage(messagesNode.path("content").textValue());
        } catch (ClassNotFoundException | IOException e) {
            throw new BusException("Unable to deserialize message", e);
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
                    // Empty
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
                    // Empty
                }
            };
        }
        
    }

    /**
     * Deletes a message
     * 
     * @param id
     */
    protected void deleteMessage(BigInteger id) {
        
        // Prepare request
        Builder request = this.connection.getBuilder(String.format(PATH_DELETE_MESSAGE_PATTERN, id));
        
        // Create task to get sync
        FutureTask<String> future = new ExecuteHTTPRequest<String>(request,
                                                              ExecuteHTTPRequest.REST_TYPE.DELETE,
                                                              new Supplier<ExecutorService>() {

                                                                  @Override
                                                                  public ExecutorService get() {
                                                                      return getExecutor();
                                                                  }
                                                                  },
                                                                  null,
                                                                  new Function<Response, String>() {

                                                                      @Override
                                                                      public String
                                                                             apply(Response response) {
                                                                          return null;
                                                                      }
                                                                  },
                                                                  ConnectionEasybackend.DEFAULT_ERROR_HANDLER,
                                                                  this.connection,
                                                                  Resources.RETRY_EASYBACKEND_NUMBER_RETRY,
                                                                  Resources.RETRY_EASYBACKEND_WAIT_TIME_RETRY).execute();
        
        // Wait for task end or exception
        try {
            future.get(Resources.TIMEOUT_EASYBACKEND, TimeUnit.MILLISECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            LOGGER.error("Error while executing HTTP request to delete message!", e);
        }        
        
    }

    @Override
    public boolean isAlive() {
        return this.thread != null && this.thread.isAlive();
    }

    @Override
    public void stop() {

        // Set stop flag
        this.stop = true;

        // Shutdown executor
        getExecutor().shutdown();

        // If on the same thread, just return
        if (this.thread == null || Thread.currentThread().equals(this.thread)) {
            return;

            // If on another thread, interrupt and wait for thread to die
        } else {

            // Stop thread
            this.thread.interrupt();

            // Wait for thread to stop
            while (thread != null && thread.isAlive()) {
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    // Ignore
                }
            }
        }
    }

    @Override
    protected Void sendInternal(BusMessage message) throws Exception {
        
        // Send message(s)
        try {
            for (BusMessage m : messageManager.splitMessage(message)) {
                send(message.getReceiver(), message.getScope(), m);
            }
        } catch (IOException | BusException e) {
            throw new BusException("Unable to send message", e);
        }        
        
        // Return
        return null;
    }

    /** Sends a message to the backend
     * 
     * @param receiver
     * @param scope
     * @param message
     */
    private void send(Participant receiver, Scope scope, Object message) throws BusException {
        
        // Create payload
        String payload;
        try {
            payload = serializeObject(message);
        } catch (IOException e) {
            throw new BusException("Unable to serialize message", e);
        }
        
        // Create request
        Builder request = this.connection.getBuilder(String.format(PATH_SEND_MESSAGE_PATTERN, scope.getName(), receiver.getName()));
        
        // Create task to get sync
        FutureTask<String> future = new ExecuteHTTPRequest<String>(request,
                                                                  ExecuteHTTPRequest.REST_TYPE.POST,
                                                                  new Supplier<ExecutorService>() {

                                                                      @Override
                                                                      public ExecutorService get() {
                                                                          return getExecutor();
                                                                      }
                                                                  },
                                                                  payload,
                                                                  null,
                                                                  ConnectionEasybackend.DEFAULT_ERROR_HANDLER,
                                                                  this.connection,
                                                                  Resources.RETRY_EASYBACKEND_NUMBER_RETRY,
                                                                  Resources.RETRY_EASYBACKEND_WAIT_TIME_RETRY).execute();
        
        // Wait for task end or exception
        try {
            future.get(Resources.TIMEOUT_EASYBACKEND, TimeUnit.MILLISECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            throw new BusException("Error while executing HTTP request to send message!", e);
        }

    }

    @Override
    public void purge(MessageFilter filter) throws BusException, InterruptedException {
        // TODO Implement purge
    }
    
    
    /**
     * Serialize an object.
     *
     * @param object
     * @return the string
     * @throws IOException Signals that an I/O exception has occurred.
     */
    private String serializeObject(Object o) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(new GZIPOutputStream(bos));
        oos.writeObject(o);
        oos.close();
        return Base64.getEncoder().encodeToString(bos.toByteArray());
    }
    
    /**
     * Deserialize an object
     *
     * @param serialued object
     * @return the object
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws ClassNotFoundException the class not found exception
     */
    private Object deserializeMessage(String msg) throws IOException, ClassNotFoundException {
        byte[] data = Base64.getDecoder().decode(msg);
        ObjectInputStream ois = new ObjectInputStream(new GZIPInputStream(new ByteArrayInputStream(data)));
        Object o = ois.readObject();
        ois.close();
        return o;
    }
    
    @Override
    public FutureTask<Void> sendPlain(String recipient, String subject, String body) throws BusException {
        throw new UnsupportedOperationException("Sending plain messages is not supported by this bus");
    }
}

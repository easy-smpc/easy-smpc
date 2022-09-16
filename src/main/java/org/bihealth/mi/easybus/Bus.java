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
package org.bihealth.mi.easybus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * The Bus collecting and sending the messages
 * 
 * @author Felix Wirth
 * @author Fabian Prasser
 */
public abstract class Bus {
    
    /** Logger */
    private static final Logger                                       LOGGER = LogManager.getLogger(Bus.class);
    /** Stores the subscriptions with known participants */
    private final Map<Scope, Map<Participant, List<MessageListener>>> subscriptions;
    /** Executor service */
    private final ExecutorService                                     executor;
    
    /**
     * Creates a new instance
     * 
     * @param sizeThreadpool
     */
    public Bus(int sizeThreadpool) {
        
        // Check
        if (sizeThreadpool <= 0) {
            throw new IllegalArgumentException("sizeThreadpool must be a positive number");
        }
        
        // Create
        this.executor = Executors.newFixedThreadPool(sizeThreadpool);
        this.subscriptions = new HashMap<>();
    }

    /**
     * Returns whether potentially running backend services are alive
     * @return
     */
    public abstract boolean isAlive();    
    
    /**
     * Allows to subscribe to a scope for a participant 
     * 
     * @param scope
     * @param participant
     * @param messageListener
     */
    public synchronized void receive(Scope scope, Participant participant, MessageListener messageListener) {
        
        // Get or create scope
        Map<Participant,List<MessageListener>> subscriptionsForScope = subscriptions.get(scope);
        if (subscriptionsForScope == null) {
            subscriptionsForScope = new HashMap<>();
            subscriptions.put(scope, subscriptionsForScope);
        }
        
        // Get or create listeners for participant
        List<MessageListener> listenerForParticipant = subscriptionsForScope.get(participant);
        if (listenerForParticipant == null) {
            listenerForParticipant = new ArrayList<>();
            subscriptionsForScope.put(participant, listenerForParticipant);
        }
        
        // Add listener
        listenerForParticipant.add(messageListener);
    }
    
    /**
     * Passes on receiving errors
     *  
     * @param messageListener
     */
    public synchronized void receiveErrorInternal(Exception exception) {
        
        for(Entry<Scope, Map<Participant, List<MessageListener>>> scope : subscriptions.entrySet()) {
            for(Entry<Participant, List<MessageListener>> participant : scope.getValue().entrySet()) {
                for(MessageListener messageListener : participant.getValue()) {
                    messageListener.receiveError(exception);
                }
            }
        }        
    }    

    /**
     * Allows to send a message to a participant
     * In case of error retries infinitely - error handling must be done by using the returned FutureTask object
     * 
     * @param message
     * @param scope
     * @param participant
     * @return 
     * @throws BusException
     */
    public FutureTask<Void> send(String message, Scope scope, Participant participant) throws BusException {
        // Create future task 
        FutureTask<Void> task = new FutureTask<>(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                // Init
                boolean sent = false;
                
                // Retry until sent successful or interrupted
                while (!sent && !Thread.interrupted()) {
                    try {
                        sendInternal(new BusMessage(participant, scope, message));
                        sent = true;
                    } catch (BusException e) {
                        // Log and repeat
                        LOGGER.error("Error sending message", e);
                    }
                }
                return null;
            }
        });
        
        // Start and return
        executor.execute(task);
        return task;
    }
    
    /**
     * Stops all backend services that might be running
     */
    public abstract void stop();
    
    /**
     * @return the executor
     */
    protected ExecutorService getExecutor() {
        return executor;
    }
    
    /**
     * Is there a listener for the participant and scope registered
     * 
     * @param scope
     * @param participant
     * @return
     */
    protected synchronized boolean isParticipantScopeRegistered(Scope scope, Participant participant) {
        // Check not null
        if (scope == null || participant == null) {
            return false;
        }
        
        // Check if scope exists
        Map<Participant,List<MessageListener>> subscriptionsForScope = subscriptions.get(scope);        
        if (subscriptionsForScope == null) {
           return false;
        }
        
        // Check if participant is registered for scope
        if (subscriptionsForScope.get(participant) == null ||
            subscriptionsForScope.get(participant).size() == 0) {
            return false;
        }
        
        // At least one listener is registered for scope and participant tuple
        return true;
    }    
    
    /**
     * Receives an external received message
     * 
     * @param message
     * @throws InterruptedException 
     */
    protected synchronized boolean receiveInternal(BusMessage message) throws InterruptedException {
        
        Scope scope = message.getScope();
        Participant participant = message.getReceiver();
        
        // Mark received
        boolean received = false;
        
        // Send to subscribers
        if (subscriptions.get(scope) != null && subscriptions.get(scope).get(participant) != null) {
            for (MessageListener messageListener : subscriptions.get(scope).get(participant)) {

                // Check for interrupt
                if (Thread.interrupted()) { 
                    throw new InterruptedException();
                }

                messageListener.receive(message.getMessage());
                received = true;
            }
        }

        // Done
        return received;
    }

    /**
     * Abstract method to send a message
     * 
     * @param message
     * @return task
     * @throws Exception
     */
    protected abstract Void sendInternal(BusMessage message) throws Exception;

    /**
     * Deletes EasySMPC relevant data
     * 
     * @throws BusException, InterruptedException 
     */
    public abstract void purge(MessageFilter filter) throws BusException, InterruptedException;
    
    
    /**
     * Get all scopes for a participant
     * 
     * @param participant
     * @return
     */
    protected synchronized List<String> getScopesForParticipant(Participant participant) {
        // Prepare
        List<String> result = new ArrayList<>();
        
        // Loop over scopes
        for (Entry<Scope, Map<Participant, List<MessageListener>>> subscription : subscriptions.entrySet()) {
            if(subscription.getValue().containsKey(participant)) {
                result.add(subscription.getKey().getName());
            }
        }
        
        // Return
        return result;
    }

    /**
     * Send a plain message (no bus functionality)
     * 
     * @param recipient
     * @param subject
     * @param body
     * @return 
     * @throws BusException
     */
    public abstract FutureTask<Void> sendPlain(String recipient, String subject, String body) throws BusException;
}
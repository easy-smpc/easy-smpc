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

/**
 * The Bus collecting and sending the messages
 * 
 * @author Felix Wirth
 * @author Fabian Prasser
 */
public abstract class Bus {
    
    /** Stores the subscriptions with  known participants*/    
    private final Map<Scope, Map<Participant, List<MessageListener>>> subscriptions;   
    /** Executor service */
    private final ExecutorService executor;
    
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
     * Abstract method to send a message
     * 
     * @param message
     * @param scope
     * @param participant
     * @return task
     * @throws Exception
     */
    public abstract Void sendInternal(Message message, Scope scope, Participant participant) throws Exception;
    
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
    public FutureTask<Void> send(Message message, Scope scope, Participant participant) throws BusException {
        // Create future task 
        FutureTask<Void> task = new FutureTask<>(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                // Init
                boolean sent = false;
                
                // Retry until sent successful
                while(!sent) {
                    try {
                        sendInternal(message, scope, participant);
                        sent = true;
                    } catch (BusException e) {
                        // Ignore and repeat
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
     * Receives an external received message
     * 
     * @param message
     * @param scope
     * @param participant
     * @throws InterruptedException 
     */
    protected synchronized boolean receiveInternal(Message message, Scope scope, Participant participant) throws InterruptedException {
        
        // Mark received
        boolean received = false;
        
        // Send to subscribers
        if (subscriptions.get(scope) != null && subscriptions.get(scope).get(participant) != null) {
            for (MessageListener messageListener : subscriptions.get(scope).get(participant)) {

                // Check for interrupt
                if (Thread.interrupted()) { 
                    throw new InterruptedException();
                }

                messageListener.receive(message);
                received = true;
            }
        }

        // Done
        return received;
    }
    
    /**
     * Stops all backend services that might be running
     */
    public abstract void stop();    
    
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
     * @return the executor
     */
    protected ExecutorService getExecutor() {
        return executor;
    }

    /**
     * Is the bus connected?
     * 
     * @return
     */
    public abstract boolean isConnected();
}
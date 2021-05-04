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
import java.util.concurrent.atomic.AtomicInteger;

/**
 * The Bus collecting and sending the messages
 * 
 * @author Felix Wirth
 * @author Fabian Prasser
 */
public abstract class Bus {
    
    /** Total number of messages started to receive */
    public static final AtomicInteger numberMessagesReceived = new AtomicInteger();
    /** Total attachment size of messages started to receive */
    public static final AtomicInteger totalSizeMessagesReceived = new AtomicInteger();
    /** Total number of messages sent */
    public static final AtomicInteger numberMessagesSent = new AtomicInteger();
    /** Total attachment size of messages sent */
    public static final AtomicInteger totalSizeMessagesSent = new AtomicInteger();
    /** Stores the subscriptions with  known participants*/    
    private final Map<Scope, Map<Participant, List<MessageListener>>> subscriptions;
    
    /**
     * Creates a new instance
     */
    public Bus(){
        this.subscriptions = new HashMap<>();
    }
    
    /**
     * Resets the statistics
     */
    public static void resetStatistics() {
        numberMessagesReceived.set(0);
        totalSizeMessagesReceived.set(0);
        numberMessagesSent.set(0);
        totalSizeMessagesSent.set(0);
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
    public void receive(Scope scope, Participant participant, MessageListener messageListener) {
        
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
     * Allows to send a message to a participant
     * 
     * @param message
     * @param scope
     * @param participant
     * @throws BusException
     */
    public abstract void send(Message message, Scope scope, Participant participant) throws BusException;
   
    /**
     * Stops all backend services that might be running
     */
    public abstract void stop();

    /**
     * Receives an external received message
     * 
     * @param message
     * @param scope
     * @param participant
     * @throws InterruptedException 
     */
    protected boolean receiveInternal(Message message, Scope scope, Participant participant) throws InterruptedException {
        
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
}

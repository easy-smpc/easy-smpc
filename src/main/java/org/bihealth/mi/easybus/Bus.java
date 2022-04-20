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

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
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
    private static final Logger LOGGER = LogManager.getLogger(Bus.class);
    /** Stores the subscriptions with  known participants*/    
    private final Map<Scope, Map<Participant, List<MessageListener>>> subscriptions;   
    /** Executor service */
    private final ExecutorService executor;
    /** Maximal size of a message to transfer in bytes*/
    private final int maxMessageSize;
    /** Message fragments */
    private final Map<String, MessageFragmentFinish[]> messagesFragments;
    
    /**
     * Creates a new instance
     * 
     * @param sizeThreadpool
     * @param maxMessageSize
     */
    public Bus(int sizeThreadpool, int maxMessageSize) {
        
        // Check
        if (sizeThreadpool <= 0) {
            throw new IllegalArgumentException("sizeThreadpool must be a positive number");
        }
        
        if(maxMessageSize <= 0) {
            throw new IllegalArgumentException("maxMessageSize must be a positive number");
        }
        
        // Create
        this.executor = Executors.newFixedThreadPool(sizeThreadpool);
        this.subscriptions = new HashMap<>();
        this.maxMessageSize = maxMessageSize;
        this.messagesFragments = new ConcurrentHashMap<>();
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
    public FutureTask<Void> send(Message message, Scope scope, Participant participant) throws BusException {
        // Create future task 
        FutureTask<Void> task = new FutureTask<>(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
            	
                // Init
                List<MessageFragment> fragments = MessageFragment.createInternalMessagesFromMessage(message, maxMessageSize);                
                List<MessageFragment> successFragments = new ArrayList<>(); 
                
                // Retry until sent successful or interrupted
                while(fragments.size() > 0  && !Thread.interrupted()) {
                    try {
                        // Remove sent fragments
                    	fragments.removeAll(successFragments);
                        successFragments.clear();
                        
                        // Send single fragments
                        for(MessageFragment fragment : fragments) {
                            sendInternal(fragment, scope, participant);
                            successFragments.add(fragment);
                        }
                        
                    } catch (BusException e) {
                        LOGGER.error("Unable to send message. Will be re-retried", e);
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
     * Abstract method to send a message
     * 
     * @param messageInternal
     * @param scope
     * @param participant
     * @return task
     * @throws Exception
     */
    protected abstract Void sendInternal(MessageFragment messageInternal, Scope scope, Participant participant) throws Exception;
    
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
     * @param messageFragment
     * @param scope
     * @param participant
     * @throws InterruptedException 
     * @throws BusException 
     */
    protected synchronized boolean receiveInternal(MessageFragmentFinish messageFragment, Scope scope, Participant participant) throws InterruptedException, BusException {                       
        
        // Get or create fragments array
        MessageFragmentFinish[] messageFragments = this.messagesFragments.computeIfAbsent(messageFragment.getId(), (key) -> new MessageFragmentFinish[messageFragment.getSplitTotal()]);
        
        // Check
        if (messageFragment.getSplitTotal() > messageFragments.length) {
            throw new BusException(String.format("Index for number of messages %d for new fragment does not suit to total number of messages %d message id %s",
                                                 messageFragment.getSplitNr(),
                                                 messageFragments.length,
                                                 messageFragment.getId()));
        }

        // Add to list
        messageFragments[messageFragment.getSplitNr()] = messageFragment;
        
        // TODO Do this in an own thread? Rearrange Thread.interrupted() accordingly
        // If message complete receive internal
        if(messageComplete(messageFragments)) {            
            return receiveCompleteMessage(messageFragment.getId(), scope, participant);
        }
        
        // Return
        return false;
    }

    /**
     * Receives a complete messages for subscribers
     * 
     * @param messageId
     * @param scope
     * @param participant
     * @return
     * @throws BusException
     * @throws InterruptedException
     */
    private boolean receiveCompleteMessage(String messageId, Scope scope, Participant participant) throws BusException, InterruptedException {
        
        // Init
        MessageFragmentFinish[] messageFragments = this.messagesFragments.get(messageId);
        String messageSerialized = "";
        Message message;
        boolean received = false;

        // Loop over fragments to re-assemble string
        for (int index = 0; index < messageFragments.length; index++) {
            messageSerialized = messageSerialized + messageFragments[index].getContent();
        }

        // Recreate message
        try {
            message = Message.deserializeMessage(messageSerialized);
        } catch (ClassNotFoundException | IOException e) {
            throw new BusException("Unable to deserialize message", e);
        }

        // Send to subscribers
        if (subscriptions.get(scope) != null && subscriptions.get(scope).get(participant) != null) {
            for (MessageListener messageListener : subscriptions.get(scope).get(participant)) {

                // Check for interrupt
                if (Thread.interrupted()) { throw new InterruptedException(); }

                messageListener.receive(message);
                received = true;
            }
        }
        
        if (received) {
            // Loop over fragments to delete messages
            for (int index = 0; index < messageFragments.length; index++) {
                try {
                    messageFragments[index].delete();
                } catch (BusException e) {
                    LOGGER.error("Unable to delete message fragment", e);
                }
            }

            // Finalize
            messageFragments[0].finalize();

            // Remove from map
            this.messagesFragments.remove(messageFragments[0].getId());
        }

        // Done
        return received;
    }
    
    /**
     * Get all subscribed participants regardless of scope
     * 
     * @return participants
     */
    protected List<Participant> getAllParticipants(){
        
        // Prepare
        List<Participant> result = new ArrayList<>();
        
        // Loop over scope and subscribed participants
        for (Entry<Scope, Map<Participant, List<MessageListener>>> subscription : this.subscriptions.entrySet()) {
            for (Entry<Participant, List<MessageListener>> participants : subscription.getValue()
                                                                                      .entrySet()) {
                result.add(participants.getKey());
            }
        }
        
        // Return
        return result;       
    }
    /**
     * Is a message complete?
     * 
     * @param messageFragement
     * @return
     */
    private boolean messageComplete(MessageFragmentFinish[] messageFragement) {
        
        // Loop over array
        for(int index = 0; index < messageFragement.length; index++) {
            if(messageFragement[index] == null) {
                return false;
            }
        }
        
        // Finished
        return true;
    }
    
    /**
     * Deletes all EasyBus relevant data - use with care
     * 
     * @throws BusException
     * @throws InterruptedException
     */
    public abstract void purge() throws Exception;
}
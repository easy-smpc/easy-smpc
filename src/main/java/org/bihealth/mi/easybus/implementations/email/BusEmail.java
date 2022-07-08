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
package org.bihealth.mi.easybus.implementations.email;

import java.io.IOException;
import java.util.concurrent.FutureTask;

import org.bihealth.mi.easybus.Bus;
import org.bihealth.mi.easybus.BusException;
import org.bihealth.mi.easybus.Message;
import org.bihealth.mi.easybus.MessageFilter;
import org.bihealth.mi.easybus.MessageFragment;
import org.bihealth.mi.easybus.MessageManager;
import org.bihealth.mi.easybus.Participant;
import org.bihealth.mi.easybus.Scope;
import org.bihealth.mi.easysmpc.resources.Resources;

/**
 * Bus implementation by email
 * 
 * @author Felix Wirth
 * @author Fabian Prasser
 */
public class BusEmail extends Bus {
    /**
     * Internal message used by email-based implementations
     * 
     * @author Fabian Prasser
     */
    protected abstract static class BusEmailMessage {
        
        /** Receiver */
        protected final Participant     receiver;
        /** Scope */
        protected final Scope           scope;
        /** Message */
        protected final MessageFragment message;
        /** Subject */
        protected final String          subject;
        
        
        /**
         * Message
         * @param receiver
         * @param scope
         * @param attachment
         */
        BusEmailMessage(Participant receiver, Scope scope, MessageFragment message, String subject) {
            this.receiver = receiver;
            this.scope = scope;
            this.message = message;
            this.subject = subject;
        }
    
        /** Deletes the message on the server
         * @throws BusException */
        protected abstract void delete() throws BusException;
        
    
        /** Expunges all deleted messages on the server
         * @throws BusException */
        protected abstract void expunge() throws BusException;
        
        /**
         * Create a MessageFragmentFinishEmail
         * 
         * @return
         */
        protected MessageFragmentEmail getMessageFragment() 
        {
            return new MessageFragmentEmail(this);
        }
    }

    /** Connection */
    private ConnectionEmail connection;
    /** Thread */
    private Thread          thread;
    /** Stop flag */
    private boolean         stop = false;
    /** Message manager */
    private MessageManager  messageManager;
    
    /**
     * Creates a new instance
     * @param connection
     * @param millis - interval in milliseconds in which messages are polled. If zero a send only bus is returned
     */
    public BusEmail(ConnectionEmail connection, int millis) {
        this(connection, millis, Resources.SIZE_THREADPOOL);
    }

    /**
     * Creates a new instance
     * 
     * @param connection
     * @param millis
     * @param sizeThreadpool
     */
    public BusEmail(ConnectionEmail connection, int millis, int sizeThreadpool) {
        this(connection, millis, sizeThreadpool, Resources.EMAIL_DEFAULT_MESSAGE_SIZE);
    }
    
    /**
     * Creates a new instance
     * 
     * @param connection
     * @param millis
     * @param sizeThreadpool
     * @param maxMessageSize
     */
    public BusEmail(ConnectionEmail connection, int millis, int sizeThreadpool, int maxMessageSize) {
        
        // Super
        super(sizeThreadpool);
        
        // Check
        if(millis <= 0) {
            throw new IllegalArgumentException("millis must be a positive number");
        }
        
        // Store and create
        this.connection = connection;
        messageManager = new MessageManager(maxMessageSize);
        
        // Create thread
        this.thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    while (!stop) {
                        receiveEmails();
                        Thread.sleep(millis);
                    }
                } catch (InterruptedException e) {
                    connection.close();
                    // Die silently
                }
            }
        });
        thread.setDaemon(true);
        thread.start();
    }
    
    @Override
    public boolean isAlive() {
        return this.thread != null && this.thread.isAlive();
    }
    
    /**
     * Deletes all e-mails in inbox relevant for easysmpc
     * @throws BusException 
     * @throws InterruptedException 
     */
    public void purgeEmails() throws BusException, InterruptedException {
        purgeEmails(null);
    }
    
    /**
     * Deletes all e-mails matching the message filter
     * @throws BusException 
     * @throws InterruptedException 
     */
    public void purgeEmails(MessageFilter filter) throws BusException, InterruptedException {

            // Get mails
            BusEmail.BusEmailMessage deleted = null;
            for (BusEmail.BusEmailMessage message : connection.receive(filter)) {
                // Delete
                message.delete();
                deleted = message;
            }
            
            // Expunge
            if (deleted != null) {
                deleted.expunge();
            }
    }
    
    
    
    @Override
    protected Void sendInternal(Message message, Scope scope, Participant participant) throws BusException {

        // Send message in fragments
        try {
            for (MessageFragment m : messageManager.splitMessage(message)) {
                this.connection.send(m, scope, participant);
            }
        } catch (IOException | BusException e) {
            throw new BusException("Unable to send message", e);
        }
        
        // Return
        return null;
    }    
    
    /**
     * Send a plain e-mail (no bus functionality)
     * 
     * @param recipient
     * @param subject
     * @param body
     * @return 
     * @throws BusException
     */
    public FutureTask<Void> sendPlain(String recipient, String subject, String body) throws BusException {
        // Create future task
        FutureTask<Void> task = new FutureTask<>(new Runnable() {
            @Override
            public void run() {
                // Init
                boolean sent = false;
                
                // Retry until sent successful or interrupted
                while(!sent && !Thread.interrupted()) {
                    try {
                        connection.send(recipient, subject, body, null);
                        sent = true;
                    } catch (BusException e) {
                        // Ignore and repeat
                    }
                }
            }
        }, null);
        
        // Start and return
        getExecutor().execute(task);
        return task;
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
    
    /**
     * Receives e-mails
     * @throws BusException 
     * @throws InterruptedException 
     */
    private synchronized void receiveEmails() throws InterruptedException {
        
        // Create filter for relevant messages
        MessageFilter filter = new MessageFilter() {
            @Override
            public boolean accepts(String messageDescription) {
                // Check if participant and scope is registered
                return isParticipantScopeRegistered(ConnectionEmail.getScope(messageDescription),
                                                    ConnectionEmail.getParticipant(messageDescription));
            }
        };

        try {
            // Get mails
            for (BusEmail.BusEmailMessage message : connection.receive(filter)) {

                // Check for interrupt
                if (Thread.interrupted()) {
                    throw new InterruptedException();
                }
                
                // Process with message manager
                Message messageComplete = messageManager.mergeMessage(message.getMessageFragment());
                
                // Send to scope and participant
                if (messageComplete != null) {
                    receiveInternal(messageComplete, message.scope, message.receiver);
                }
            }
            
        } catch (BusException e) {
            // Pass error over
            this.receiveErrorInternal(e);
        } finally {
            // Close connection
            connection.close();
        }
    }
}
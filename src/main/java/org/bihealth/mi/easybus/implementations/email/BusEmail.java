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

import java.util.Date;
import java.util.concurrent.FutureTask;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bihealth.mi.easybus.Bus;
import org.bihealth.mi.easybus.BusException;
import org.bihealth.mi.easybus.Message;
import org.bihealth.mi.easybus.MessageFilter;
import org.bihealth.mi.easybus.Participant;
import org.bihealth.mi.easybus.Scope;

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
        protected final Participant receiver;
        /** Scope */
        protected final Scope       scope;
        /** Message */
        protected final Message     message;
        /** Subject */
        protected final String     subject;
        
        
        /**
         * Message
         * @param receiver
         * @param scope
         * @param message
         */
        BusEmailMessage(Participant receiver, Scope scope, Message message, String subject) {
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
    }

    /** Logger */
    private static final Logger logger = LogManager.getLogger(BusEmail.class);

    /** Connection */
    private ConnectionEmail connection;
    /** Thread */
    private Thread          thread;
    /** Stop flag */
    private boolean         stop = false;
    
    /**
     * Creates a new instance
     * @param connection
     * @param millis - interval in milliseconds in which messages are polled. If zero a send only bus is returned
     */
    public BusEmail(ConnectionEmail connection, int millis) {
        this(connection, millis, 0);
    }

    /**
     * Creates a new instance
     * 
     * @param connection
     * @param millis
     * @param sizeThreadpool
     */
    public BusEmail(ConnectionEmail connection, int millis, int sizeThreadpool) {
        super(sizeThreadpool);
        this.connection = connection;
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
     * Is there a working connection to receive?
     * 
     * @return
     */
    public boolean isReceivingConnected() {
        // Check if connected
        if(this.connection != null) {
            return this.connection.isReceivingConnected();
        }
        
        // Return false
        return false;
    }
    
    /**
     * Deletes all e-mails in inbox relevant for easysmpc
     * @throws BusException 
     * @throws InterruptedException 
     */
    public void purgeEmails() throws BusException, InterruptedException {

            // Get mails
            BusEmail.BusEmailMessage deleted = null;
            for (BusEmail.BusEmailMessage message : connection.receive(null)) {
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
    public Void sendInternal(Message message, Scope scope, Participant participant) throws BusException {
        this.connection.send(message, scope, participant);
        return null;
    }    
    
    @Override
    public void stop() {
        
        // Set stop flag
        this.stop = true;
        
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
            BusEmail.BusEmailMessage deleted = null;
            for (BusEmail.BusEmailMessage message : connection.receive(filter)) {

                // Check for interrupt
                if (Thread.interrupted()) {
                    throw new InterruptedException();
                }

                // Mark
                boolean received = false;
                
                // Send to scope and participant
                received |= receiveInternal(message.message, message.scope, message.receiver);

                // Delete 
                if (received) {
                    try {
                        message.delete();
                        logger.debug("Message deleted logged", new Date(),"Message deleted", message.scope.getName(),  message.receiver.getName(), message.subject);
                        deleted = message;
                    } catch (BusException e) {
                        logger.error("Deletion error logged", new Date(), message.scope.getName(), message.receiver.getName(), message.subject);
                    }

                }
            }

            // Expunge
            if (deleted != null) {
                deleted.expunge();
            }
        } catch (BusException e) {
            // Pass error over
            this.receiveErrorInternal(e);
        } finally {
            // Close connection
            connection.close();
        }
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
                
                // Retry until sent successful
                while(!sent) {
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
}
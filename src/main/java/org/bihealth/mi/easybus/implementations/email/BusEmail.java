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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bihealth.mi.easybus.Bus;
import org.bihealth.mi.easybus.BusException;
import org.bihealth.mi.easybus.Message;
import org.bihealth.mi.easybus.Participant;
import org.bihealth.mi.easybus.Scope;

/**
 * Bus implementation by email
 * 
 * @author Felix Wirth
 * @author Fabian Prasser
 */
public class BusEmail extends Bus {
    /** Logger */
    private static final Logger logger = LogManager.getLogger(ConnectionIMAP.class);

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
        
        /**
         * Message
         * @param receiver
         * @param scope
         * @param message
         */
        BusEmailMessage(Participant receiver, Scope scope, Message message) {
            this.receiver = receiver;
            this.scope = scope;
            this.message = message;
        }
    
        /** Deletes the message on the server
         * @throws BusException */
        protected abstract void delete() throws BusException;
        
    
        /** Expunges all deleted messages on the server
         * @throws BusException */
        protected abstract void expunge() throws BusException;
    }

    /** Connection */
    private ConnectionEmail connection;
    /** Thread */
    private Thread          thread;
    /** Stop flag */
    private boolean         stop = false;
  
    /**
     * Creates a new instance
     * @param connection
     * @param millis - interval in milliseconds in which messages are polled
     */
    public BusEmail(ConnectionEmail connection, int millis) {
        this.connection = connection;
        this.thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    while (!stop) {
                        try {
                            receiveEmails();
                        } catch (BusException e) {
                            // Stop thread
                            throw new RuntimeException(e);
                        }
                        Thread.sleep(millis);
                    }
                } catch (InterruptedException e) {
                    connection.close();
                    logger.debug("", new Date(), "Receive thread stopped", e.getMessage());
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

    @Override
    public void send(Message message, Scope scope, Participant participant) throws BusException {
        this.connection.send(message, scope, participant);
    }
    
    @Override
    public void stop() {
        
        // Set stop flag
        this.stop = true;
        
        // If on the same thread, just return
        if (Thread.currentThread().equals(this.thread)) {
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
    private synchronized void receiveEmails() throws BusException, InterruptedException {
        
        // Get mails
        BusEmail.BusEmailMessage deleted = null;
        for (BusEmail.BusEmailMessage message : connection.receive()) {

            // Check for interrupt
            if (Thread.interrupted()) {
                connection.close();
                throw new InterruptedException();
            }

            // Mark
            boolean received = false;
            
            // Send to scope and participant
            try {
                received |= receiveInternal(message.message, message.scope, message.receiver);
            } catch (InterruptedException e) {
                connection.close();
                throw e;
            }

            // Delete 
            if (received) {
                message.delete();
                deleted = message;
            }
        }
        
        // Expunge
        if (deleted != null) {
            deleted.expunge();
        }
    }
    
    /**
     * Is there an working connection to receive?
     * 
     * @return
     */
    public boolean isReceivingConnected() {
        if(this.connection != null) {
            return this.connection.isReceivingConnected();
        }
        
        return false;
    };
    
    
    /**
     * Send a plain e-mail (no bus functionality)
     * 
     * @param recipient
     * @param subject
     * @param body
     * @throws BusException
     */
    public void sendPlain(String recipient, String subject, String body) throws BusException {
        this.connection.send(recipient, subject, body, null);
    }
}
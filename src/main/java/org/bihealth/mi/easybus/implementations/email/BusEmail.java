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
    /** Thread*/
    private Thread          thread;
  
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
                    while (true) {
                        try {
                            receiveEmails();
                        } catch (BusException e) {
                            // Stop thread
                            throw new RuntimeException(e);
                        }
                        Thread.sleep(millis);
                    }
                } catch (InterruptedException e) {
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
        this.thread.interrupt();
    }
    
    /**
     * Receives e-mails
     * @throws BusException 
     */
    private synchronized void receiveEmails() throws BusException {
        
        // Get mails
        BusEmail.BusEmailMessage deleted = null;
        for (BusEmail.BusEmailMessage message : connection.receive()) {
         
            // Mark
            boolean received = false;
            
            // Send to scope and participant
            received |= receiveInternal(message.message, message.scope, message.receiver);

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
}
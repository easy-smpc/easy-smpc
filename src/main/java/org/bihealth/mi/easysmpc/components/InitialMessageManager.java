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
package org.bihealth.mi.easysmpc.components;

import java.util.List;
import java.util.function.Consumer;

import org.bihealth.mi.easysmpc.resources.Resources;

import de.tu_darmstadt.cbs.emailsmpc.MessageInitial;

/**
 * Manages the connection to obtain the initial message
 * @author Felix Wirth
 *
 */
public abstract class InitialMessageManager {
    
    /** Stores the initial message together with an id
     * @author Felix Wirth
     */
    public static class MessageInitialWithIdString {
        
        /** Message */
        private final MessageInitial message;
        /** Id */
        private final String         id;
        /** Message string */
        private final String         messageString;
        
        /**
         * Creates a new instance
         * 
         * @param message
         * @param id
         * @param messageString
         */
        public MessageInitialWithIdString(MessageInitial message, String id, String messageString) {
            // Store
            this.message = message;
            this.id = id;
            this.messageString = messageString;
        }

        /**
         * @return the message
         */
        public MessageInitial getMessage() {
            return message;
        }

        /**
         * @return the id
         */
        public String getID() {
            return id;
        }

        /**
         * @return the messageString
         */
        protected String getMessageString() {
            return messageString;
        }
    }
    
    
    /** Action to perform with new messages */
    private final Consumer<List<MessageInitialWithIdString>> actionUpdateMessage;
    /** Stop flag */
    private volatile boolean                           stop = false;
    /** Action if error occurs */
    private final Consumer<String>                     actionError;

    public InitialMessageManager(Consumer<List<MessageInitialWithIdString>> actionUpdateMessage, Consumer<String> actionError) {
        
        // Store
        this.actionUpdateMessage = actionUpdateMessage;
        this.actionError = actionError;
    }

    /**
     * Stops the manager
     */
    public void stop() {
        this.stop = true;
    }

    /**
     * Start manager
     */
    public void start() {
        while(!stop) {
            // Update messages
            try {
                actionUpdateMessage.accept(retrieveMessages());
            } catch (IllegalStateException e) {
                actionError.accept(Resources.getString("DialogMessagePicker.5"));
                this.stop = true;
                throw new IllegalStateException("Unable to access messages", e);
            }
            
            // Sleep
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                // Empty
            }
        }
    }

    /**
     * Retrieve messages
     * @return
     */
    public abstract List<MessageInitialWithIdString> retrieveMessages() throws IllegalStateException;
    
    /**
     * Deletes a message
     * @param id
     */
    public abstract void deleteMessage(String id) throws IllegalStateException;

    /**
     * @return the actionError
     */
    protected Consumer<String> getActionError() {
        return actionError;
    }
}

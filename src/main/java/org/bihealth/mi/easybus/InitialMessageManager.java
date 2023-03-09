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

import java.util.List;
import java.util.function.Consumer;

import org.bihealth.mi.easysmpc.resources.Resources;

/**
 * Manages the connection to obtain initial messages
 * @author Felix Wirth
 *
 */
public abstract class InitialMessageManager {
    
    /** Action to perform with new messages */
    private final Consumer<List<BusMessage>> actionUpdateMessage;
    /** Stop flag */
    private volatile boolean                 stop = false;
    /** Action if error occurs */
    private final Consumer<String>           actionError;
    /** Check interval */
    private final int checkInterval;

    /**
     * Creates a new instance
     * 
     * @param actionUpdateMessage
     * @param actionError
     * @param 
     */
    public InitialMessageManager(Consumer<List<BusMessage>> actionUpdateMessage, Consumer<String> actionError, int checkInterval) {
        
        // Store
        this.actionUpdateMessage = actionUpdateMessage;
        this.actionError = actionError;
        this.checkInterval = checkInterval;
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
                Thread.sleep(checkInterval);
            } catch (InterruptedException e) {
                // Empty
            }
        }
    }

    /**
     * Retrieve messages
     * @return
     */
    public abstract List<BusMessage> retrieveMessages() throws IllegalStateException;

    /**
     * @return the actionError
     */
    protected void processError(String message) {
        actionError.accept(message);
        throw new IllegalStateException("Unable to process messages. Error message is" + message);
    }
}

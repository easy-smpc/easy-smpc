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

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.bihealth.mi.easybus.BusException;
import org.bihealth.mi.easybus.BusMessage;
import org.bihealth.mi.easybus.InitialMessageManager;
import org.bihealth.mi.easybus.MessageFilter;
import org.bihealth.mi.easybus.MessageManager;
import org.bihealth.mi.easysmpc.resources.Resources;

/**
 * Message manager for Email
 * 
 * @author Felix Wirth
 *
 */
public class InitialMessageManagerEmail extends InitialMessageManager {

    /** Message manager */
    private final MessageManager messageManager = new MessageManager(1024);
    /** Connection */
    private ConnectionEmail      connection;

    /**
     * Creates a new instance
     * 
     * @param actionUpdateMessage
     * @param actionError
     * @param settings
     * @param checkInterval
     * @throws BusException 
     */
    public InitialMessageManagerEmail(Consumer<List<BusMessage>> updateMessage,
                                            Consumer<Exception> actionError,
                                            ConnectionSettingsIMAP settings,
                                            int checkInterval) {
        super(updateMessage, actionError, checkInterval);
        
        // Store
        try {
            this.connection = new ConnectionIMAP(settings, false);
        } catch (BusException e) {
            processError(e);
        }
    }

    @Override
    public List<BusMessage> retrieveMessages() throws IllegalStateException {
        // Prepare
        List<BusMessage> result = new ArrayList<>();
        
        // Create filter for relevant messages
        MessageFilter filter = new MessageFilter() {
            @Override
            public boolean accepts(String messageDescription) {
                // Check if participant and scope is registered
                return messageDescription.contains(Resources.ROUND_0);
            }
        };
        
        try {
            // Get mails
            for (BusMessage message : connection.receive(filter)) {

                
                // Process with message manager
                BusMessage messageComplete = messageManager.mergeMessage(message, false);
                
                // Add to list if actually complete
                if (messageComplete != null) {
                    result.add(messageComplete);
                }
            }
        } catch (BusException | InterruptedException e) {
            // Pass error over
            this.processError(e);
        }

        // Return
        return result;
    }
}
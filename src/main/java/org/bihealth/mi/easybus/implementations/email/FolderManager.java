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
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bihealth.mi.easybus.implementations.email.ConnectionEmail.ConnectionEmailMessage;

import jakarta.mail.Flags.Flag;
import jakarta.mail.Folder;
import jakarta.mail.MessagingException;

/**
 * Manages a folder
 * 
 * @author Felix Wirth
 *
 */
public class FolderManager {
    
    /** Logger */
    private static final Logger LOGGER = LogManager.getLogger(FolderManager.class);
    /** The folder */
    private final Folder folder;
    /** Messages related to the folder */
    private final List<ConnectionEmailMessage> messages = new ArrayList<>();
    
    /** Creates a new instance
     * @param folder
     */
    public FolderManager(Folder folder) {
        this.folder = folder;
    }
    
    /**
     * Adds a message 
     * @param message
     */
    public void addMessage(ConnectionEmailMessage message) {
        this.messages.add(message);
    }

    /**
     * Deletes a message 
     * @param connectionEmailMessage
     */
    public void delete(ConnectionEmailMessage connectionEmailMessage) {
        try {
            connectionEmailMessage.getMessage().setFlag(Flag.DELETED, true);
            this.messages.remove(connectionEmailMessage);
        }
        catch (MessagingException e) {
            LOGGER.debug("Delete failed logged", new Date(), "delete failed", ExceptionUtils.getStackTrace(e));
            // Ignore, as this may be a result of non-transactional properties of the IMAP protocol
        }

        if (this.messages.isEmpty()) {
            try
            {
                if (folder != null && folder.isOpen()) {
                    folder.close(true);
                }

            } catch (MessagingException e) {
                LOGGER.debug("Closing folder failed logged", new Date(), "Closing folder failed", ExceptionUtils.getStackTrace(e));
                // Ignore, as this may be a result of non-transactional properties of the IMAP protocol
            }
        }
    }

    /**
     * Expunge 
     * @param connectionEmailMessage
     */
    public void expunge(ConnectionEmailMessage connectionEmailMessage) {
        try
        {
            if (folder != null && folder.isOpen()) {
                folder.expunge();
            }
        } catch (MessagingException e) {
            LOGGER.debug("Expunge failed logged", new Date(), "expunge failed", ExceptionUtils.getStackTrace(e));
            // Ignore, as this may be a result of non-transactional properties of the IMAP protocol
        }
    }
}

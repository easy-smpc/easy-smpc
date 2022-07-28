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
import java.io.Serializable;

/**
 * The message a party is sending.
 * TODO: This class can be removed as it is just
 * TODO: a wrapper around a string
 * 
 * @author Felix Wirth
 */
public class Message implements Serializable {
    
    /** SVUID */
    private static final long serialVersionUID = 7569290904692958620L;

    /**
     * Deserialize message from base64 encoded string
     *
     * @param msg the msg
     * @return the message
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws ClassNotFoundException the class not found exception
     */
    public static Message deserializeMessage(String msg) throws IOException, ClassNotFoundException {
        return new Message(msg);
    }
    
    /** The message content */
    private String messageContent;
    
    /**
     * Creates a new instance
     */
    public Message(String messageContent){
       this.messageContent = messageContent;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        Message other = (Message) obj;
        if (messageContent == null) {
            if (other.messageContent != null) return false;
        } else if (!messageContent.equals(other.messageContent)) return false;
        return true;
    }    
    
    /**
     * Returns the message
     */
    public String getMessage(){
        return this.messageContent;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((messageContent == null) ? 0 : messageContent.hashCode());
        return result;
    }

    /**
     * Serializes this messages to a string
     * 
     * @return
     * @throws IOException
     */
    public String serialize() throws IOException {
        return messageContent;
    }
}

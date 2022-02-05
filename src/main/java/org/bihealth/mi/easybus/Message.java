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

import java.io.Serializable;

/**
 * The message a party is sending
 * 
 * @author Felix Wirth
 *
 */
public class Message implements Serializable {
    
    /** SVUID */
    private static final long serialVersionUID = 1557145500504512577L;    
    /** The message content */
    private Object messageContent;
    /** Participant to respond to on this message */
    private Participant respondTo;
    
    /**
     * Creates a new instance
     */
    public Message(Object messageContent){
       this(messageContent, null);
    }
    
    public Message(Object messageContent, Participant respondTo) {
        this.messageContent = messageContent;
        this.respondTo = respondTo;
    }
    
    /**
     * Returns the message
     */
    public Object getMessage(){
        return this.messageContent;
    }
    
    /**
     * Returns the respond to participant
     */
    public Object getRespondTo(){
        return this.respondTo;
    }
}

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
 * Internal message used by bus implementations
 * 
 * @author Fabian Prasser
 */
public class BusMessage implements Serializable {
    
    /** SVUID */
    private static final long serialVersionUID = 8541645750687762860L;
    /** Receiver */
    protected final Participant     receiver;
    /** Scope */
    protected final Scope           scope;
    /** Message: A serialized base 64 encoded blob */
    protected final String          message;
    
    /**
     * Message
     * @param receiver
     * @param scope
     * @param message
     */
    public BusMessage(Participant receiver, Scope scope, String message) {
        this.receiver = receiver;
        this.scope = scope;
        this.message = message;
    }
    
    /**
     * Create from other message
     * @param other
     */
    public BusMessage(BusMessage other) {
        this(other.receiver, other.scope, other.message);
    }
    
    /** 
     * Can be used to clean up by bus implementations.
     * Implement if needed.
     * 
     * @throws BusException 
     */
    public void delete() throws BusException {
        // TODO: Implement if needed
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        BusMessage other = (BusMessage) obj;
        if (message == null) {
            if (other.message != null) return false;
        } else if (!message.equals(other.message)) return false;
        if (receiver == null) {
            if (other.receiver != null) return false;
        } else if (!receiver.equals(other.receiver)) return false;
        if (scope == null) {
            if (other.scope != null) return false;
        } else if (!scope.equals(other.scope)) return false;
        return true;
    }
    
    /** 
     * Can be used to clean up by bus implementations.
     * Implement if needed.
     * 
     * @throws BusException
     */
    public void expunge() throws BusException {
        // TODO: Implement if needed
    }
    
    /**
     * Return the message
     * @return the message
     */
    public String getMessage() {
        return message;
    }
    
    
    /**
     * Return the receiver
     * @return the receiver
     */
    public Participant getReceiver() {
        return receiver;
    }

    /**
     * Return the scope
     * @return the scope
     */
    public Scope getScope() {
        return scope;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((message == null) ? 0 : message.hashCode());
        result = prime * result + ((receiver == null) ? 0 : receiver.hashCode());
        result = prime * result + ((scope == null) ? 0 : scope.hashCode());
        return result;
    }
}

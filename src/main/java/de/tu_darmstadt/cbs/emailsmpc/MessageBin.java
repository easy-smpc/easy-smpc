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
package de.tu_darmstadt.cbs.emailsmpc;

import java.io.Serializable;

import de.tu_darmstadt.cbs.secretshare.ArithmeticShare;

/**
 * Bin for a message.
 * @author Tobias Kussel
 */
public class MessageBin implements Serializable {
    
    /**  SVUID. */
    private static final long serialVersionUID = -1447538147308567885L;
    
    /**
     * Gets the bin.
     *
     * @param mb the mb
     * @param numParticipants the num participants
     * @return the bin
     */
    public static Bin getBin(MessageBin mb, int numParticipants) {
        Bin bin = new Bin(mb.name, numParticipants);
        bin.setInShare(mb.share, 0);
        return bin;
    }
    
    /** The name. */
    public final String name;

    /** The share. */
    public final ArithmeticShare share;

    /**
     * Instantiates a new message bin.
     *
     * @param bin the bin
     * @param recipientId the recipient id
     */
    public MessageBin(Bin bin, int recipientId) {
        this.name = bin.name;
        this.share = bin.getOutShare(recipientId);
    }

    /**
     * Instantiates a new message bin.
     *
     * @param name the name
     * @param share the share
     */
    public MessageBin(String name, ArithmeticShare share) {
        this.name = name;
        this.share = share;
    }

    /**
     * Equals.
     *
     * @param o the o
     * @return true, if successful
     */
    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (!(o instanceof MessageBin))
            return false;
        MessageBin bin = (MessageBin) o;
        return this.name.equals(bin.name) && this.share.equals(bin.share);
    }

    /**
     * Hash code.
     *
     * @return the int
     */
    @Override
    public int hashCode() {
        int result = name.hashCode();
        return 31 * result + share.hashCode();
    }

    /**
     * To string.
     *
     * @return the string
     */
    @Override
    public String toString() {
        return name + ": " + share;
    }
}

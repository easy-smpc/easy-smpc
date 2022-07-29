package org.bihealth.mi.easybus;

import java.io.Serializable;

/**
 * Represents a fragment/splitted part of a BusMessage
 * 
 * @author Felix Wirth
 */
public class BusMessageFragment extends BusMessage implements Serializable {
    
    /** SVUID */
    private static final long serialVersionUID = 8935224381534818963L;
    /** Number of this message fragment */
    private final int         fragmentNumber;
    /** Total of fragments */
    private final int         numberOfFragments;
    /** Id of message (same over all fragments) */
    private final String      messageID;
    
    
    /**
     * Creates a new instance
     * 
     * @param receiver
     * @param scope
     * @param message
     * @param messageID
     * @param fragmentNumber
     * @param numberOfFragments
     * @param content
     */
    public BusMessageFragment(Participant receiver,
                              Scope scope,
                              String message,
                              String messageID,
                              int fragmentNumber,
                              int numberOfFragments) {
        // Super
        super(receiver, scope, message);
        
        // Check
        if(messageID == null) {
            throw new NullPointerException("Id and content can not be null!");
        }
        
        if(fragmentNumber >= numberOfFragments || numberOfFragments < 1) {
            throw new IllegalArgumentException("Number of fragments must be greater than 0 and fragment number must be lower than number of fragments");
        }
        
        // Store
        this.messageID = messageID;
        this.fragmentNumber = fragmentNumber;
        this.numberOfFragments = numberOfFragments;
    }
    
    /**
     * Create from other message
     * @param other
     */
    public BusMessageFragment(BusMessageFragment other) {
        this(other.receiver,
             other.scope,
             other.message,
             other.messageID,
             other.fragmentNumber,
             other.numberOfFragments);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!super.equals(obj)) return false;
        if (getClass() != obj.getClass()) return false;
        BusMessageFragment other = (BusMessageFragment) obj;
        if (fragmentNumber != other.fragmentNumber) return false;
        if (messageID == null) {
            if (other.messageID != null) return false;
        } else if (!messageID.equals(other.messageID)) return false;
        if (numberOfFragments != other.numberOfFragments) return false;
        return true;
    }
    
    /**
     * @return the fragmentNumber
     */
    public int getFragmentNumber() {
        return fragmentNumber;
    }
    
    /**
     * @return the id
     */
    public String getMessageID() {
        return messageID;
    }

    /**
     * @return the numberOfFragments
     */
    public int getNumberOfFragments() {
        return numberOfFragments;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + fragmentNumber;
        result = prime * result + ((messageID == null) ? 0 : messageID.hashCode());
        result = prime * result + numberOfFragments;
        return result;
    }
}

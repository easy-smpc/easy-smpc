package org.bihealth.mi.easybus;

import java.util.Objects;

/**
 * Represents a fragment/splitted part of a { @link org.bihealth.mi.easybus.message message}
 * 
 * @author Felix Wirth
 */
public class MessageFragment extends Message {
    
    /** SVUID */
    private static final long serialVersionUID = -14070272349211270L;
    /** Number of this message fragment */
    private final int         fragmentNumber;
    /** Total of fragments */
    private final int         numberOfFragments;
    /** Id of message (same over all fragments) */
    private final String      messageID;
    /** The fragment content */
    private final String      content;
    
    /**
     * Creates a new instance
     * 
     * @param messageID
     * @param fragmentNumber
     * @param numberOfFragments
     * @param content
     */
    public MessageFragment(String messageID, int fragmentNumber, int numberOfFragments, String content) {
        // Super
        super(content);
        
        // Check
        if(messageID == null || content == null) {
            throw new NullPointerException("Id and content can not be null!");
        }
        
        if(fragmentNumber >= numberOfFragments || numberOfFragments < 1) {
            throw new IllegalArgumentException("Number of fragments must be greater than 0 and fragment number must be lower than number of fragments");
        }
        
        // Store
        this.messageID = messageID;
        this.fragmentNumber = fragmentNumber;
        this.numberOfFragments = numberOfFragments;
        this.content = content;
    }

    /**
     * @return the fragmentNumber
     */
    public int getFragmentNumber() {
        return fragmentNumber;
    }

    /**
     * @return the numberOfFragments
     */
    public int getNumberOfFragments() {
        return numberOfFragments;
    }

    /**
     * @return the id
     */
    public String getMessageID() {
        return messageID;
    }

    @Override
    public int hashCode() {
        return Objects.hash(content, messageID, fragmentNumber, numberOfFragments);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        MessageFragment other = (MessageFragment) obj;
        return Objects.equals(content, other.content) && Objects.equals(messageID, other.messageID) &&
               fragmentNumber == other.fragmentNumber && numberOfFragments == other.numberOfFragments;
    }
    
    /**
     * Action to delete the message after it was read
     * 
     * @throws BusException
     */
    public void delete() throws BusException {
        // Empty
    }
    
    /**
     * Action to finish up message deletion. Usually only called for one fragment after delete() has been called for several messages
     *
     */
    public void finalize() throws BusException {
        // Empty
    }
}

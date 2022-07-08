package org.bihealth.mi.easybus;

import java.io.IOException;
import java.util.Objects;

/**
 * Represents a fragment/splitted part of a { @link org.bihealth.mi.easybus.message message}
 * 
 * @author Felix Wirth
 *
 */
public class MessageFragment extends Message {
    
    /** SVUID */
    private static final long serialVersionUID = -14070272349211270L;
    /** Number of this message fragment */
    private final int         splitNr;
    /** Total of fragments */
    private final int         splitTotal;
    /** Id of message (same over all fragments) */
    private final String      id;
    /** The fragment content */
    private final String      content;
    
    /**
     * Creates a new instance
     * 
     * @param id
     * @param splitNr
     * @param splitTotal
     * @param content
     */
    public MessageFragment(String id, int splitNr, int splitTotal, String content) {
        // Super
        super(content);
        
        // Check
        if(id == null || content == null) {
            throw new IllegalArgumentException("Id and content can not be null!");
        }
        
        if(splitNr >= splitTotal || splitTotal < 1) {
            throw new IllegalArgumentException("Please provide a number >= 1 for splitTotal and a splitNr < splitTotal");
        }
        
        // Store
        this.id = id;
        this.splitNr = splitNr;
        this.splitTotal = splitTotal;
        this.content = content;
    }

    /**
     * @return the splitNr
     */
    public int getSplitNr() {
        return splitNr;
    }

    /**
     * @return the splitTotal
     */
    public int getSplitTotal() {
        return splitTotal;
    }

    /**
     * @return the id
     */
    public String getId() {
        return id;
    }
    
    /**
     * Serializes this messages to string
     * 
     * @return
     * @throws IOException
     */
    public String serialize() throws IOException {
        return serializeMessage(this);
    }

    @Override
    public int hashCode() {
        return Objects.hash(content, id, splitNr, splitTotal);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        MessageFragment other = (MessageFragment) obj;
        return Objects.equals(content, other.content) && Objects.equals(id, other.id) &&
               splitNr == other.splitNr && splitTotal == other.splitTotal;
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

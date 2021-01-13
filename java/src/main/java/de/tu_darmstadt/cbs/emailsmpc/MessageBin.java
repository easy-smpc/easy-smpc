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

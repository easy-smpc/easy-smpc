package org.bihealth.mi.easybus;

/**
 * A message fragment extended with methods to finish up the message fragment usage
 * 
 * @author Felix Wirth
 *
 */
public abstract class MessageFragmentFinish extends MessageFragment {
    
    
    /** SVUID */
    private static final long serialVersionUID = 3097392175558299932L;

    /**
     * Creates a new instance
     * 
     * @param id
     * @param splitNr
     * @param splitTotal
     * @param content
     */
    public MessageFragmentFinish(MessageFragment fragment){
        // Super
        super(fragment.getId(), fragment.getSplitNr(), fragment.getSplitTotal(), fragment.getContent());
    }
    
    /**
     * Action to delete the message after it was read
     * 
     * @throws BusException
     */
    public abstract void delete() throws BusException;
    
    /**
     * Action to finish up message deletion. Usually only called for one fragment after delete() has been called for several messages
     *
     */
    public abstract void finalize() throws BusException;
 }
package org.bihealth.mi.easybus.implementations.email;

import org.bihealth.mi.easybus.BusException;
import org.bihealth.mi.easybus.MessageFragment;
import org.bihealth.mi.easybus.implementations.email.BusEmail.BusEmailMessage;

/**
 * A MessageFragmentFinish implementation for e-mail
 * 
 * @author Felix Wirth
 *
 */
public class MessageFragmentEmail extends MessageFragment {

    /** SVUID */
    private static final long serialVersionUID = 4271457895164547161L;
    /** Bus email message */
    private BusEmailMessage busEmailMessage;

    /**
     * Creates a new instance
     * 
     * @param fragment
     */
    public MessageFragmentEmail(MessageFragment fragment) {
        super(fragment.getMessageID(), fragment.getFragmentNumber(), fragment.getNumberOfFragments(), (String) fragment.getMessage());
    }

    /**
     * Creates a new instance
     * 
     * @param busEmailMessage
     */
    public MessageFragmentEmail(BusEmailMessage busEmailMessage) {
        this(busEmailMessage.message);
        
        this.busEmailMessage = busEmailMessage;
    }

    @Override
    public void delete() throws BusException {
        this.busEmailMessage.delete();
        
    }

    @Override
    public void finalize() throws BusException {
        this.busEmailMessage.expunge();        
    }
}

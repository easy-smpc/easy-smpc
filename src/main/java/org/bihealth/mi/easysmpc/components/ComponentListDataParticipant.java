package org.bihealth.mi.easysmpc.components;

import java.util.List;

import javax.swing.event.ChangeListener;

import de.tu_darmstadt.cbs.emailsmpc.Participant;

public abstract class ComponentListDataParticipant extends ComponentListData<Participant> {

    /** SVUID */
    private static final long serialVersionUID = -1598634754861356711L;

    /**
     * Creates a new instance
     * 
     * @param inputData
     * @param listener
     */
    public ComponentListDataParticipant(List<Participant> inputData, ChangeListener listener) {
        super(inputData, listener);
    }
    
    /**
     * Sets the e-mail address in the first the first entry and deactivates field if not already done
     * 
     * @param email
     */
    public abstract void setEMailAddress(String emailAddress);
    
    /**
     * Enables or disables the e-mail field in the first entry
     * 
     * @param enabled
     */
    public abstract void enableEMailField(boolean b);
}

package org.bihealth.mi.easysmpc.components;

import java.util.List;

import javax.swing.event.ChangeListener;

import de.tu_darmstadt.cbs.emailsmpc.Participant;

public interface ComponentListDataParticipant extends ChangeListener {
    
    /**
     * Sets the e-mail address in the first the first entry and deactivates field if not already done
     * 
     * @param email
     */
    public void setEMailAddress(String emailAddress);
    
    /**
     * Enables or disables the e-mail field in the first entry
     * 
     * @param enabled
     */
    public void enableEMailField(boolean b);
    
    /**
     * Returns whether all values are valid
     */
    public boolean areValuesValid();
    
    /**
     * Returns the participant
     * 
     * @return
     */
    public List<Participant> getParticipants();
    
    // TODO Change Return type to component itself?
    /**
     * Returns the participant
     * 
     * @return
     */
    public void setParticipants(List<Participant> participants);
    
    
    /**
     * Returns the number of the filled rows, thus the numbers of rows a list in getOutputtData would have
     * 
     * @return
     */
    public int getLength();
    
    /**
     * @return the listener
     */
    public ChangeListener getListener();
    
    /**
     * Removes empty lines in the data
     */
    public abstract void removeEmptyLines();

}

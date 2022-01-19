package org.bihealth.mi.easysmpc.components;

import java.util.List;

import javax.swing.event.ChangeListener;

import de.tu_darmstadt.cbs.emailsmpc.Participant;

/**
 * Allows to display participants with a list of the components EntryParticipant
 * 
 * @author Felix Wirth
 *
 */
public class EntryParticipantsView extends ComponentListDataParticipantEntryLines {

    /** SVUID */
    private static final long serialVersionUID = 8814286326744090255L;

    /**
     * Creates a new instance
     * 
     * @param inputData
     * @param listener
     * @param compareParticipant
     */
    public EntryParticipantsView(List<Participant> inputData, ChangeListener listener, ComponentCompare<Participant> compareParticipant) {
        // Super
        super(inputData, listener, false, compareParticipant);
    }

    @Override
    protected void addParticipant(EntryParticipant previous, String name, String email) {
        // Is this the own participant?
        boolean ownParticipant = name.equals("") || email.equals("") ? false: getCompareParticipant().isSame(new Participant(name, email));
        
        // Create participant
        EntryParticipant newNameEmailParticipantEntry = new EntryParticipant(name,
                                                                             email,
                                                                             false,
                                                                             false,
                                                                             ownParticipant);
        // Add to panel
        getPanelParticipants().add(newNameEmailParticipantEntry);
    }

    @Override
    public void removeEmptyLines() {
        // Not supported
        throw new UnsupportedOperationException();
    }

    @Override
    public void reset() {
        // Not supported
        throw new UnsupportedOperationException();
    }

    @Override
    public void setEMailAddress(String emailAddress) {
        // Not supported
        throw new UnsupportedOperationException();
        
    }

    @Override
    public void enableEMailField(boolean b) {
        // Not supported
        throw new UnsupportedOperationException();        
    }
}

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
     */
    public EntryParticipantsView(List<Participant> inputData, ChangeListener listener) {
        // Super
        super(inputData, listener, false);
    }

    @Override
    protected void addParticipant(EntryParticipant previous, String name, String emailAddress) {
        // TODO fix
        // EntryParticipant newNameEmailParticipantEntry = new
        // EntryParticipant(name,
        // emailAddress,
        // false,
        // false,
        // i == getApp().getModel()
        // .getOwnId());
        // getPanelParticipants().add(newNameEmailParticipantEntry);
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

package org.bihealth.mi.easysmpc.components;

import java.awt.BorderLayout;
import java.awt.Component;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.event.ChangeListener;

import de.tu_darmstadt.cbs.emailsmpc.Participant;

/**
 * Allows to display/edit participants with a list of the components
 * EntryParticipant
 * 
 * @author Felix Wirth
 *
 */
public abstract class ComponentListDataParticipantEntryLines extends ComponentListDataParticipant {

    /** SVUID */
    private static final long     serialVersionUID = -5311279647815163120L;
    /** Panel for participants */
    private final ScrollablePanel panelParticipants;
    /** Is entry editable */
    private boolean               editable;
    /** Compare Participant */
    private final ComponentCompare<Participant> compareParticipant;

    /**
     * Creates a new instance
     * 
     * @param inputData
     * @param listener
     * @param editable
     */
    protected ComponentListDataParticipantEntryLines(List<Participant> inputData,
                                                     ChangeListener listener,
                                                     boolean editable,
                                                     ComponentCompare<Participant> compareParticipant) {

        // Super
        super(inputData, listener);

        // Store
        this.editable = editable;
        this.compareParticipant = compareParticipant;

        // Init panel and layout
        this.panelParticipants = new ScrollablePanel();
        this.panelParticipants.setLayout(new BoxLayout(this.panelParticipants, BoxLayout.Y_AXIS));
        this.setLayout(new BorderLayout());
        this.add(panelParticipants, BorderLayout.CENTER);

        // Set initial data
        if (inputData != null) {
            for (Participant p : inputData) {
                addParticipant(null, p.name, p.emailAddress);
            }
        }
    }

    /**
     * Adds a new participant
     * 
     * @param previous
     * @param name
     * @param emailAddress
     */
    protected abstract void
              addParticipant(EntryParticipant previous, String name, String emailAddress);

    @Override
    protected List<Participant> collectOutputData() {
        // Prepare
        List<Participant> participants = new ArrayList<>();

        // Collect participants
        for (Component entry : this.panelParticipants.getComponents()) {
            Participant participant = new Participant(((EntryParticipant) entry).getLeftValue(),
                                                      ((EntryParticipant) entry).getRightValue());
            participants.add(participant);
        }

        // Return
        return participants;
    }

    /**
     * @return
     */
    protected ScrollablePanel getPanelParticipants() {
        return panelParticipants;
    }

    @Override
    public int getLength() {
        return panelParticipants.getComponents().length;
    }

    @Override
    public boolean areValuesValid() {
        // Check each participant
        for (Component c : this.panelParticipants.getComponents()) {
            if (!((EntryParticipant) c).areValuesValid()) { return false; }
        }

        // Return true as default
        return true;
    }

    /**
     * Remove empty lines
     */
    public abstract void removeEmptyLines();

    /**
     * Is the component editable
     * 
     * @return
     */
    public boolean isEditable() {
        return editable;
    }

    /**
     * @return the compareParticipant
     */
    public ComponentCompare<Participant> getCompareParticipant() {
        return compareParticipant;
    }
}

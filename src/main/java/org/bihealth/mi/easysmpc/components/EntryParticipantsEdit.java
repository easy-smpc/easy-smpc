package org.bihealth.mi.easysmpc.components;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.bihealth.mi.easysmpc.resources.Resources;

import de.tu_darmstadt.cbs.emailsmpc.Participant;

/**
 * Allows to edit participants with a list of the components EntryParticipant
 * 
 * @author Felix Wirth
 *
 */
public class EntryParticipantsEdit extends ComponentListDataParticipantEntryLines {

    /** SVUID */
    private static final long serialVersionUID = -1458229671322876363L;

    /**
     * Creates a new instance
     * 
     * @param inputData
     * @param listener
     * @param compareParticipant
     */
    public EntryParticipantsEdit(List<Participant> inputData,
                                 ChangeListener listener,
                                 ComponentCompare<Participant> compareParticipant) {
        // Super
        super(inputData, listener, true, compareParticipant);

        // If no participants exists add an empty one
        if (getPanelParticipants() != null && getPanelParticipants().getComponents().length == 0) {
            addParticipant(null, "", "");
        }
    }
    
    @Override
    public void setEMailAddress(String email) {

        // Get participant entry component
        final EntryParticipant entry = ((EntryParticipant) this.getPanelParticipants()
                                                               .getComponents()[0]);

        // Change in new thread
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                // No entry in field allowed
                if (entry.isRightEnabled()) {
                    entry.setRightEnabled(false);
                }

                // Set e-mail address
                if (!entry.getRightValue().equals(email)) {
                    entry.setRightValue(email);
                }
            }
        });
    }

    @Override
    public void enableEMailField(boolean enabled) {
        // Get entry
        final EntryParticipant entry = ((EntryParticipant) this.getPanelParticipants()
                                                               .getComponents()[0]);

        // Change in new thread
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                if (!entry.isRightEnabled()) {
                    // Allow entry in field
                    entry.setRightEnabled(true);
                }
            }
        });
    }

    /**
     * Removes a participant
     * 
     * @param entry
     */
    private void removeParticipant(EntryParticipant entry) {

        // Check whether it's the last entry
        if (getPanelParticipants().getComponentCount() == 1) {
            JOptionPane.showMessageDialog(this,
                                          Resources.getString("PerspectiveCreate.errorTooFewEntries"));
            return;
        }

        // Remove and update
        getPanelParticipants().remove(entry);
        this.stateChanged(new ChangeEvent(this));
        getPanelParticipants().revalidate();
        getPanelParticipants().repaint();
    }

    /**
     * Remove empty lines
     */
    @Override
    public void removeEmptyLines() {

        // Collect to remove
        List<EntryParticipant> participantsToRemove = new ArrayList<>();
        for (Component entry : getPanelParticipants().getComponents()) {
            // Remove participants if both fields empty
            if (((EntryParticipant) entry).isEmpty()) {
                participantsToRemove.add((EntryParticipant) entry);
            }
        }

        // Actually remove
        for (EntryParticipant entry : participantsToRemove) {
            if (getPanelParticipants().getComponentCount() > 1) {
                removeParticipant(entry);
            }
        }
    }

    /**
     * Adds a new line of participant entry
     * 
     * @param previous
     * @param name
     * @param email
     * @param enabled
     */
    protected void addParticipant(EntryParticipant previous, String name, String email) {

        // Find index
        int index = Arrays.asList(getPanelParticipants().getComponents()).indexOf(previous);
        index = index == -1 ? 0 : index + 1;
        
        // Is this the own participant?
        boolean ownParticipant = name.equals("") || email.equals("") ? false: getCompareParticipant().isSame(new Participant(name, email));
        
        // Create and add entry
        EntryParticipant entry = new EntryParticipant(name, email, isEnabled(), isEnabled(), ownParticipant);
        entry.setChangeListener(this);
        entry.setAddListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addParticipant(entry, "", "");
            }
        });
        entry.setRemoveListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                removeParticipant(entry);
            }
        });
        getPanelParticipants().add(entry, index);
        getPanelParticipants().revalidate();
        getPanelParticipants().repaint();

        // Set change event
        if (getListener() != null) {
            getListener().stateChanged(new ChangeEvent(this));
        }
    }

    @Override
    public void reset() {
        // Remove
        getPanelParticipants().removeAll();

        // Add a single line
        addParticipant(null, "", "");
    }
}

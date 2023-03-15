/* 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.bihealth.mi.easysmpc;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.bihealth.mi.easybus.implementations.local.ConnectionSettingsManual;
import org.bihealth.mi.easysmpc.components.ComponentTextField;
import org.bihealth.mi.easysmpc.components.ComponentTextFieldValidator;
import org.bihealth.mi.easysmpc.components.EntryBin;
import org.bihealth.mi.easysmpc.components.EntryParticipant;
import org.bihealth.mi.easysmpc.components.ScrollablePanel;
import org.bihealth.mi.easysmpc.resources.Resources;

import de.tu_darmstadt.cbs.emailsmpc.Bin;
import de.tu_darmstadt.cbs.emailsmpc.Participant;

/**
 * A perspective
 * 
 * @author Fabian Prasser
 * @author Felix Wirth
 */
public class Perspective1ACreate extends Perspective implements ChangeListener {

    /** Panel for participants */
    private ScrollablePanel               panelParticipants;

    /** Panel for bins */
    private ScrollablePanel               panelBins;

    /** Text field containing title of study */
    private ComponentTextField            fieldTitle;

    /** Save button */
    private JButton                       buttonSave;

    /**
     * Creates the perspective
     * @param app
     */
    protected Perspective1ACreate(App app) {
        super(app, Resources.getString("PerspectiveCreate.0"), 1, false); //$NON-NLS-1$
    }

    /**
     * Reacts on all changes in any components
     */
    public void stateChanged(ChangeEvent e) {
        // Is saving possible?
        this.buttonSave.setEnabled(this.areValuesValid());
    }

    /**
     * Loads and sets bin names and data from a file
     */
    private void actionLoadFromFile() {
        Map<String, String> data = getApp().getDataFromFile();
        if (data != null) {
            this.panelBins.removeAll();
            EntryBin previousBin = null;
            List<String> names = new ArrayList<>();
            List<String> values = new ArrayList<>();
            List<Boolean>  enabled = new ArrayList<>();
            for (Entry<String, String> entry : data.entrySet()) {
                names.add(entry.getKey());
                values.add(entry.getValue());
                enabled.add(true);
            }
            addBin(previousBin, names, values, enabled);
            this.stateChanged(new ChangeEvent(this));
        }   
    }

    /**
     * Removes empty lines in participants and bins
     */
    private void actionRemoveEmptyLines() {
        // Collect to remove
        List<EntryParticipant> participantsToRemove = new ArrayList<>();
        for (Component entry : this.panelParticipants.getComponents()) {
                // Remove participants if both fields empty
                if (((EntryParticipant) entry).isEmpty()) {
                    participantsToRemove.add((EntryParticipant) entry);
                }
        }
        // Actually remove
        for (EntryParticipant entry : participantsToRemove) {
            if (this.panelParticipants.getComponentCount() > 1) {
                removeParticipant(entry);
            }
        }
        
        // Collect to remove
        List<EntryBin> binsToRemove = new ArrayList<>();
        for (Component entry : this.panelBins.getComponents()) {
                // Remove bin if left field empty and right field empty or zero
                if (((EntryBin) entry).isEmpty()) {
                    binsToRemove.add((EntryBin) entry);
                }
        }
        
        // Actually remove
        for (EntryBin entry : binsToRemove) {
            if (this.panelBins.getComponentCount() > 1) {
                removeBin((EntryBin) entry);
            }
        }
    }

    /**
     * Save the project and proceed.
     */
    private void actionSave() {
       
        // Check whether at least three participants
        if (this.panelParticipants.getComponents().length < 3) {
            JOptionPane.showMessageDialog(getPanel(), Resources.getString("PerspectiveCreate.notEnoughParticipants"));
            return;
        }
        
        // Collect participants
        List<Participant> participants = new ArrayList<>();
        for (Component entry : this.panelParticipants.getComponents()) {
            Participant participant = new Participant(((EntryParticipant)entry).getLeftValue(),
                                                      ((EntryParticipant)entry).getRightValue());
            participants.add(participant);
        }
        
        // Collect bins
        List<Bin> bins = new ArrayList<>();
        for (Component entry : this.panelBins.getComponents()) {
            Bin bin = new Bin(((EntryBin)entry).getLeftValue());
            bin.initialize(participants.size());
            bin.shareValue(new BigDecimal(((EntryBin)entry).getRightValue().trim().replace(',','.')), Resources.FRACTIONAL_BITS);
            bins.add(bin);
        }
        
        // Check whether there are duplicates
        for (int i = 0; i < bins.size(); i++) {
            for (int j = i + 1; j < bins.size(); j++) {
                if (bins.get(i).name.equals(bins.get(j).name)) {
                    JOptionPane.showMessageDialog(getPanel(), Resources.getString("PerspectiveCreate.DuplicateBins"));
                    return;
                }
            }
        }

        // Check whether there are duplicates
        for (int i = 0; i < participants.size(); i++) {
            for (int j = i + 1; j < participants.size(); j++) {
                if (participants.get(i).name.equals(participants.get(j).name) ||
                    participants.get(i).emailAddress.equals(participants.get(j).emailAddress)) {
                    JOptionPane.showMessageDialog(getPanel(), Resources.getString("PerspectiveCreate.DuplicateParticipants"));
                    return;
                }
            }
        }
        // Initialize study
        getApp().actionCreateDone(this.fieldTitle.getText(), participants.toArray(new Participant[participants.size()]), bins.toArray(new Bin[bins.size()]), getApp().getConnectionSettings());
    }

    /**
     * Adds several bins at once
     * @param previous
     * @param names
     * @param values
     * @param enabled
     * @return
     */
    private EntryBin addBin(EntryBin previous, List<String> names, List<String> values, List<Boolean> enabled) {

        // Find index
        int index = Arrays.asList(this.panelBins.getComponents()).indexOf(previous);
        index = index == -1 ? 0 : index + 1;
        
        // Create and add entries
        EntryBin entry = null;
        for (int i = 0; i < names.size(); i++) {
            entry = new EntryBin(names.get(i), enabled.get(i), values.get(i), enabled.get(i), enabled.get(i));
            final EntryBin _entry = entry;
            entry.setChangeListener(this);
            entry.setAddListener(new ActionListener() {
               @Override
                public void actionPerformed(ActionEvent e) {
                   addBin(_entry, "", "", true);
                } 
            });
            entry.setRemoveListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    removeBin(_entry);
                }
            });
            this.panelBins.add(entry, index);
            index++;
        }
        
        // Update GUI
        this.panelBins.revalidate();
        this.panelBins.repaint();
        this.stateChanged(new ChangeEvent(this));
        return entry;
    }

    /**
     * Adds a new line for bin entry
     * @param enabled
     */
    private EntryBin addBin(EntryBin previous, String name, String value, boolean enabled) {
        return addBin(previous, Arrays.asList(name), Arrays.asList(value), Arrays.asList(enabled));
    }

    /**
     * Adds a new line for participant entry
     * @param previous
     * @param enabled
     */
    private void addParticipant(EntryParticipant previous, boolean enabled) {
        
        // Find index
        int index = Arrays.asList(this.panelParticipants.getComponents()).indexOf(previous);
        index = index == -1 ? 0 : index + 1;
        
        // Create and add entry
        EntryParticipant entry = new EntryParticipant("", "", enabled, enabled, false);
        entry.setChangeListener(this);
        entry.setAddListener(new ActionListener() {
           @Override
            public void actionPerformed(ActionEvent e) {
               addParticipant(entry, true);
            } 
        });
        entry.setRemoveListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                removeParticipant(entry);
            }
        });
        this.panelParticipants.add(entry, index);
        this.panelParticipants.revalidate();
        this.panelParticipants.repaint();
        this.stateChanged(new ChangeEvent(this));
    }

    /**
     * Checks all values for validity
     * @return
     */
    private boolean areValuesValid() {
        
        // Check participants
        if (this.panelParticipants != null) {
            for (Component c : this.panelParticipants.getComponents()) {
                if (!((EntryParticipant) c).areValuesValid()) { return false; }
            }
        }
        
        // Check bins
        if (this.panelBins != null) {
            for (Component c : this.panelBins.getComponents()) {
                if (!((EntryBin) c).areValuesValid()) { return false; }
            }
        }
      
        // Check title
        if (fieldTitle != null) {
            if (!fieldTitle.isValueValid()) { return false; }
        }
        // Done
        return true;
    }


    /**
     * Removes a bin
     * @param entry
     */
    private void removeBin(EntryBin entry) {
        
        // Check whether it's the last entry
        if (this.panelBins.getComponentCount() == 1) {
            JOptionPane.showMessageDialog(getPanel(), Resources.getString("PerspectiveCreate.errorTooFewEntries"));
            return;
        }
        
        // Remove and update
        this.panelBins.remove(entry);
        this.stateChanged(new ChangeEvent(this));
        this.panelBins.revalidate();
        this.panelBins.repaint();
    }
    
    /**
     * Removes a participant
     * @param entry
     */
    private void removeParticipant(EntryParticipant entry) {
        
        // Check whether it's the last entry
        if (this.panelParticipants.getComponentCount() == 1) {
            JOptionPane.showMessageDialog(getPanel(), Resources.getString("PerspectiveCreate.errorTooFewEntries"));
            return;
        }
        
        // Remove and update
        this.panelParticipants.remove(entry);
        this.stateChanged(new ChangeEvent(this));
        this.panelParticipants.revalidate();    
        this.panelParticipants.repaint();
    }

    /**
     *Creates and adds UI elements
     */
    @Override
    protected void createContents(JPanel panel) {

        // Layout
        panel.setLayout(new BorderLayout());

        // General data data of study
        JPanel generalDataPanel = new JPanel();
        generalDataPanel.setLayout(new GridLayout(2, 1, Resources.ROW_GAP, Resources.ROW_GAP));
        panel.add(generalDataPanel, BorderLayout.NORTH);
        generalDataPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED),
                                                         Resources.getString("PerspectiveCreate.General"),
                                                         TitledBorder.LEFT,
                                                         TitledBorder.DEFAULT_POSITION));
        
        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new BorderLayout(Resources.ROW_GAP, Resources.ROW_GAP));
        titlePanel.add(new JLabel(Resources.getString("PerspectiveCreate.studyTitle")), BorderLayout.WEST);
        this.fieldTitle = new ComponentTextField(new ComponentTextFieldValidator() {
            @Override
            public boolean validate(String text) {
                return !text.trim().isEmpty();
            }
        });
        this.fieldTitle.setChangeListener(this);
        titlePanel.add(this.fieldTitle, BorderLayout.CENTER);
        
        // Panel for exchange config
        JPanel automaticExchangePanel = new JPanel();
        automaticExchangePanel.setLayout(new BoxLayout(automaticExchangePanel, BoxLayout.X_AXIS));
       
        // Add
        generalDataPanel.add(titlePanel);
        automaticExchangePanel.add(new JLabel(Resources.getString("PerspectiveCreate.AutomatedMailbox")));
        generalDataPanel.add(automaticExchangePanel);
        
        // Central panel
        JPanel central = new JPanel();
        central.setLayout(new GridLayout(2, 1));
        panel.add(central, BorderLayout.CENTER);
        
        // Participants
        this.panelParticipants = new ScrollablePanel();
        this.panelParticipants.setLayout(new BoxLayout(this.panelParticipants, BoxLayout.Y_AXIS));     
        JScrollPane pane = new JScrollPane(panelParticipants, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        pane.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED),
                                                                           Resources.getString("PerspectiveCreate.participants"),
                                                                           TitledBorder.LEFT,
                                                                           TitledBorder.DEFAULT_POSITION));
        central.add(pane, BorderLayout.NORTH);

        // Bins
        this.panelBins = new ScrollablePanel();
        this.panelBins.setLayout(new BoxLayout(this.panelBins, BoxLayout.Y_AXIS));
        pane = new JScrollPane(panelBins, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        pane.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED),
                                                                    Resources.getString("PerspectiveCreate.bins"),
                                                                    TitledBorder.LEFT,
                                                                    TitledBorder.DEFAULT_POSITION));

        central.add(pane, BorderLayout.SOUTH);
       
        // Buttons pane
        JPanel buttonsPane = new JPanel();
        buttonsPane.setLayout(new GridLayout(3, 1));
       
        // Load from file button      
        JButton loadFromFile = new JButton(Resources.getString("PerspectiveCreate.LoadFromFile"));
        loadFromFile.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                actionLoadFromFile();
            }
        });
        buttonsPane.add(loadFromFile, 0, 0);        
        
        // Remove empty lines button
        JButton removeEmptylines = new JButton(Resources.getString("PerspectiveCreate.removeEmptyLines"));
        removeEmptylines.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                actionRemoveEmptyLines();
            }
        });
        buttonsPane.add(removeEmptylines, 0, 1);
        
        // Save button
        buttonSave = new JButton(Resources.getString("PerspectiveCreate.save"));
        buttonSave.setEnabled(this.areValuesValid());
        buttonSave.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                actionSave();
            }
        });
        buttonsPane.add(buttonSave, 0, 2);
        panel.add(buttonsPane, BorderLayout.SOUTH);
        

    }

    /**
     * Initialize perspective based on model
     */
    @Override
    protected void initialize() {
        
        // Clear
        this.panelParticipants.removeAll();
        this.panelBins.removeAll();
        this.fieldTitle.setText("");
        
        
        // Add initial bin
        this.addParticipant(null, true);
        this.addBin(null, "", "", true);
        
        // Set first participant's email when in automated mode 
        if(getApp().getConnectionSettings() != null && !(getApp().getConnectionSettings() instanceof ConnectionSettingsManual)) {
            ((EntryParticipant) panelParticipants.getComponents()[0]).setRightEnabled(false);
            ((EntryParticipant) panelParticipants.getComponents()[0]).setRightValue(getApp().getConnectionSettings().getIdentifier());
        }
        
        // Update
        this.stateChanged(new ChangeEvent(this));
    }
}
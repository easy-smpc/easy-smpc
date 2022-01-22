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
import java.util.prefs.BackingStoreException;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.bihealth.mi.easybus.BusException;
import org.bihealth.mi.easybus.implementations.email.ConnectionIMAPSettings;
import org.bihealth.mi.easysmpc.components.ComponentCompare;
import org.bihealth.mi.easysmpc.components.ComponentListDataParticipant;
import org.bihealth.mi.easysmpc.components.ComponentTextField;
import org.bihealth.mi.easysmpc.components.ComponentTextFieldValidator;
import org.bihealth.mi.easysmpc.components.DialogEmailConfig;
import org.bihealth.mi.easysmpc.components.EntryBin;
import org.bihealth.mi.easysmpc.components.EntryParticipantsEdit;
import org.bihealth.mi.easysmpc.components.ScrollablePanel;
import org.bihealth.mi.easysmpc.resources.Connections;
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

    /** Allows to set a custom text for each object in the list */
    private class CustomRenderer extends DefaultListCellRenderer {
        /** SVUID */
        private static final long serialVersionUID = 1L;

        @Override
        public Component getListCellRendererComponent(JList<?> list,
                                                      Object value,
                                                      int index,
                                                      boolean isSelected,
                                                      boolean cellHasFocus) {
            JLabel label = (JLabel) super.getListCellRendererComponent(list,
                                                                       value,
                                                                       index,
                                                                       isSelected,
                                                                       cellHasFocus);
            if (value != null) {
                label.setText(((ConnectionIMAPSettings) value).getEmailAddress());
            }
            else {
                label.setText(Resources.getString("EmailConfig.19"));
            }
            return label;
        }
    }

    /** Panel for bins */
    private ScrollablePanel                   panelBins;
    
    /** Panel for participants */
    private ComponentListDataParticipant      panelEntryParticipants;

    /** Text field containing title of study */
    private ComponentTextField                fieldTitle;

    /** Save button */
    private JButton                           buttonSave;

    /** Add configuration e-mail box */
    private JButton                           buttonAddMailbox;

    /** Combo box to select mail box configuration */
    private JComboBox<ConnectionIMAPSettings> comboSelectMailbox;

    /** Add configuration e-mail box */
    private JButton                           buttonRemoveMailbox;

    /** Edit configuration e-mail box */
    private JButton                           buttonEditMailbox;
    
    /** Has e-mail config been checked? */
    private boolean                           emailconfigCheck;
    
    /** Central panel */
    private JPanel central;
    
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
        if (this.buttonSave != null) {
            this.buttonSave.setEnabled(this.areValuesValid());
        }
        // Can a mailbox be added or removed
        if (this.comboSelectMailbox.getSelectedItem() != null) {
            this.buttonEditMailbox.setEnabled(true);
            this.buttonRemoveMailbox.setEnabled(true);
        } else {
            this.buttonEditMailbox.setEnabled(false);
            this.buttonRemoveMailbox.setEnabled(false);
        }
        
        // Can a mailbox be added or removed
        if (this.comboSelectMailbox.getSelectedItem() != null) {
            this.buttonEditMailbox.setEnabled(true);
            this.buttonRemoveMailbox.setEnabled(true);
        } else {
            this.buttonEditMailbox.setEnabled(false);
            this.buttonRemoveMailbox.setEnabled(false);
        }
        
        // If participants panels exists and automated mode selected => set e-mail address of creator automatically 
        if (this.panelEntryParticipants != null &&
            this.panelEntryParticipants.getLength() >= getApp().getModel().getOwnId() + 1) {
            if (this.comboSelectMailbox.getSelectedItem() != null) {
                // Set email address and deactivate
                this.panelEntryParticipants.setEMailAddress(((ConnectionIMAPSettings) comboSelectMailbox.getSelectedItem()).getEmailAddress());
            } else {
                // Enables e-mail field if not already done
                this.panelEntryParticipants.enableEMailField(true);
            }
        }
    }

    /**
     * Adds an e-mail configuration
     */
    private void actionAddEMailConf() {
        // Get new settings
        ConnectionIMAPSettings newSettings = new DialogEmailConfig(null, getApp()).showDialog();
        
        if (newSettings != null) {
            // Update connections in preferences
            Connections.addOrUpdate(newSettings);
            
            // Reset combo  box
            comboSelectMailbox.removeAllItems();
            for(ConnectionIMAPSettings settings: getEmailConfig()) {
                this.comboSelectMailbox.addItem(settings);
                
                // Set selected
                if(settings != null && settings.getEmailAddress().equals(newSettings.getEmailAddress())) {
                    settings.setPassword(newSettings.getPassword());
                    this.comboSelectMailbox.setSelectedItem(settings);
                }
            }
            
            // Set checked
            this.emailconfigCheck = true;
        }
        this.stateChanged(new ChangeEvent(this));
    }
    
    /**
     * Edits an e-mail configuration
     */
    private void actionEditEMailConf() {
        
        // Get new settings
        ConnectionIMAPSettings newSettings = new DialogEmailConfig((ConnectionIMAPSettings) this.comboSelectMailbox.getSelectedItem(),
                                                                   getApp()).showDialog();
        
        // Alter combo box if new settings given
        if (newSettings != null) {
            // Update connections in preferences
            Connections.addOrUpdate(newSettings);
            
            // Reset combo  box
            comboSelectMailbox.removeAllItems();
            for(ConnectionIMAPSettings settings: getEmailConfig()) {
                this.comboSelectMailbox.addItem(settings);
                
                // Set selected
                if(settings != null && settings.getEmailAddress().equals(newSettings.getEmailAddress())) {
                    settings.setPassword(newSettings.getPassword());
                    this.comboSelectMailbox.setSelectedItem(settings);
                }
            }
            
            // Set checked
            this.emailconfigCheck = true;
        }
        
        // Change state
        this.stateChanged(new ChangeEvent(this));
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
     * Removes an e-mail configuration
     */
    private void actionRemoveEMailConf() {
        try {
            Connections.remove((ConnectionIMAPSettings) this.comboSelectMailbox.getSelectedItem());
            this.comboSelectMailbox.removeItem(this.comboSelectMailbox.getSelectedItem());
        } catch (BackingStoreException e) {
            JOptionPane.showMessageDialog(getPanel(), Resources.getString("PerspectiveCreate.ErrorDeletePreferences"), Resources.getString("PerspectiveCreate.Error"), JOptionPane.ERROR_MESSAGE);
        }
        this.stateChanged(new ChangeEvent(this));  
    }

    /**
     * Removes empty lines in participants and bins
     */
    private void actionRemoveEmptyLines() {
        this.panelEntryParticipants.removeEmptyLines();
        
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
        if (this.panelEntryParticipants.getLength() < 3) {
            JOptionPane.showMessageDialog(getPanel(), Resources.getString("PerspectiveCreate.notEnoughParticipants"));
            return;
        }
        
        // Check e-mail configuration if not done so far
        if (comboSelectMailbox.getSelectedItem() != null && !emailconfigCheck) {
            
            try {
                if (!((ConnectionIMAPSettings) comboSelectMailbox.getSelectedItem()).isValid(true)) {
                    throw new BusException("Connection error");
                }
            } catch (BusException e) {
                // Error message
                JOptionPane.showMessageDialog(getPanel(), Resources.getString("PerspectiveCreate.emailConnectionNotWorking"));
                return;
            }
        }
        
        // Collect particpants
        List<Participant> participants = panelEntryParticipants.getParticipants();
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
        getApp().actionCreateDone(this.fieldTitle.getText(), participants.toArray(new Participant[participants.size()]), bins.toArray(new Bin[bins.size()]), (ConnectionIMAPSettings) comboSelectMailbox.getSelectedItem());
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
     * Checks all values for validity
     * @return
     */
    private boolean areValuesValid() {        
        
        // Check participants
        if (panelEntryParticipants == null || !panelEntryParticipants.areValuesValid()) { return false; }
        
        // Check bins
        for (Component c : this.panelBins.getComponents()) {
            if (!((EntryBin) c).areValuesValid()) { 
                return false;
            }
        }
        
        // Check title
        if (!fieldTitle.isValueValid()) {
            return false;
        }
        
        // Done
        return true;
    }


    /**
     * Returns previous configurations
     * 
     * @return
     */
    private ConnectionIMAPSettings[] getEmailConfig() {
        try {
            // Read from preferences
            ArrayList<ConnectionIMAPSettings> configFromPreferences = Connections.list();
            // Add null for non-automatic
            configFromPreferences.add(0, null);
            return configFromPreferences.toArray(new ConnectionIMAPSettings[configFromPreferences.size()]);
        } catch (BackingStoreException e) {
            JOptionPane.showMessageDialog(getPanel(), Resources.getString("PerspectiveCreate.ErrorLoadingPreferences"), Resources.getString("PerspectiveCreate.Error"), JOptionPane.ERROR_MESSAGE);
            return null;
        }
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
        
        // Panel for automatic e-mail config
        JPanel automaticEMailPanel = new JPanel();
        automaticEMailPanel.setLayout(new BoxLayout(automaticEMailPanel, BoxLayout.X_AXIS));
       
        // Check box to use mail box automatically
        comboSelectMailbox = new JComboBox<>(getEmailConfig());
        comboSelectMailbox.setRenderer(new CustomRenderer());
        comboSelectMailbox.addActionListener(new ActionListener() {            
            @Override
            public void actionPerformed(ActionEvent e) {
                stateChanged(new ChangeEvent(this));
            }
        });
        
        // Button to add e-mail config
        buttonAddMailbox = new JButton(Resources.getString("PerspectiveCreate.OpenEMailConfigAdd"));
        buttonAddMailbox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
               actionAddEMailConf();
            }
        });
        
        // Button to edit e-mail config
        buttonEditMailbox = new JButton(Resources.getString("PerspectiveCreate.OpenEMailConfigEdit"));
        buttonEditMailbox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                actionEditEMailConf();
            }
        });
        
        // Button to remove e-mail config
        buttonRemoveMailbox = new JButton(Resources.getString("PerspectiveCreate.OpenEMailConfigRemove"));
        buttonRemoveMailbox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                actionRemoveEMailConf();
            }
        });
        
        // Add
        generalDataPanel.add(titlePanel);       
        automaticEMailPanel.add(new JLabel(Resources.getString("PerspectiveCreate.AutomatedMailbox")));
        automaticEMailPanel.add(comboSelectMailbox);
        automaticEMailPanel.add(buttonAddMailbox);
        automaticEMailPanel.add(buttonEditMailbox);
        automaticEMailPanel.add(buttonRemoveMailbox);
        generalDataPanel.add(automaticEMailPanel);
        
        // Central panel
        central = new JPanel();
        central.setLayout(new GridLayout(2, 1));
        panel.add(central, BorderLayout.CENTER);
       
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
        
        // Clear existing fields
        fieldTitle.setText("");
        emailconfigCheck = false;
        central.removeAll();
        
        // Participants
        panelEntryParticipants = new EntryParticipantsEdit(this, new ComponentCompare<Participant>() {          
            @Override
            public boolean isSame(Participant participant) {
                // Compare if participant is the same as own participant
                Participant ownParticipant = getApp().getModel().getParticipantFromId(getApp().getModel().getOwnId());
                return participant.name.equals(ownParticipant.name) && participant.emailAddress.equals(ownParticipant.emailAddress);
            }
        });
        JScrollPane pane = new JScrollPane((EntryParticipantsEdit) panelEntryParticipants,
                                           JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                                           JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
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
        
        // Add initial
        this.addBin(null, "", "", true);
        
        // Update
        this.stateChanged(new ChangeEvent(this));        
        getPanel().revalidate();
        getPanel().repaint();

    }
}
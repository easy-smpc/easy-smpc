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
import java.math.BigInteger;
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

import org.bihealth.mi.easybus.implementations.email.ConnectionIMAPSettings;
import org.bihealth.mi.easysmpc.components.ComponentTextField;
import org.bihealth.mi.easysmpc.components.ComponentTextFieldValidator;
import org.bihealth.mi.easysmpc.components.DialogEmailConfig;
import org.bihealth.mi.easysmpc.components.EntryBin;
import org.bihealth.mi.easysmpc.components.EntryParticipant;
import org.bihealth.mi.easysmpc.components.ScrollablePanel;
import org.bihealth.mi.easysmpc.dataimport.ImportPreferences;
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

    /** Panel for participants */
    private ScrollablePanel    participants;

    /** Panel for bins */
    private ScrollablePanel    bins;

    /** Text field containing title of study */
    private ComponentTextField title;
    
    /** Save button */
    private JButton            save;
    
    /** Add configuration e-mail box*/
    private JButton addEmailboxButton;
    
    /** Combo box to select mail box configuration */
    private JComboBox<ConnectionIMAPSettings> selectMailboxCombo;
    
    /** Add configuration e-mail box */
    private JButton removeEmailboxButton;
    
    /** Edit configuration e-mail box */
    private JButton editEmailboxButton;;
    
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
        this.save.setEnabled(this.areValuesValid());
        // Can a mailbox be added or removed
        if (this.selectMailboxCombo.getSelectedItem() != null) {
            this.editEmailboxButton.setEnabled(true);
            this.removeEmailboxButton.setEnabled(true);
        } else {
            this.editEmailboxButton.setEnabled(false);
            this.removeEmailboxButton.setEnabled(false);
        }               
    }

    /**
     * Adds an e-mail configuration
     */
    private void actionAddEMailConf() {
        ConnectionIMAPSettings settings = new DialogEmailConfig(null, getApp()).showDialog();
        if(settings != null) {
            ImportPreferences.setConnectionIMAPSetting(settings);
            this.selectMailboxCombo.addItem(settings);
            this.selectMailboxCombo.setSelectedItem(settings);
        }
        this.stateChanged(new ChangeEvent(this));
    }
    
    /**
     * Edits an e-mail configuration
     */
    private void actionEditEMailConf() {
        ConnectionIMAPSettings settings = new DialogEmailConfig((ConnectionIMAPSettings) this.selectMailboxCombo.getSelectedItem(), getApp()).showDialog();
        if(settings != null) {
            ImportPreferences.setConnectionIMAPSetting(settings);
            this.selectMailboxCombo.addItem(settings);
            this.selectMailboxCombo.setSelectedItem(settings);
        }
        this.stateChanged(new ChangeEvent(this));        
    }

    /**
     * Loads and sets bin names and data from a file
     */
    private void actionLoadFromFile() {
        Map<String, String> data = getApp().getDataFromFile();
        if (data != null) {
            this.bins.removeAll();
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
            ImportPreferences.removeConnectionIMAPSetting((ConnectionIMAPSettings) this.selectMailboxCombo.getSelectedItem());
            this.selectMailboxCombo.removeItem(this.selectMailboxCombo.getSelectedItem());
        } catch (BackingStoreException e) {
            JOptionPane.showMessageDialog(getPanel(), Resources.getString("PerspectiveCreate.ErrorDeletePreferences"), Resources.getString("PerspectiveCreate.Error"), JOptionPane.ERROR_MESSAGE);
        }
        this.stateChanged(new ChangeEvent(this));  
    }

    /**
     * Removes empty lines in participants and bins
     */
    private void actionRemoveEmptyLines() {
        // Collect to remove
        List<EntryParticipant> participantsToRemove = new ArrayList<>();
        for (Component entry : this.participants.getComponents()) {
                // Remove participants if both fields empty
                if (((EntryParticipant) entry).isEmpty()) {
                    participantsToRemove.add((EntryParticipant) entry);
                }
        }
        // Actually remove
        for (EntryParticipant entry : participantsToRemove) {
            if (this.participants.getComponentCount() > 1) {
                removeParticipant(entry);
            }
        }
        
        // Collect to remove
        List<EntryBin> binsToRemove = new ArrayList<>();
        for (Component entry : this.bins.getComponents()) {
                // Remove bin if left field empty and right field empty or zero
                if (((EntryBin) entry).isEmpty()) {
                    binsToRemove.add((EntryBin) entry);
                }
        }
        
        // Actually remove
        for (EntryBin entry : binsToRemove) {
            if (this.bins.getComponentCount() > 1) {
                removeBin((EntryBin) entry);
            }
        }
    }

    /**
     * Save the project and proceed.
     */
    private void actionSave() {
       
        // Check whether at least three participants
        if (this.participants.getComponents().length < 3) {
            JOptionPane.showMessageDialog(getPanel(), Resources.getString("PerspectiveCreate.notEnoughParticipants"));
            return;
        }
        
        // Collect participants
        List<Participant> participants = new ArrayList<>();
        for (Component entry : this.participants.getComponents()) {
            Participant participant = new Participant(((EntryParticipant)entry).getLeftValue(),
                                                      ((EntryParticipant)entry).getRightValue());
            participants.add(participant);
        }
        
        // Collect bins
        List<Bin> bins = new ArrayList<>();
        for (Component entry : this.bins.getComponents()) {
            Bin bin = new Bin(((EntryBin)entry).getLeftValue());
            bin.initialize(participants.size());
            bin.shareValue(new BigInteger(((EntryBin)entry).getRightValue().trim()));
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
        getApp().actionCreateDone(this.title.getText(), participants.toArray(new Participant[participants.size()]), bins.toArray(new Bin[bins.size()]), (ConnectionIMAPSettings) selectMailboxCombo.getSelectedItem());
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
        int index = Arrays.asList(this.bins.getComponents()).indexOf(previous);
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
            this.bins.add(entry, index);
            index++;
        }
        
        // Update GUI
        this.bins.revalidate();
        this.bins.repaint();
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
        int index = Arrays.asList(this.participants.getComponents()).indexOf(previous);
        index = index == -1 ? 0 : index + 1;
        
        // Create and add entry
        EntryParticipant entry = new EntryParticipant("", "", enabled, enabled);
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
        this.participants.add(entry, index);
        this.participants.revalidate();
        this.participants.repaint();
        this.stateChanged(new ChangeEvent(this));
    }

    /**
     * Checks all values for validity
     * @return
     */
    private boolean areValuesValid() {
        
        // Check participants
        for (Component c : this.participants.getComponents()) {
            if (!((EntryParticipant) c).areValuesValid()) {
                return false;
            }
        }
        
        // Check bins
        for (Component c : this.bins.getComponents()) {
            if (!((EntryBin) c).areValuesValid()) { 
                return false; 
            }
        }
      
        // Check title
        if (!title.isValueValid()) {
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
            ArrayList<ConnectionIMAPSettings> configFromPreferences = ImportPreferences.getConnectionIMAPSettings();
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
        if (this.bins.getComponentCount() == 1) {
            JOptionPane.showMessageDialog(getPanel(), Resources.getString("PerspectiveCreate.errorTooFewEntries"));
            return;
        }
        
        // Remove and update
        this.bins.remove(entry);
        this.stateChanged(new ChangeEvent(this));
        this.bins.revalidate();
        this.bins.repaint();
    }
    
    /**
     * Removes a participant
     * @param entry
     */
    private void removeParticipant(EntryParticipant entry) {
        
        // Check whether it's the last entry
        if (this.participants.getComponentCount() == 1) {
            JOptionPane.showMessageDialog(getPanel(), Resources.getString("PerspectiveCreate.errorTooFewEntries"));
            return;
        }
        
        // Remove and update
        this.participants.remove(entry);
        this.stateChanged(new ChangeEvent(this));
        this.participants.revalidate();    
        this.participants.repaint();
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
        generalDataPanel.setLayout(new BoxLayout(generalDataPanel, BoxLayout.Y_AXIS));
        panel.add(generalDataPanel, BorderLayout.NORTH);
        generalDataPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED),
                                                         Resources.getString("PerspectiveCreate.General"),
                                                         TitledBorder.LEFT,
                                                         TitledBorder.DEFAULT_POSITION));
        
        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new BorderLayout());
        titlePanel.add(new JLabel(Resources.getString("PerspectiveCreate.studyTitle")), BorderLayout.WEST);
        this.title = new ComponentTextField(new ComponentTextFieldValidator() {
            @Override
            public boolean validate(String text) {
                return !text.trim().isEmpty();
            }
        });
        this.title.setChangeListener(this);
        titlePanel.add(this.title, BorderLayout.CENTER);
        
        // Panel for automatic e-mail config
        JPanel automaticEMailPanel = new JPanel();
        automaticEMailPanel.setLayout(new BoxLayout(automaticEMailPanel, BoxLayout.X_AXIS));
       
        // Check box to use mail box automatically
        selectMailboxCombo = new JComboBox<>(getEmailConfig());
        selectMailboxCombo.setRenderer(new CustomRenderer());
        selectMailboxCombo.addActionListener(new ActionListener() {            
            @Override
            public void actionPerformed(ActionEvent e) {
                stateChanged(new ChangeEvent(this));
            }
        });
        
        // Button to add e-mail config
        addEmailboxButton = new JButton(Resources.getString("PerspectiveCreate.OpenEMailConfigAdd"));
        addEmailboxButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
               actionAddEMailConf();
            }
        });
        
        // Button to edit e-mail config
        editEmailboxButton = new JButton(Resources.getString("PerspectiveCreate.OpenEMailConfigEdit"));
        editEmailboxButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                actionEditEMailConf();
            }
        });
        
        // Button to remove e-mail config
        removeEmailboxButton = new JButton(Resources.getString("PerspectiveCreate.OpenEMailConfigRemove"));
        removeEmailboxButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                actionRemoveEMailConf();
            }
        });
        
        // Add
        generalDataPanel.add(titlePanel);       
        automaticEMailPanel.add(new JLabel(Resources.getString("PerspectiveCreate.AutomatedMailbox")));
        automaticEMailPanel.add(selectMailboxCombo);
        automaticEMailPanel.add(addEmailboxButton);
        automaticEMailPanel.add(editEmailboxButton);
        automaticEMailPanel.add(removeEmailboxButton);
        generalDataPanel.add(automaticEMailPanel);
        
        // Central panel
        JPanel central = new JPanel();
        central.setLayout(new GridLayout(2, 1));
        panel.add(central, BorderLayout.CENTER);
        
        // Participants
        this.participants = new ScrollablePanel();
        this.participants.setLayout(new BoxLayout(this.participants, BoxLayout.Y_AXIS));     
        JScrollPane pane = new JScrollPane(participants, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        pane.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED),
                                                                           Resources.getString("PerspectiveCreate.participants"),
                                                                           TitledBorder.LEFT,
                                                                           TitledBorder.DEFAULT_POSITION));
        central.add(pane, BorderLayout.NORTH);

        // Bins
        this.bins = new ScrollablePanel();
        this.bins.setLayout(new BoxLayout(this.bins, BoxLayout.Y_AXIS));
        pane = new JScrollPane(bins, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
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
        save = new JButton(Resources.getString("PerspectiveCreate.save"));
        save.setEnabled(this.areValuesValid());
        save.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                actionSave();
            }
        });
        buttonsPane.add(save, 0, 2);
        panel.add(buttonsPane, BorderLayout.SOUTH);
    }

    /**
     * Initialize perspective based on model
     */
    @Override
    protected void initialize() {
        
        // Clear
        this.participants.removeAll();
        this.bins.removeAll();
        this.title.setText("");

        // Add initial
        this.addParticipant(null, true);
        this.addBin(null, "", "", true);
        
        // Update
        this.stateChanged(new ChangeEvent(this));
    }
}

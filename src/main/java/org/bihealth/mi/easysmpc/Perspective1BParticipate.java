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
import java.util.List;
import java.util.Map;
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
import org.bihealth.mi.easysmpc.components.ComponentTextField;
import org.bihealth.mi.easysmpc.components.DialogEmailConfig;
import org.bihealth.mi.easysmpc.components.EntryBin;
import org.bihealth.mi.easysmpc.components.EntryParticipant;
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
public class Perspective1BParticipate extends Perspective implements ChangeListener {
    
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
    private ScrollablePanel    panelParticipants;

    /** Panel for bins */
    private ScrollablePanel    panelBins;

    /** Text field containing title of study */
    private ComponentTextField fieldTitle;

    /** Save button */
    private JButton            buttonSave;

    /** Central panel */
    private JPanel             panelCentral;
    
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

    /**
     * Creates the perspective
     * @param app
     */
    protected Perspective1BParticipate(App app) {
        super(app, Resources.getString("PerspectiveParticipate.participate"), 1, false); //$NON-NLS-1$
    }    
    
    /**
     * Reacts on all changes in any components
     */
    @Override
    public void stateChanged(ChangeEvent e) {

        // Deactivate e-mail configuration if manual mode
        this.buttonAddMailbox.setEnabled(getApp().getModel().isAutomatedMode());
        this.buttonEditMailbox.setEnabled(getApp().getModel().isAutomatedMode());
        this.buttonRemoveMailbox.setEnabled(getApp().getModel().isAutomatedMode());
        this.comboSelectMailbox.setEnabled(getApp().getModel().isAutomatedMode());
        
        // Set save button enabled/disabled
        this.buttonSave.setEnabled(this.areValuesValid());
    }

    /**
     * Adds an e-mail configuration
     */
    private void actionAddEMailConf() {
        ConnectionIMAPSettings settings = new DialogEmailConfig(null, getApp()).showDialog();
        if(settings != null) {
            Connections.addOrUpdate(settings);
            this.comboSelectMailbox.addItem(settings);
            this.comboSelectMailbox.setSelectedItem(settings);
            this.emailconfigCheck = true;
        }
        this.stateChanged(new ChangeEvent(this));
    }

    /**
     * Edits an e-mail configuration
     */
    private void actionEditEMailConf() {
        ConnectionIMAPSettings settings = new DialogEmailConfig((ConnectionIMAPSettings) this.comboSelectMailbox.getSelectedItem(), getApp()).showDialog();
        if(settings != null) {
            Connections.addOrUpdate(settings);
            this.comboSelectMailbox.addItem(settings);
            this.comboSelectMailbox.setSelectedItem(settings);
        }
        this.stateChanged(new ChangeEvent(this));        
    }
    
    /**
     * Loads and sets bin names and data from a file
     */
    private void actionLoadFromFile() {
        Map<String, String> data = getApp().getDataFromFile();
        List<String> binsWithoutValue = new ArrayList<>();
        if (data != null) {
            for (Component c : this.panelBins.getComponents()) {
                String value = data.get(((EntryBin) c).getLeftValue());
                if (value == null) {
                    binsWithoutValue.add(((EntryBin) c).getLeftValue());
                }
                ((EntryBin) c).setRightValue(value);
            }
            
            // Error message if a value for at least one bin was not found
            if (binsWithoutValue.size() > 0){                
                JOptionPane.showMessageDialog(getPanel(),
                                              String.format(Resources.getString("PerspectiveParticipate.BinsWithoutValues"),
                                                            binsWithoutValue.toString().substring(1, binsWithoutValue.toString().length() - 1)),
                                              Resources.getString("App.11"),
                                              JOptionPane.ERROR_MESSAGE);
            }            
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
     * Save the project
     * 
     * @return Saving actually performed?
     */
    private void actionSave() {
        
        // If automated mode: Check e-mail address used as defined by study creator
        if (getApp().getModel().isAutomatedMode() &&
            (this.comboSelectMailbox.getSelectedItem() == null ||
             !((ConnectionIMAPSettings) comboSelectMailbox.getSelectedItem()).getEmailAddress()
                                                                             .equals(getApp().getModel().getParticipants()[getApp().getModel().getOwnId()].emailAddress))) {
            JOptionPane.showMessageDialog(getPanel(),
                                          Resources.getString("PerspectiveParticipate.wrongEMailaddress"));
            return;
        }
        
        // Check e-mail configuration if not done so far
        if (comboSelectMailbox.getSelectedItem() != null && !emailconfigCheck) {
            try {
                if (!((ConnectionIMAPSettings) comboSelectMailbox.getSelectedItem()).isValid()) {
                    throw new BusException("Connection error");
                }
            } catch (BusException e) {
                // Error message
                JOptionPane.showMessageDialog(getPanel(), Resources.getString("PerspectiveCreate.emailConnectionNotWorking"));
                return;
            }
        }
        
        // Collect and store secrets
        BigDecimal[] secret = new BigDecimal[getApp().getModel().getBins().length];
        for (int i = 0; i < this.panelBins.getComponents().length; i++) {
            secret[i] = new BigDecimal(((EntryBin) this.panelBins.getComponents()[i]).getRightValue().trim().replace(',', '.'));
        }
        
        // Proceed
        getApp().actionParticipateDone(secret, (ConnectionIMAPSettings) comboSelectMailbox.getSelectedItem());
    }

    /**
     * Checks bins for validity
     * @return
     */
    private boolean areValuesValid() {
        // Check bins
        for (Component c : this.panelBins.getComponents()) {
            if (!((EntryBin) c).isFieldRightValueValid()) { 
                return false; 
            }
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
        titlePanel.add(new JLabel(Resources.getString("PerspectiveCreate.studyTitle")),
                       BorderLayout.WEST);
        this.fieldTitle = new ComponentTextField(null);
        this.fieldTitle.setEnabled(false);
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
        panelCentral = new JPanel();
        panelCentral.setLayout(new GridLayout(2, 1));
        panel.add(panelCentral, BorderLayout.CENTER);        
        
        // Participants
        this.panelParticipants = new ScrollablePanel();
        this.panelParticipants.setLayout(new BoxLayout(this.panelParticipants, BoxLayout.Y_AXIS));
        JScrollPane pane = new JScrollPane(panelParticipants, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        pane.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED),
                                                        Resources.getString("PerspectiveParticipate.participants"),
                                                        TitledBorder.LEFT,
                                                        TitledBorder.DEFAULT_POSITION));
        panelCentral.add(pane, BorderLayout.NORTH);    
                        
        // Bins
        this.panelBins = new ScrollablePanel();
        this.panelBins.setLayout(new BoxLayout(this.panelBins, BoxLayout.Y_AXIS));
        pane = new JScrollPane(panelBins, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        pane.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED),
                                                        Resources.getString("PerspectiveParticipate.bins"),
                                                        TitledBorder.LEFT,
                                                        TitledBorder.DEFAULT_POSITION));
        panelCentral.add(pane, BorderLayout.SOUTH);

        // Buttons pane
        JPanel buttonsPane = new JPanel();
        buttonsPane.setLayout(new GridLayout(2, 1));
        
        // Load from file button      
        JButton loadFromFile = new JButton(Resources.getString("PerspectiveCreate.LoadFromFile"));
        loadFromFile.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                actionLoadFromFile();
            }
        });
        buttonsPane.add(loadFromFile, 0, 0);   

        // Save button
        buttonSave = new JButton(Resources.getString("PerspectiveParticipate.save"));
        buttonSave.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                actionSave();
            }
        });
        buttonsPane.add(buttonSave, 0, 1);
        panel.add(buttonsPane, BorderLayout.SOUTH);
    }

    /**
     * Initialize perspective based on model
     */
    @Override
    protected void initialize() {
        
        // Clear
        panelParticipants.removeAll();
        panelBins.removeAll();
        
        // Title
        this.fieldTitle.setText(getApp().getModel().getName());
        
        // Add participants
        int i = 0;
        for (Participant currentParticipant : getApp().getModel().getParticipants()) {
            EntryParticipant newNameEmailParticipantEntry = new EntryParticipant(currentParticipant.name, currentParticipant.emailAddress, false, false, i == getApp().getModel().getOwnId());
            panelParticipants.add(newNameEmailParticipantEntry);
            i++;
        }
        for (Bin currentBin : getApp().getModel().getBins()) {
            EntryBin newBin = new EntryBin(currentBin.name, false, "", true, false);
            newBin.setChangeListener(this);
            panelBins.add(newBin);
        }
        
        // If automated mode and e-mail config is already known chose the e-mail config
        if (getApp().getModel().isAutomatedMode()) {
            // Loop over all settings
            for (int index = 0; index < comboSelectMailbox.getItemCount(); index++) {
                if (comboSelectMailbox.getItemAt(index) != null &&
                    comboSelectMailbox.getItemAt(index)
                                      .getEmailAddress()
                                      .equals(getApp().getModel()
                                                      .getParticipantFromId(getApp().getModel()
                                                                                    .getOwnId()).emailAddress)) {
                    comboSelectMailbox.setSelectedIndex(index);
                    break;
                }
            }
        }
        
        // Update GUI
        this.stateChanged(new ChangeEvent(this));
        getPanel().revalidate();
        getPanel().repaint();        
    }
}
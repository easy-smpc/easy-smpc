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
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.prefs.BackingStoreException;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.bihealth.mi.easybus.BusException;
import org.bihealth.mi.easybus.ConnectionSettings;
import org.bihealth.mi.easybus.ConnectionSettings.ConnectionTypes;
import org.bihealth.mi.easybus.implementations.email.ConnectionSettingsIMAP;
import org.bihealth.mi.easybus.implementations.http.easybackend.ConnectionSettingsEasybackend;
import org.bihealth.mi.easysmpc.Perspective1ACreate.ConnectionSettingsRenderer;
import org.bihealth.mi.easysmpc.components.ComponentTextField;
import org.bihealth.mi.easysmpc.components.DialogEasybackendConfig;
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
    
    /** Panel for participants */
    private ScrollablePanel               panelParticipants;

    /** Panel for bins */
    private ScrollablePanel               panelBins;

    /** Text field containing title of study */
    private ComponentTextField            fieldTitle;

    /** Save button */
    private JButton                       buttonSave;

    /** Central panel */
    private JPanel                        panelCentral;

    /** Add exchange configuration */
    private JButton                       buttonAddExchangeConfig;

    /** Combo box to select exchange mode */
    private JComboBox<ConnectionTypes>    comboExchangeMode;

    /** Combo box to select mail box configuration */
    private JComboBox<ConnectionSettings> comboExchangeConfig;

    /** Add exchange configuration */
    private JButton                       buttonRemoveExchangeConfig;

    /** Edit exchange configuration */
    private JButton                       buttonEditExchangeConfig;

    /** Has exchange config been checked? */
    private boolean                       exchangeConfigCheck;

    /**
     * Creates the perspective
     * 
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
        
        // Change in combo box exchange
        if (e.getSource() == comboExchangeMode) {
            // Reset combo box
            ConnectionSettings currentSetting = (ConnectionSettings) comboExchangeConfig.getSelectedItem();
            comboExchangeConfig.removeAllItems();
            for (ConnectionSettings settings : getExchangeConfig()) {
                comboExchangeConfig.addItem(settings);

                // Set selected
                if (currentSetting != null && settings != null &&
                        settings.getIdentifier().equals(currentSetting.getIdentifier())) {
                    settings.setPasswordStore(currentSetting.getPasswordStore());
                    comboExchangeConfig.setSelectedItem(settings);
                }
            }
        }
        
        if (getApp().getModel().isAutomatedMode()) {
            // Activate exchange configuration if necessary
            this.comboExchangeMode.setSelectedItem(getApp().getModel().getExchangeMode());
            this.buttonAddExchangeConfig.setEnabled(true);
            this.buttonEditExchangeConfig.setEnabled(true);
            this.buttonRemoveExchangeConfig.setEnabled(true);
            this.comboExchangeConfig.setEnabled(true);
        } else {
            // Deactivate exchange configuration if manual mode
            this.buttonAddExchangeConfig.setEnabled(false);
            this.buttonEditExchangeConfig.setEnabled(false);
            this.buttonRemoveExchangeConfig.setEnabled(false);
            this.comboExchangeConfig.setEnabled(false);
        }
        // Set save button enabled/disabled
        this.buttonSave.setEnabled(this.areValuesValid());
    }

    /**
     * Adds an exchange configuration
     */
    private void actionAddExchangeConf() {
        // Get new settings
        ConnectionSettings newSettings;
        switch ((ConnectionTypes) this.comboExchangeMode.getSelectedItem()) {
        case EASYBACKEND:
            newSettings = new DialogEasybackendConfig(null, getApp()).showDialog();
            break;
        case EMAIL:
            newSettings = new DialogEmailConfig(null, getApp()).showDialog();
            break;
        case MANUAL:
            return;
        default:
            return;
        }

        if (newSettings != null) {
            // Update connections in preferences
            try {
                Connections.addOrUpdate(newSettings);
            } catch (IOException e) {
                JOptionPane.showMessageDialog(getPanel(), Resources.getString("PerspectiveCreate.ErrorStorePreferences"), Resources.getString("PerspectiveCreate.Error"), JOptionPane.ERROR_MESSAGE);
            }

            // Reset combo box
            comboExchangeConfig.removeAllItems();
            for(ConnectionSettings settings: getExchangeConfig()) {
                this.comboExchangeConfig.addItem(settings);

                // Set selected
                if(settings != null && settings.getIdentifier().equals(newSettings.getIdentifier())) {
                    settings.setPasswordStore(newSettings.getPasswordStore());
                    this.comboExchangeConfig.setSelectedItem(settings);
                }
            }

            // Set checked
            this.exchangeConfigCheck = true;
        }
        this.stateChanged(new ChangeEvent(this));
    }

    /**
     * Edits an exchange configuration
     */
    private void actionEditExchangeConf() {
        
        // Get new settings
        ConnectionSettings newSettings = getApp().editExchangeConf((ConnectionSettings) this.comboExchangeConfig.getSelectedItem());

        
        // Alter combo box if new settings given
        if (newSettings != null) {
            // Update connections in preferences
            try {
                Connections.addOrUpdate(newSettings);
            } catch (IOException e) {
                JOptionPane.showMessageDialog(getPanel(), Resources.getString("PerspectiveCreate.ErrorStorePreferences"), Resources.getString("PerspectiveCreate.Error"), JOptionPane.ERROR_MESSAGE);
            }
            
            // Reset combo  box
            comboExchangeConfig.removeAllItems();
            for(ConnectionSettings settings: getExchangeConfig()) {
                this.comboExchangeConfig.addItem(settings);
                
                // Set selected
                if(settings != null && settings.getIdentifier().equals(newSettings.getIdentifier())) {
                    settings.setPasswordStore(newSettings.getPasswordStore());
                    this.comboExchangeConfig.setSelectedItem(settings);
                }
            }
            
            // Set checked
            this.exchangeConfigCheck = true;
        }
        
        // Change state
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
     * Removes an exchange configuration
     */
    private void actionRemoveExchangeConf() {
        try {
            Connections.remove((ConnectionSettings) this.comboExchangeConfig.getSelectedItem());
        } catch (BackingStoreException e) {
            JOptionPane.showMessageDialog(getPanel(), Resources.getString("PerspectiveCreate.ErrorDeletePreferences"), Resources.getString("PerspectiveCreate.Error"), JOptionPane.ERROR_MESSAGE);
        }
        
        // Reset combo box
        comboExchangeConfig.removeAllItems();
        for (ConnectionSettings settings : getExchangeConfig()) {
            this.comboExchangeConfig.addItem(settings);
        }

        // State changed
        this.stateChanged(new ChangeEvent(this));
    }

    /**
     * Save the project
     * 
     * @return Saving actually performed?
     */
    private void actionSave() {
        
        // If automated mode: Check identifier used as defined by study creator
        if (getApp().getModel().isAutomatedMode() &&
            (this.comboExchangeConfig.getSelectedItem() == null ||
             !((ConnectionSettings) comboExchangeConfig.getSelectedItem()).getIdentifier()
             .equals(getApp().getModel().getParticipants()[getApp().getModel().getOwnId()].emailAddress))) {
            // Show error message and return
            JOptionPane.showMessageDialog(getPanel(), Resources.getString("PerspectiveParticipate.wrongIdentifier"));
            return;
        }
        
        // Check exchange configuration if not done so far
        if (comboExchangeConfig.getSelectedItem() != null && !exchangeConfigCheck) {
            try {
                if (!((ConnectionSettings) comboExchangeConfig.getSelectedItem()).isValid(true)) {
                    throw new BusException("Connection error");
                }
            } catch (BusException e) {
                // Error message
                JOptionPane.showMessageDialog(getPanel(), Resources.getString("PerspectiveParticipate.exchangeConnectionNotWorking"));
                return;
            }
        }
        
        // Collect and store secrets
        BigDecimal[] secret = new BigDecimal[getApp().getModel().getBins().length];
        for (int i = 0; i < this.panelBins.getComponents().length; i++) {
            secret[i] = new BigDecimal(((EntryBin) this.panelBins.getComponents()[i]).getRightValue().trim().replace(',', '.'));
        }
        
        // Proceed
        getApp().actionParticipateDone(secret, (ConnectionSettings) comboExchangeConfig.getSelectedItem());
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
    private ConnectionSettings[] getExchangeConfig() {
        
        try {
            // Read from preferences
            ArrayList<ConnectionSettings> configFromPreferences;

            switch ((ConnectionTypes) this.comboExchangeMode.getSelectedItem()) {
            case EASYBACKEND:
                configFromPreferences = Connections.list(ConnectionSettingsEasybackend.class);
                break;
            case EMAIL:
                configFromPreferences = Connections.list(ConnectionSettingsIMAP.class);
                break;
            case MANUAL:
                configFromPreferences = new ArrayList<>();
            default:
                configFromPreferences = new ArrayList<>();
            }

            // Add null for non-automatic
            return configFromPreferences.toArray(new ConnectionSettings[configFromPreferences.size()]);
        } catch (BackingStoreException | ClassNotFoundException | IOException e) {
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
        
        // Panel for exchange config
        JPanel automaticExchangePanel = new JPanel();
        automaticExchangePanel.setLayout(new BoxLayout(automaticExchangePanel, BoxLayout.X_AXIS));
       
        // Combo boxes for exchange mode & config
        comboExchangeMode = new JComboBox<>(ConnectionTypes.values());
        comboExchangeMode.setSelectedItem(ConnectionTypes.MANUAL);
        comboExchangeMode.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                stateChanged(new ChangeEvent(comboExchangeMode));
            }
        });
        
        comboExchangeConfig = new JComboBox<>();
        comboExchangeConfig.setRenderer(new ConnectionSettingsRenderer());
        comboExchangeConfig.addActionListener(new ActionListener() {            
            @Override
            public void actionPerformed(ActionEvent e) {
                stateChanged(new ChangeEvent(this));
            }
        });
        
        // Button to add e-mail config
        buttonAddExchangeConfig = new JButton(Resources.getString("PerspectiveCreate.OpenEMailConfigAdd"));
        buttonAddExchangeConfig.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
               actionAddExchangeConf();
            }
        });
        
        // Button to edit e-mail config
        buttonEditExchangeConfig = new JButton(Resources.getString("PerspectiveCreate.OpenEMailConfigEdit"));
        buttonEditExchangeConfig.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                actionEditExchangeConf();
            }
        });
        
        // Button to remove e-mail config
        buttonRemoveExchangeConfig = new JButton(Resources.getString("PerspectiveCreate.OpenEMailConfigRemove"));
        buttonRemoveExchangeConfig.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                actionRemoveExchangeConf();
            }
        });
        
        // Add
        generalDataPanel.add(titlePanel);       
        automaticExchangePanel.add(new JLabel(Resources.getString("PerspectiveCreate.AutomatedMailbox")));
        automaticExchangePanel.add(comboExchangeMode);
        automaticExchangePanel.add(comboExchangeConfig);
        automaticExchangePanel.add(buttonAddExchangeConfig);
        automaticExchangePanel.add(buttonEditExchangeConfig);
        automaticExchangePanel.add(buttonRemoveExchangeConfig);
        generalDataPanel.add(automaticExchangePanel);        
        
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
        
        // If automated mode and the correct exchange config is already known select it
        if (getApp().getModel().isAutomatedMode()) {
            // Loop over all settings
            for (int index = 0; index < comboExchangeConfig.getItemCount(); index++) {
                if (comboExchangeConfig.getItemAt(index) != null &&
                    comboExchangeConfig.getItemAt(index)
                                      .getIdentifier()
                                      .equals(getApp().getModel()
                                                      .getParticipantFromId(getApp().getModel()
                                                                                    .getOwnId()).emailAddress)) {
                    comboExchangeConfig.setSelectedIndex(index);
                    break;
                }
            }
        }
        
        // Set default
        this.comboExchangeMode.setEnabled(false);
        
        // Update GUI
        this.stateChanged(new ChangeEvent(this));
        getPanel().revalidate();
        getPanel().repaint();        
    }
}
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
package org.bihealth.mi.easysmpc.components;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.prefs.BackingStoreException;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.ListSelectionModel;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.bihealth.mi.easybus.ConnectionSettings;
import org.bihealth.mi.easybus.implementations.http.easybackend.ConnectionSettingsEasyBackend;
import org.bihealth.mi.easysmpc.resources.Connections;
import org.bihealth.mi.easysmpc.resources.Resources;

/**
 * Entry for Easybackend config
 * 
 * @author Felix Wirth
 *
 */
public class EntryConnectionConfigEasyBackend extends ComponentConnectionConfig implements ChangeListener {

    /** SVUID */
    private static final long                          serialVersionUID = -1273453640931118450L;
    /** Entry for basic details */
    private EntryEasyBackendBasic                      entryEasybackendBasic;
    /** Entry for advanced details */
    private EntryEasyBackendAdvanced                   entryEasybackendAdvanced;
    /** Edit or create mode? */
    private boolean                                    createMode       = true;
    /** Tabbed pane */
    JTabbedPane                                        tabbedPane       = new JTabbedPane();
    /** Entry for message size */
    private ComponentEntryOne                          entryMessageSize;
    /** Entry for check interval */
    private ComponentEntryOne                          entryCheckInterval;
    /** Entry for e-mail sending timeout */
    private ComponentEntryOne                          entrySendTimeout;
    /** Radio button group dialog type simple/advanced */
    private ComponentRadioEntry                        radioDialogType;
    /** Parent */
    private JDialog                                    parent;
    /** Listener */
    private ChangeListener                             listener;
    /** Config list */
    private final JList<ConnectionSettingsEasyBackend> configList;
    /** List data model */
    DefaultListModel<ConnectionSettingsEasyBackend>    configListModel  = new DefaultListModel<>();
    /** Central panel */
    private JPanel                                     centralBase;
    
    /**
     * Creates a new instance
     * @param parent 
     */
    public EntryConnectionConfigEasyBackend(JDialog parent){
        
        // Store
        this.parent = parent;
        
        // Set layout
        this.setLayout(new BorderLayout());
        
        // Create list
        configList = new JList<>(configListModel);
        configList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        configList.addListSelectionListener(new ListSelectionListener() {
            
            @Override
            public void valueChanged(ListSelectionEvent e) {
                stateChanged(new ChangeEvent(e.getSource()));
            }
        });
        configList.setCellRenderer(new DefaultListCellRenderer() {
            
            /** SVUID */
            private static final long serialVersionUID = 779154691407559989L;
            
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
                    label.setText(((ConnectionSettings) value).getIdentifier());
                }
                return label;
            }
        });
        
        // Base settings panes
        centralBase = new JPanel();
        JPanel top = new JPanel();
        JPanel mainBase = new JPanel();
        mainBase.setLayout(new BorderLayout());

        // Create central panel and simple/advanced radio 
        centralBase.setLayout(new BoxLayout(centralBase, BoxLayout.Y_AXIS));


        // Create switch between simple and advanced dialog        
        this.radioDialogType = new ComponentRadioEntry(null,
                                                       Resources.getString("EmailConfig.26"),
                                                       Resources.getString("EmailConfig.27"),
                                                       false);
        this.radioDialogType.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {

                // If swap from simple to advanced
                if (!radioDialogType.isFirstOptionSelected()) {
                    // Display advanced
                    displayEasyBackendAdvancedSettings(entryEasybackendBasic.getSettings());
                    return;
                }

                // If swap from advanced to simple and no dialog necessary
                if (!isAdvancedDialogNecessary(entryEasybackendAdvanced.getSettings()) && radioDialogType.isFirstOptionSelected()) {
                    // Display basic
                    displayEasyBackendBasicSettings(entryEasybackendAdvanced.getSettings());
                    return;
                }

                // If swap from advanced to simple ask before
                if (radioDialogType.isFirstOptionSelected() &&
                        isAdvancedDialogNecessary(entryEasybackendAdvanced.getSettings()) &&
                        JOptionPane.showConfirmDialog(parent,
                                                      Resources.getString("EmailConfig.41"),
                                                      Resources.getString("EmailConfig.42"),
                                                      JOptionPane.OK_CANCEL_OPTION) == JOptionPane.YES_OPTION) {
                    // Display basic dialog
                    displayEasyBackendBasicSettings(entryEasybackendAdvanced.getSettings());
                    return;
                } else {
                    // Reset
                    radioDialogType.setFirstOptionSelected(false);
                }

            }
        });

        // Add base settings pane
        top.add(radioDialogType);
        mainBase.add(top, BorderLayout.NORTH);
        mainBase.add(centralBase, BorderLayout.CENTER);

        // Create further settings pane
        JPanel mainFurther = new JPanel();
        JPanel centralFurther = new JPanel();
        centralFurther.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED),
                                                                  Resources.getString("EmailConfig.44"),
                                                                  TitledBorder.LEFT,
                                                                  TitledBorder.DEFAULT_POSITION));
        entryMessageSize = new ComponentEntryOne(Resources.getString("EmailConfig.45"),
                                                 null,
                                                 true,
                                                 new ComponentTextFieldValidator() {

            @Override
            public boolean validate(String text) {
                try {
                    Integer.parseInt(text);
                    return true;
                } catch (Exception e) {
                    return false;
                }
            }
        },
                                                 false,
                                                 false);
        entryCheckInterval = new ComponentEntryOne(Resources.getString("EmailConfig.46"),
                                                   null,
                                                   true,
                                                   new ComponentTextFieldValidator() {

            @Override
            public boolean validate(String text) {
                try {
                    Integer.parseInt(text);
                    return true;
                } catch (Exception e) {
                    return false;
                }
            }
        },
                                                   false,
                                                   false);
        entrySendTimeout = new ComponentEntryOne(Resources.getString("EmailConfig.47"),
                                                 null,
                                                 true,
                                                 new ComponentTextFieldValidator() {

            @Override
            public boolean validate(String text) {
                try {
                    Integer.parseInt(text);
                    return true;
                } catch (Exception e) {
                    return false;
                }
            }
        },
                                                 false,
                                                 false);

        // Set default values for further settings
        entryMessageSize.setValue(String.valueOf(Resources.EMAIL_MAX_MESSAGE_SIZE_DEFAULT/ (1024 * 1024)));
        entryCheckInterval.setValue(String.valueOf(Resources.INTERVAL_CHECK_EASYBACKEND_DEFAULT / 1000));
        entrySendTimeout.setValue(String.valueOf(Resources.TIMEOUT_EASYBACKEND / 1000));

        // Add further settings pane
        centralFurther.setLayout(new BoxLayout(centralFurther, BoxLayout.Y_AXIS));
        mainFurther.setLayout(new BorderLayout());
        centralFurther.add(entryMessageSize);
        centralFurther.add(entryCheckInterval);
        centralFurther.add(entrySendTimeout);
        mainFurther.add(centralFurther, BorderLayout.CENTER);
        
        // Add tabbed panes
        tabbedPane.add(Resources.getString("EmailConfig.43"), mainBase);
        tabbedPane.add(Resources.getString("EmailConfig.44"), mainFurther);
        
        // Add
        this.add(configList, BorderLayout.WEST);
        this.add(tabbedPane, BorderLayout.CENTER);
        
        // Display empty simple dialog
        displayEasyBackendSettings(null);
        
        // State changed and build list
        updateList();
        stateChanged(new ChangeEvent(configList));
    }
    
    /**
     * Displays the settings after deciding for a complex or basic dialog
     * @param settings
     */
    private void displayEasyBackendSettings(ConnectionSettingsEasyBackend settings) {
        // Fill fields
        if (settings != null) {

            // Editing, not creating
            createMode = false;

            // Display simple or advanced dialog
            if (!isAdvancedDialogNecessary(settings)) {
                this.radioDialogType.setFirstOptionSelected(true);
                displayEasyBackendBasicSettings(settings);
            } else {
                this.radioDialogType.setFirstOptionSelected(false);
                displayEasyBackendAdvancedSettings(settings);
            }

            // Set fields for further options
            entryMessageSize.setValue(String.valueOf(settings.getMaxMessageSize() / (1024 * 1024)));
            entryCheckInterval.setValue(String.valueOf(settings.getCheckInterval() / 1000));
            entrySendTimeout.setValue(String.valueOf(settings.getSendTimeout() / 1000));
        } else {
            this.createMode = true;
            displayEasyBackendBasicSettings(null);
        }
    }

    /**
     * Display basic dialog
     * @param settings
     */
    private void displayEasyBackendBasicSettings(ConnectionSettingsEasyBackend settings) {
        // Remove
        centralBase.removeAll();
        entryEasybackendAdvanced = null;
    
        // Create and add entry
        entryEasybackendBasic= new EntryEasyBackendBasic(settings, createMode);
        centralBase.add(entryEasybackendBasic);
    
        // Add listeners
        entryEasybackendBasic.setChangeListener(this);
    
        // Repaint
        this.revalidate();
        this.repaint();
        parent.pack();
    }

    /**
     * Display advanced dialog
     * @param settings
     */
    private void displayEasyBackendAdvancedSettings(ConnectionSettingsEasyBackend settings) {
        // Remove
        centralBase.removeAll();
        entryEasybackendBasic = null;
    
        // Create and add entry
        entryEasybackendAdvanced = new EntryEasyBackendAdvanced(settings, createMode);
        centralBase.add(entryEasybackendAdvanced);
    
        // Add listeners
        entryEasybackendAdvanced.setChangeListener(this);
    
        // Repaint
        this.revalidate();
        this.repaint();
        parent.pack();
    }

    /**
     * Is the advanced dialog for a settings object necessary?
     * @param settings
     * @return
     */
    public static boolean isAdvancedDialogNecessary(ConnectionSettingsEasyBackend settings) {
        // Check
        if(settings == null) return false;
        
        // Check if values deviate from defaults
        return !settings.getClientId().equals(Resources.AUTH_CLIENTID_DEFAULT) ||
               !settings.getRealm().equals(Resources.AUTH_REALM_DEFAULT) ||
               settings.getProxy() != null || settings.getClientSecret() != null ||
               (settings.getAuthServer() != null &&
                !settings.getAuthServer().equals(settings.getAPIServer())) ? true : false;
    }

    @Override
    public boolean isProceedPossible() {
        return areValuesValid();
    }

    @Override
    public boolean isAddPossible() {
        return true;
    }

    @Override
    public boolean isRemovePossible() {
        return configList.getSelectedValue() != null;
    }

    @Override
    public void actionAdd() {
        displayEasyBackendSettings(null);
        configList.setSelectedValue(null, true);
    }

    @Override
    public void actionRemove() {
        try {
            Connections.remove(configList.getSelectedValue() );
        } catch (BackingStoreException e) {
            JOptionPane.showMessageDialog(this, Resources.getString("PerspectiveCreate.ErrorDeletePreferences"), Resources.getString("PerspectiveCreate.Error"), JOptionPane.ERROR_MESSAGE);
        }
        updateList();
        displayEasyBackendSettings(null);
    }

    @Override
    public ConnectionSettings getConnectionSettings() {
        // Collect from active component
        ConnectionSettingsEasyBackend result = entryEasybackendBasic != null
                ? entryEasybackendBasic.getSettings()
                : entryEasybackendAdvanced.getSettings();
        
        // Read additional options
        result.setMaxMessageSize(Integer.valueOf(entryMessageSize.getValue()) * (1024 * 1024));
        result.setCheckInterval(Integer.valueOf(entryCheckInterval.getValue()) * 1000);
        result.setSendTimeout(Integer.valueOf(entrySendTimeout.getValue()) * 1000);

        // Return
        return result;
    }

    @Override
    public void setChangeListener(ChangeListener listener) {
        this.listener = listener;
    }

    /**
     * Reacts to changes
     */
    @Override
    public void stateChanged(ChangeEvent e) {
        // Display currently selected value
        if (e.getSource() == configList) {
            displayEasyBackendSettings(configList.getSelectedValue());
        }
        
        // Call listener
        if (this.listener != null) {
            this.listener.stateChanged(e);
        }
    }

    /**
     * Checks for validity
     * 
     * @return
     */
    private boolean areValuesValid() {
        return entryEasybackendBasic != null ? entryEasybackendBasic.areValuesValid() : true
                && entryEasybackendAdvanced != null ? entryEasybackendAdvanced.areValuesValid() : true;
    }

    /**
     * Update list to select config
     */
    private void updateList() {
        ConnectionSettings currentSetting = configList.getSelectedValue();
        configListModel.removeAllElements();
    
        try {
            for (ConnectionSettings settings : Connections.list(ConnectionSettingsEasyBackend.class)) {
                configListModel.addElement((ConnectionSettingsEasyBackend) settings);
    
                // Set selected
                if (currentSetting != null && settings != null &&
                    settings.getIdentifier().equals(currentSetting.getIdentifier())) {
                    settings.setPasswordStore(currentSetting.getPasswordStore());
                    configList.setSelectedValue(settings, true);
                }
            }
        } catch (ClassNotFoundException | BackingStoreException | IOException e1) {
            JOptionPane.showMessageDialog(this,
                                          Resources.getString("PerspectiveCreate.ErrorLoadingPreferences"),
                                          Resources.getString("PerspectiveCreate.Error"),
                                          JOptionPane.ERROR_MESSAGE);
        }
    }
}

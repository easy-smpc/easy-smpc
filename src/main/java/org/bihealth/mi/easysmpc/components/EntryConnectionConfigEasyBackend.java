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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.bihealth.mi.easybus.ConnectionSettings;
import org.bihealth.mi.easybus.implementations.http.easybackend.ConnectionSettingsEasyBackend;
import org.bihealth.mi.easysmpc.resources.Resources;

/**
 * Entry for Easybackend config
 * 
 * @author Felix Wirth
 *
 */
public class EntryConnectionConfigEasyBackend extends ComponentConnectionConfig implements ChangeListener {

    /** SVUID */
    private static final long        serialVersionUID = -1273453640931118450L;
    /** Entry for basic details */
    private EntryEasyBackendBasic    entryEasybackendBasic;
    /** Entry for advanced details */
    private EntryEasyBackendAdvanced entryEasybackendAdvanced;
    /** Edit or create mode? */
    private boolean                  createMode       = true;
    /** Tabbed pane */
    JTabbedPane                      tabbedPane       = new JTabbedPane();
    /** Entry for message size */
    private ComponentEntryOne        entryMessageSize;
    /** Entry for check interval */
    private ComponentEntryOne        entryCheckInterval;
    /** Entry for e-mail sending timeout */
    private ComponentEntryOne        entrySendTimeout;
    /** Radio button group dialog type simple/advanced */
    private ComponentRadioEntry      radioDialogType;
    /** Central panel */
    private JPanel                   centralBase;

    /**
     * Creates a new instance
     * @param parent 
     * @param settings
     * @param changeConfigAllowed
     */
    public EntryConnectionConfigEasyBackend(JDialog parent, ConnectionSettingsEasyBackend settings, boolean changeConfigAllowed){
        super(parent, settings, changeConfigAllowed);
        
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
        tabbedPane.addChangeListener(this);
        
        // Add
        this.add(tabbedPane, BorderLayout.CENTER);
        
        // Display dialog
        updateList();
        displaySettings(settings);
        
        // State changed
        stateChanged(new ChangeEvent(this));
    }

    /**
     * Creates a new instance
     * @param parent
     */
    public EntryConnectionConfigEasyBackend(JDialog parent) {
        this(parent, null, true);
    }

    /**
     * Displays the settings after deciding for a complex or basic dialog
     * @param settings
     */
    @Override
    public void displaySettings(ConnectionSettings settingsGeneric) {
        // Check
        if(settingsGeneric != null && !(settingsGeneric instanceof ConnectionSettingsEasyBackend)) {
            throw new IllegalStateException("Settings must be of type ConnectionSettingsEasyBackend");
        }
        
        // Typecast
        ConnectionSettingsEasyBackend settings = (ConnectionSettingsEasyBackend) settingsGeneric;
        
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
        getParentDialog().pack();
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
        getParentDialog().pack();
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

    /**
     * Checks for validity
     * 
     * @return
     */
    private boolean areValuesValid() {
        return entryEasybackendBasic != null ? entryEasybackendBasic.areValuesValid() : true
                && entryEasybackendAdvanced != null ? entryEasybackendAdvanced.areValuesValid() : true;
    }

    @Override
    public Class<?> getSettingsClass() {
        return ConnectionSettingsEasyBackend.class;
    }
}

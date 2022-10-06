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
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.KeyStroke;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.bihealth.mi.easybus.BusException;
import org.bihealth.mi.easybus.implementations.http.easybackend.ConnectionSettingsEasyBackend;
import org.bihealth.mi.easysmpc.resources.Resources;

/**
 * Dialog for entering details of an EasyBackend connect
 * 
 * @author Felix Wirth
 */
public class DialogEasyBackendConfig extends JDialog implements ChangeListener {


    /** SVUID */
    private static final long             serialVersionUID = 8499684881939867625L;
    /** Button */
    private JButton                       buttonOK;
    /** Result */
    private ConnectionSettingsEasyBackend result;
    /** Parent frame */
    private JFrame                        parent;
    /** Central panel */
    private JPanel                        centralBase;
    /** Init finished */
    private boolean                       initFinished     = false;
    /** Entry for IMAP details */
    private EntryEasyBackendBasic         entryEasybackendBasic;
    /** Entry for SMTP details */
    private EntryEasyBackendAdvanced      entryEasybackendAdvanced;
    /** Edit or create mode? */
    private boolean                       createMode       = true;
    /** Tabbed pane */
    JTabbedPane                           tabbedPane       = new JTabbedPane();
    /** Entry for message size */
    private ComponentEntryOne             entryMessageSize;
    /** Entry for check interval */
    private ComponentEntryOne             entryCheckInterval;
    /** Entry for e-mail sending timeout */
    private ComponentEntryOne             entrySendTimeout;
    /** Radio button group dialog type simple/advanced */
    private ComponentRadioEntry           radioDialogType;

    /**
     * Create a new instance
     * 
     * @param settings to fill as default in the fields and deactivate the email field
     * @param parent Component to set the location of JDialog relative to
     */
    public DialogEasyBackendConfig(ConnectionSettingsEasyBackend settings, JFrame parent) {
        this(parent);

        // Fill fields
        if (settings != null) {

            // Editing, not creating
            createMode = false;

            // Display simple or advanced dialog
            if (!isAdvancedDialogNecessary(settings)) {
                this.radioDialogType.setFirstOptionSelected(true);
                displayBasicDialog(settings);
            } else {
                this.radioDialogType.setFirstOptionSelected(false);
                displayAdvancedDialog(settings);
            }

            // Set fields for further options
            entryMessageSize.setValue(String.valueOf(settings.getMaxMessageSize() / (1024 * 1024)));
            entryCheckInterval.setValue(String.valueOf(settings.getCheckInterval() / 1000));
            entrySendTimeout.setValue(String.valueOf(settings.getSendTimeout() / 1000));

            // Update state
            stateChanged(new ChangeEvent(this));
        }
    }

    /**
     * Is the advanced dialog for a settings object necessary?
     * 
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

    /**
     * Create a new instance
     * 
     * @param parent Component to set the location of JDialog relative to
     */
    public DialogEasyBackendConfig(JFrame parent) {

        // Dialog properties
        this.parent = parent;
        this.setTitle(Resources.getString("Easybackend.0"));
        this.getContentPane().setLayout(new BorderLayout());
        this.setIconImage(this.parent.getIconImage());
        this.setResizable(false);

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
                    displayAdvancedDialog(entryEasybackendBasic.getSettings());
                    return;
                }

                // If swap from advanced to simple and no dialog necessary
                if (!isAdvancedDialogNecessary(entryEasybackendAdvanced.getSettings()) && radioDialogType.isFirstOptionSelected()) {
                    // Display basic
                    displayBasicDialog(entryEasybackendAdvanced.getSettings());
                    return;
                }

                // If swap from advanced to simple ask before
                if (radioDialogType.isFirstOptionSelected() &&
                        isAdvancedDialogNecessary(entryEasybackendAdvanced.getSettings()) &&
                        JOptionPane.showConfirmDialog(DialogEasyBackendConfig.this,
                                                      Resources.getString("EmailConfig.41"),
                                                      Resources.getString("EmailConfig.42"),
                                                      JOptionPane.OK_CANCEL_OPTION) == JOptionPane.YES_OPTION) {
                    // Display basic dialog
                    displayBasicDialog(entryEasybackendAdvanced.getSettings());
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

        // Buttons
        JPanel okCancelPane = new JPanel();
        okCancelPane.setLayout(new GridLayout(1, 2));
        this.buttonOK = new JButton(Resources.getString("EmailConfig.6"));
        JButton buttonCancel = new JButton(Resources.getString("EmailConfig.7"));

        // Add
        okCancelPane.add(buttonCancel);
        okCancelPane.add(buttonOK);
        getContentPane().add(okCancelPane, BorderLayout.SOUTH);
        getContentPane().add(tabbedPane);

        this.buttonOK.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                actionCheckAndProceed();
            }
        });

        buttonCancel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                actionCancel();
            }
        });

        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent windowEvent) {
                DialogEasyBackendConfig.this.result = null;
            }
        });

        // Add shortcut key for escape
        JPanel dialogPanel = (JPanel) getContentPane();
        dialogPanel.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT)
        .put(KeyStroke.getKeyStroke("ESCAPE"), "cancel");
        dialogPanel.getActionMap().put("cancel", new AbstractAction() {
            /** SVUID */
            private static final long serialVersionUID = -5809172959090943313L;

            @Override
            public void actionPerformed(ActionEvent e) {
                actionCancel();
            }
        });

        // Add shortcut key for enter
        dialogPanel.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT)
        .put(KeyStroke.getKeyStroke("ENTER"), "proceed");
        dialogPanel.getActionMap().put("proceed", new AbstractAction() {

            /** SVUID */
            private static final long serialVersionUID = -4085624272147282716L;

            @Override
            public void actionPerformed(ActionEvent e) {
                buttonOK.doClick();
            }
        });

        // Display empty simple dialog
        displayBasicDialog(null);

        // Set init finished
        this.initFinished = true;
        this.stateChanged(new ChangeEvent(this));
    }

    /**
     * Display basic dialog
     * 
     * @param settings
     */
    private void displayBasicDialog(ConnectionSettingsEasyBackend settings) {
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
        this.pack();
    }

    /**
     * Display advanced dialog
     * 
     * @param settings
     */
    private void displayAdvancedDialog(ConnectionSettingsEasyBackend settings) {
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
        this.pack();
    }

    /**
     * Show this dialog
     */
    public ConnectionSettingsEasyBackend showDialog() {
        this.pack();
        this.setLocationRelativeTo(this.parent);
        this.setModal(true);
        this.setVisible(true);
        return this.result;
    }

    /**
     * Reacts to changes
     */
    @Override
    public void stateChanged(ChangeEvent e) {

        // Check init already done
        if (!initFinished) {
            return;
        }

        // Check button enabled
        if (this.buttonOK != null) {
            this.buttonOK.setEnabled(areValuesValid());
        }        
    }

    /**
     * Cancel action
     */
    private void actionCancel() {
        this.result = null;
        this.dispose();
    }

    /**
     * Action close
     */
    private void actionCheckAndProceed() {

        try {
            ConnectionSettingsEasyBackend settings = getConnectionSettings();
            if (!settings.isValid(false)) {
                throw new BusException("Connection error");
            }
            this.result = settings;
            this.dispose();
        } catch (BusException e) {
            JOptionPane.showMessageDialog(this, Resources.getString("Easybackend.3"), Resources.getString("EmailConfig.12"), JOptionPane.ERROR_MESSAGE);
            return;
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
     * Create a new connection settings object from data entries
     * 
     * @return connection settings
     */
    private ConnectionSettingsEasyBackend getConnectionSettings() {
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
}

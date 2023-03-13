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
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.prefs.BackingStoreException;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
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
import org.bihealth.mi.easybus.implementations.email.ConnectionSettingsIMAP;
import org.bihealth.mi.easybus.implementations.local.ConnectionSettingsManual;
import org.bihealth.mi.easysmpc.resources.Connections;
import org.bihealth.mi.easysmpc.resources.Resources;

/**
 * Entry for automated e-mail data
 * 
 * @author Felix Wirth
 *
 */
public class EntryConnectionConfigEmail extends ComponentConnectionConfig implements ChangeListener {
    
    /** Allows to set a custom text for each ConnectionSettings object in the list */
    public static class ConnectionSettingsRenderer extends DefaultListCellRenderer {
        
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
    }

    
    /** SVUID */
    private static final long                   serialVersionUID = -6353874563342643257L;
    /** Config list */
    private final JList<ConnectionSettingsIMAP> configList;
    /** List data model */
    DefaultListModel<ConnectionSettingsIMAP>    configListModel  = new DefaultListModel<>();
    /** Change listener */
    private ChangeListener                      listener;
    /** Radio button group dialog type simple/advanced */
    private ComponentRadioEntry                 radioDialogType;
    /** Central panel */
    private JPanel                              centralBase;
    /** Init finished */
    private boolean                             initFinished     = false;
    /** Entry for IMAP details */
    private EntryEMailDetails                   entryIMAPDetails;
    /** Entry for SMTP details */
    private EntryEMailDetails                   entrySMTPDetails;
    /** E-Mail and password entry */
    private EntryEMailPassword                  entryEmailPassword;
    /** Edit or create mode? */
    private boolean                             createMode       = true;
    /** Tabbed pane */
    JTabbedPane                                 tabbedPane       = new JTabbedPane();
    /** Entry for message size */
    private ComponentEntryOne                   entryMessageSize;
    /** Entry for check interval */
    private ComponentEntryOne                   entryCheckInterval;
    /** Entry for e-mail sending timeout */
    private ComponentEntryOne                   entrySendTimeout;


    /**
     * Creates a new instance
     * @param listner 
     */
    public EntryConnectionConfigEmail(ChangeListener listner){
        // Store
        this.listener = listner;
        
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
                
                // If swap from simple to advanced display simple dialog
                if (!radioDialogType.isFirstOptionSelected()) {
                        displayAdvancedDialog(entryEmailPassword.getLeftValue(),
                                              entryEmailPassword.getRightValue(),
                                              entryIMAPDetails,
                                              entrySMTPDetails);
                    return;
                }
                
                // If swap from advanced to simple and no dialog necessary
                if (!isAdvancedDialogNecessary(entrySMTPDetails, entryIMAPDetails) && radioDialogType.isFirstOptionSelected()) {
                    displaySimpleDialog(entryIMAPDetails.getEmailAddress(),
                                          entryIMAPDetails.getPassword(),
                                          entryIMAPDetails,
                                          entrySMTPDetails);
                    return;
                }
                
                // If swap from advanced to simple ask before
                if (radioDialogType.isFirstOptionSelected() &&
                    isAdvancedDialogNecessary(entrySMTPDetails, entryIMAPDetails) &&
                    JOptionPane.showConfirmDialog(EntryConnectionConfigEmail.this,
                                                  Resources.getString("EmailConfig.41"),
                                                  Resources.getString("EmailConfig.42"),
                                                  JOptionPane.OK_CANCEL_OPTION) == JOptionPane.YES_OPTION) {
                    // Display dialog
                    displaySimpleDialog(entryIMAPDetails.getEmailAddress(),
                                          entryIMAPDetails.getPassword(),
                                          entryIMAPDetails,
                                          entrySMTPDetails);
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
        entryCheckInterval.setValue(String.valueOf(Resources.INTERVAL_CHECK_MAILBOX_DEFAULT / 1000));
        entrySendTimeout.setValue(String.valueOf(Resources.TIMEOUT_SEND_EMAILS_DEFAULT / 1000));
        
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
        displaySimpleDialog(null, null, null, null);
        
        // State changed and build list
        updateList();
        stateChanged(new ChangeEvent(configList));
    }

    @Override
    public ConnectionSettings getConnectionSettings() {
        return new ConnectionSettingsManual();
    }

    @Override
    public boolean isProceedPossible() {
        return configList.getSelectedValue() != null;
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
        displaySimpleDialog(null, null, null, null);
    }

    @Override
    public void actionRemove() {
        try {
            Connections.remove(configList.getSelectedValue() );
        } catch (BackingStoreException e) {
            JOptionPane.showMessageDialog(this, Resources.getString("PerspectiveCreate.ErrorDeletePreferences"), Resources.getString("PerspectiveCreate.Error"), JOptionPane.ERROR_MESSAGE);
        }
        updateList();
        displaySimpleDialog(null, null, null, null);
    }

    @Override
    public void setChangeListener(ChangeListener listener) {
        this.listener = listener;
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        
        // Display currently selected value
        displayEMailSettings(configList.getSelectedValue());
        
        // Call listener
        if (this.listener != null) {
            this.listener.stateChanged(e);
        }
    }

    /**
     * Is the advanced dialog for a ConnectionIMAPSettings object necessary?
     * 
     * @param settings
     * @return
     */
    public static boolean isAdvancedDialogNecessary(ConnectionSettingsIMAP settings) {
        // Deviate the IMAP/SMTP addresses from each other or from the user name or is an auth mechanism set?
        return !settings.getIMAPEmailAddress().equals(settings.getSMTPEmailAddress()) ||
               settings.getIMAPUserName() != null || settings.getSMTPUserName() != null ||
               settings.getIMAPAuthMechanisms() != null || settings.getSMTPAuthMechanisms() != null;
    }

    /**
     * Is the advanced dialog for the config necessary?
     * 
     * @param imapEntry
     * @param smtpEntry
     * @return
     */
     public static boolean isAdvancedDialogNecessary(EntryEMailDetails imapEntry, EntryEMailDetails smtpEntry) {        
         
        // Deviate the IMAP/SMTP addresses from each other or from the user name or is an auth mechanism set?
        return imapEntry != null && smtpEntry != null &&
               (!imapEntry.getEmailAddress().equals(smtpEntry.getEmailAddress()) ||
                imapEntry.getUserName() != null || smtpEntry.getUserName() != null ||
                imapEntry.getAuthMechanisms() != null || smtpEntry.getAuthMechanisms() != null);
    }
     
     /**
     * Display a dialog
     */
    private void displayEMailSettings(ConnectionSettingsIMAP settings) {
        if (settings != null) {
            // Editing, not creating
            this.createMode = false;

            // Display simple or advanced dialog
            if (!isAdvancedDialogNecessary(settings)) {
                this.radioDialogType.setFirstOptionSelected(true);
                displaySimpleDialog(settings.getIMAPEmailAddress(),
                                    settings.getIMAPPassword(false),
                                    new EntryEMailDetails(null, 0, settings, true),
                                    new EntryEMailDetails(null, 0, settings, false));
            } else {
                this.radioDialogType.setFirstOptionSelected(false);
                displayAdvancedDialog(null,
                                      null,
                                      new EntryEMailDetailsAdvanced(null,
                                                                    0,
                                                                    settings,
                                                                    true,
                                                                    this.createMode),
                                      new EntryEMailDetailsAdvanced(null,
                                                                    0,
                                                                    settings,
                                                                    false,
                                                                    true));
            }

            // Set fields for further options
            entryMessageSize.setValue(String.valueOf(settings.getMaxMessageSize() / (1024 * 1024)));
            entryCheckInterval.setValue(String.valueOf(settings.getCheckInterval() / 1000));
            entrySendTimeout.setValue(String.valueOf(settings.getSendTimeout() / 1000));
        }
     }
     
    /**
     * Display simple dialog
     * 
     * @param emailAddress
     * @param password
     * @param oldDetailsIMAP
     * @param oldDetailsSMTP
     */
    private void displaySimpleDialog(String emailAddress, String password, EntryEMailDetails oldDetailsIMAP, EntryEMailDetails oldDetailsSMTP) {
        // Remove
        centralBase.removeAll();

        // Add e-mail password panel
        JPanel emailPasswordPanel = new JPanel();
        emailPasswordPanel.setLayout(new BoxLayout(emailPasswordPanel, BoxLayout.X_AXIS));
        emailPasswordPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED),
                                                                      Resources.getString("EmailConfig.36"),
                                                                      TitledBorder.LEFT,
                                                                      TitledBorder.DEFAULT_POSITION));
        this.entryEmailPassword = new EntryEMailPassword(Resources.getString("EmailConfig.1"), Resources.getString("EmailConfig.2"));
        emailPasswordPanel.add(entryEmailPassword);
        
        // Set e-mail and password data and enable or disable e-mail field
        this.entryEmailPassword.setLeftValue(emailAddress);
        this.entryEmailPassword.setRightValue(password);
        this.entryEmailPassword.setLefttEnabled(this.createMode);
        
        // Create receive and send panel
        JPanel receiveSendPanel = new JPanel();     
        receiveSendPanel.setLayout(new BoxLayout(receiveSendPanel, BoxLayout.X_AXIS));
        
        // Add IMAP panel
        entryIMAPDetails = new EntryEMailDetails(Resources.getString("EmailConfig.37"), ConnectionSettingsIMAP.DEFAULT_PORT_IMAP, oldDetailsIMAP);
        receiveSendPanel.add(entryIMAPDetails);                
        
        // Add box to separate
        receiveSendPanel.add(Box.createRigidArea(new Dimension(6, 0)));
        
        // Add SMTP panel
        entrySMTPDetails = new EntryEMailDetails(Resources.getString("EmailConfig.38"), ConnectionSettingsIMAP.DEFAULT_PORT_SMTP, oldDetailsSMTP);
        receiveSendPanel.add(entrySMTPDetails);        
        
        // Add to central
        centralBase.add(emailPasswordPanel);
        centralBase.add(receiveSendPanel);
        
        // Add listeners
        this.entryEmailPassword.setChangeListener(this);
        entryIMAPDetails.setChangeListener(this);
        entrySMTPDetails.setChangeListener(this);
        
        // Repaint
        this.revalidate();
        this.repaint();
    }

    /**
     * Display advanced dialog
     * 
     * @param emailAddress
     * @param password
     * @param oldIMAPDetails
     * @param oldSMTPDetails
     */
    private void displayAdvancedDialog(String emailAddress, String password, EntryEMailDetails oldIMAPDetails, EntryEMailDetails oldSMTPDetails) {
        // Remove
        centralBase.removeAll();
        entryEmailPassword = null;
                
        // Create send and receive panel
        JPanel receiveSendPanel = new JPanel();     
        receiveSendPanel.setLayout(new BoxLayout(receiveSendPanel, BoxLayout.X_AXIS));
        
        // Add IMAP panel
        entryIMAPDetails = new EntryEMailDetailsAdvanced(Resources.getString("EmailConfig.37"), ConnectionSettingsIMAP.DEFAULT_PORT_IMAP, oldIMAPDetails, emailAddress, password, this.createMode);
        receiveSendPanel.add(entryIMAPDetails);
        
        // Add box to separate
        receiveSendPanel.add(Box.createRigidArea(new Dimension(6, 0)));
        
        // Add SMTP panel
        entrySMTPDetails = new EntryEMailDetailsAdvanced(Resources.getString("EmailConfig.38"), ConnectionSettingsIMAP.DEFAULT_PORT_SMTP, oldSMTPDetails, emailAddress, password, true);
        receiveSendPanel.add(entrySMTPDetails);
        
        // Add to central
        centralBase.add(receiveSendPanel);
        
        // Add listeners
        entryIMAPDetails.setChangeListener(this);
        entrySMTPDetails.setChangeListener(this);
        
        // Repaint
        this.revalidate();
        this.repaint();
    }
    
    /**
     * Update list to select config
     */
    private void updateList() {
        ConnectionSettings currentSetting = configList.getSelectedValue();
        configListModel.removeAllElements();

        try {
            for (ConnectionSettings settings : Connections.list(ConnectionSettingsIMAP.class)) {
                configListModel.addElement((ConnectionSettingsIMAP) settings);

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

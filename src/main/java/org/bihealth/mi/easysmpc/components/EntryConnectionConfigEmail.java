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
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.bihealth.mi.easybus.BusException;
import org.bihealth.mi.easybus.ConnectionSettings;
import org.bihealth.mi.easybus.PasswordStore;
import org.bihealth.mi.easybus.implementations.email.ConnectionSettingsIMAP;
import org.bihealth.mi.easysmpc.AppPasswordProvider;
import org.bihealth.mi.easysmpc.resources.Resources;

/**
 * Entry for automated e-mail data
 * 
 * @author Felix Wirth
 *
 */
public class EntryConnectionConfigEmail extends ComponentConnectionConfig implements ChangeListener {
    
    /** SVUID */
    private static final long         serialVersionUID = -6353874563342643257L;
    /** Radio button group dialog type simple/advanced */
    private final ComponentRadioEntry radioDialogType;
    /** Central panel */
    private final JPanel              centralBase;
    /** Entry for IMAP details */
    private EntryEMailDetails         entryIMAPDetails;
    /** Entry for SMTP details */
    private EntryEMailDetails         entrySMTPDetails;
    /** E-Mail and password entry */
    private EntryEMailPassword        entryEmailPassword;
    /** Edit or create mode? */
    private boolean                   createMode       = true;
    /** Tabbed pane */
    private final JTabbedPane         tabbedPane       = new JTabbedPane();
    /** Entry for message size */
    private final ComponentEntryOne   entryMessageSize;
    /** Entry for check interval */
    private final ComponentEntryOne   entryCheckInterval;
    /** Entry for e-mail sending timeout */
    private final ComponentEntryOne   entrySendTimeout;

    /**
     * Creates a new instance
     * @param parent
     * @param settings
     * @param changeConfigAllowed
     */
    public EntryConnectionConfigEmail(JDialog parent, ConnectionSettingsIMAP settings, boolean changeConfigAllowed) {
        // Super
        super(parent, settings, changeConfigAllowed);
        
        // Base settings panes
        centralBase = new JPanel();
        JPanel top = new JPanel();
        JPanel mainBase = new JPanel();
        mainBase.setLayout(new BorderLayout());
        
        // Create central panel and simple/advanced radio 
        centralBase.setLayout(new BoxLayout(centralBase, BoxLayout.Y_AXIS));
        

        // Create switch between basic and advanced dialog
        this.radioDialogType = new ComponentRadioEntry(null,
                                                       Resources.getString("EmailConfig.26"),
                                                       Resources.getString("EmailConfig.27"),
                                                       false);
        this.radioDialogType.addActionListener(new ActionListener() {
            
            @Override
            public void actionPerformed(ActionEvent e) {
                
                // If swap from basic to advanced display simple dialog
                if (!radioDialogType.isFirstOptionSelected()) {
                        displayEMailSettingsAdvanced(entryEmailPassword.getLeftValue(),
                                              entryEmailPassword.getRightValue(),
                                              entryIMAPDetails,
                                              entrySMTPDetails);
                    return;
                }
                
                // If swap from advanced to basic and no dialog necessary
                if (!isAdvancedDialogNecessary(entrySMTPDetails, entryIMAPDetails) && radioDialogType.isFirstOptionSelected()) {
                    displayEMailSettingsSimple(entryIMAPDetails.getEmailAddress(),
                                          entryIMAPDetails.getPassword(),
                                          entryIMAPDetails,
                                          entrySMTPDetails);
                    return;
                }
                
                // If swap from advanced to basic ask before
                if (radioDialogType.isFirstOptionSelected() &&
                    isAdvancedDialogNecessary(entrySMTPDetails, entryIMAPDetails) &&
                    JOptionPane.showConfirmDialog(EntryConnectionConfigEmail.this,
                                                  Resources.getString("EmailConfig.41"),
                                                  Resources.getString("EmailConfig.42"),
                                                  JOptionPane.OK_CANCEL_OPTION) == JOptionPane.YES_OPTION) {
                    // Display dialog
                    displayEMailSettingsSimple(entryIMAPDetails.getEmailAddress(),
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
    public EntryConnectionConfigEmail(JDialog parent) {
        // Call constructor and store
        this(parent, null, true);
    }

    @Override
    public boolean isProceedPossible() {
        return areValuesValid();
    }

    @Override
    public boolean isAddPossible() {
        return true;
    }

    /**
     * Create a new connection settings object from data entries
     * 
     * @return connection settings
     * @throws BusException
     */
    public ConnectionSettingsIMAP getConnectionSettings() {
        
        // Init
        ConnectionSettingsIMAP result;
        
        // Take data from either entryEMail password or from IMAP and SMTP entries
        if(entryEmailPassword != null) {
            result = new ConnectionSettingsIMAP(entryEmailPassword.getLeftValue(), new AppPasswordProvider(Resources.getString("EmailConfig.29"), Resources.getString("EmailConfig.31")));
            result.setPasswordStore(new PasswordStore(entryEmailPassword.getRightValue(), entryEmailPassword.getRightValue()));
        }
        else {
            AppPasswordProvider provider = new AppPasswordProvider(Resources.getString("EmailConfig.29"), Resources.getString("EmailConfig.31"));
            result = new ConnectionSettingsIMAP(entryIMAPDetails.getEmailAddress(), entrySMTPDetails.getEmailAddress(), provider);
            result.setPasswordStore(new PasswordStore(entryIMAPDetails.getPassword(), entrySMTPDetails.getPassword()));
        };
        
        // Take data always coming from IMAP and SMTP entries and return
        return result.setIMAPPort(entryIMAPDetails.getPort())
                     .setSMTPPort(entrySMTPDetails.getPort())
                     .setIMAPServer(entryIMAPDetails.getServer())
                     .setSMTPServer(entrySMTPDetails.getServer())
                     .setSSLTLSIMAP(entryIMAPDetails.isSSLTLS())
                     .setSSLTLSSMTP(entrySMTPDetails.isSSLTLS())
                     .setIMAPUserName(entryIMAPDetails.getUserName())
                     .setSMTPUserName(entrySMTPDetails.getUserName())
                     .setIMAPAuthMechanisms(entryIMAPDetails.getAuthMechanisms())
                     .setSMTPAuthMechanisms(entrySMTPDetails.getAuthMechanisms())
                     .setMaxMessageSize(Integer.valueOf(entryMessageSize.getValue()) * 1024 * 1024)
                     .setCheckInterval(Integer.valueOf(entryCheckInterval.getValue()) * 1000)
                     .setEmailSendTimeout(Integer.valueOf(entrySendTimeout.getValue()) * 1000);
    }

    /**
     * Is the advanced dialog for a ConnectionIMAPSettings object necessary?
     * 
     * @param settings
     * @return
     */
    private static boolean isAdvancedDialogNecessary(ConnectionSettingsIMAP settings) {
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
     private static boolean isAdvancedDialogNecessary(EntryEMailDetails imapEntry, EntryEMailDetails smtpEntry) {        
         
        // Deviate the IMAP/SMTP addresses from each other or from the user name or is an auth mechanism set?
        return imapEntry != null && smtpEntry != null &&
               imapEntry.getEmailAddress() != null && smtpEntry.getEmailAddress() != null &&
               (!imapEntry.getEmailAddress().equals(smtpEntry.getEmailAddress()) ||
                imapEntry.getUserName() != null || smtpEntry.getUserName() != null ||
                imapEntry.getAuthMechanisms() != null || smtpEntry.getAuthMechanisms() != null);
    }
     
     /**
     * Displays the settings after deciding for a complex or simple dialog 
     */
    @Override
    public void displaySettings(ConnectionSettings settingsGeneric) {
        // Check
        if(settingsGeneric != null && !(settingsGeneric instanceof ConnectionSettingsIMAP)) {
            throw new IllegalStateException("Settings must be of type ConnectionSettingsIMAP");
        }
        
        // Typecast
        ConnectionSettingsIMAP settings = (ConnectionSettingsIMAP) settingsGeneric;
        
        // Display existing settings?
        if (settings != null) {
            this.createMode = false;

            // Display simple or advanced dialog
            if (!isAdvancedDialogNecessary(settings)) {
                this.radioDialogType.setFirstOptionSelected(true);
                displayEMailSettingsSimple(settings.getIMAPEmailAddress(),
                                    settings.getIMAPPassword(false),
                                    new EntryEMailDetails(null, 0, settings, true),
                                    new EntryEMailDetails(null, 0, settings, false));
            } else {
                this.radioDialogType.setFirstOptionSelected(false);
                displayEMailSettingsAdvanced(null,
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
        } else {
            this.createMode = true;
            displayEMailSettingsSimple(null, null, null, null);
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
    private void displayEMailSettingsSimple(String emailAddress, String password, EntryEMailDetails oldDetailsIMAP, EntryEMailDetails oldDetailsSMTP) {
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
        getParentDialog().pack();
    }

    /**
     * Display advanced dialog
     * 
     * @param emailAddress
     * @param password
     * @param oldIMAPDetails
     * @param oldSMTPDetails
     */
    private void displayEMailSettingsAdvanced(String emailAddress, String password, EntryEMailDetails oldIMAPDetails, EntryEMailDetails oldSMTPDetails) {
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
        getParentDialog().pack();
    }
    
    /**
     * Are values valid
     * 
     * @return
     */
    private boolean areValuesValid() {
        return  (entryEmailPassword != null ? entryEmailPassword.areValuesValid() : true)
                && entryIMAPDetails.areValuesValid() 
                && entrySMTPDetails.areValuesValid();
    }

    @Override
    public Class<?> getSettingsClass() {
        return ConnectionSettingsIMAP.class;
    }
}

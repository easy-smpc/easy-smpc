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
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.function.BooleanSupplier;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.bihealth.mi.easybus.BusException;
import org.bihealth.mi.easybus.Participant;
import org.bihealth.mi.easybus.implementations.email.ConnectionIMAPSettings;
import org.bihealth.mi.easysmpc.AppPasswordProvider;
import org.bihealth.mi.easysmpc.resources.Resources;

/**
 * Dialog for entering details of a e-mail box
 * 
 * @author Felix Wirth
 */
public class DialogEmailConfig extends JDialog implements ChangeListener {

    /** SVUID */
    private static final long                serialVersionUID = -5892937473681272650L;
    /** Button */
    private JButton                          buttonOK;
    /** Result */
    private ConnectionIMAPSettings           result;
    /** Parent frame */
    private JFrame                           parent;
    /** Radio button group dialog type simple/advanced */
    private ComponentRadioComfirmSwitchEntry radioDialogType;
    /** Central panel */
    JPanel                                   central;
    /** Encryption type panel */
    JPanel                                   encryptionTypePanel;
    /** Init finished */
    private boolean                          initFinished     = false;
    /** Entry for IMAP details */
    EntryEMailDetails                        entryIMAPDetails;
    /** Entry for SMTP details */
    EntryEMailDetails                        entrySMTPDetails;
    /** E-Mail and password entry */
    private EntryEMailPassword               entryEmailPassword;
    /** Edit or create mode? */
    private boolean                          createMode       = true;
    
    /**
     * Create a new instance
     * 
     * @param settings to fill as default in the fields and deactivate the email field
     * @param parent Component to set the location of JDialog relative to
     */
    public DialogEmailConfig(ConnectionIMAPSettings settings, JFrame parent) {
        this(parent);
        
        // Fill fields
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
                                      new EntryEMailDetailsAdvanced(null, 0, settings, true, this.createMode),
                                      new EntryEMailDetailsAdvanced(null, 0, settings, false, true));
            }
        }
    }

    /**
     * Is the advanced dialog for a ConnectionIMAPSettings object necessary?
     * 
     * @param settings
     * @return
     */
    public static boolean isAdvancedDialogNecessary(ConnectionIMAPSettings settings) {
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
     * Create a new instance
     * 
     * @param parent Component to set the location of JDialog relative to
     */
    public DialogEmailConfig(JFrame parent) {

        // Dialog properties
        this.parent = parent;
        this.setTitle(Resources.getString("EmailConfig.0"));
        this.getContentPane().setLayout(new BorderLayout());
        this.setIconImage(this.parent.getIconImage());
        this.setResizable(false);

        // Title
        central = new JPanel();
        JPanel top = new JPanel();
        JPanel main = new JPanel();
        main.setLayout(new BorderLayout());
        
        // Create central panel and simple/advanced radio 
        central.setLayout(new BoxLayout(central, BoxLayout.Y_AXIS));
        

        // Create switch between simple and advanced dialog
        this.radioDialogType = new ComponentRadioComfirmSwitchEntry(null,
                                                       Resources.getString("EmailConfig.26"),
                                                       Resources.getString("EmailConfig.27"),
                                                       false,
                                                       central,
                                                       Resources.getString("EmailConfig.41"),
                                                       Resources.getString("EmailConfig.42"),
                                                       new BooleanSupplier() {
                                                        
                                                        @Override
                                                        public boolean getAsBoolean() {
                                                            return entryEmailPassword == null && isAdvancedDialogNecessary(entryIMAPDetails, entrySMTPDetails); 
                                                        }
                                                    });
        this.radioDialogType.setChangeListener(this);
        
        // Add
        top.add(radioDialogType);
        main.add(top, BorderLayout.NORTH);
        main.add(central, BorderLayout.CENTER);

        // Buttons
        JPanel buttonsPane = new JPanel();
        buttonsPane.setLayout(new GridLayout(2, 1));
        JPanel okCancelPane = new JPanel();
        okCancelPane.setLayout(new GridLayout(1, 2));
        JButton buttonGuessConfig = new JButton(Resources.getString("EmailConfig.8"));
        this.buttonOK = new JButton(Resources.getString("EmailConfig.6"));
        JButton buttonCancel = new JButton(Resources.getString("EmailConfig.7"));

        // Add
        buttonsPane.add(buttonGuessConfig);
        okCancelPane.add(buttonCancel);
        okCancelPane.add(buttonOK);
        buttonsPane.add(okCancelPane);
        getContentPane().add(buttonsPane, BorderLayout.SOUTH);
        getContentPane().add(main);

        // Listeners
        buttonGuessConfig.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                actionGuessConfig();
            }
        });

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
                DialogEmailConfig.this.result = null;
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
         displaySimpleDialog(null, null, null, null);

        // Set init finished
        this.initFinished = true;
        this.stateChanged(new ChangeEvent(this));
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
        central.removeAll();
        
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
        
        // Create send and receive panel
        JPanel receiveSendPanel = new JPanel();     
        receiveSendPanel.setLayout(new BoxLayout(receiveSendPanel, BoxLayout.X_AXIS));
        
        // Add IMAP panel
        entryIMAPDetails = new EntryEMailDetails(Resources.getString("EmailConfig.37"), ConnectionIMAPSettings.DEFAULT_PORT_IMAP, oldDetailsIMAP);
        receiveSendPanel.add(entryIMAPDetails);                
        
        // Add box to separate
        receiveSendPanel.add(Box.createRigidArea(new Dimension(6, 0)));
        
        // Add SMTP panel
        entrySMTPDetails = new EntryEMailDetails(Resources.getString("EmailConfig.38"), ConnectionIMAPSettings.DEFAULT_PORT_SMTP, oldDetailsSMTP);
        receiveSendPanel.add(entrySMTPDetails);        
        
        // Add to central
        central.add(emailPasswordPanel);
        central.add(receiveSendPanel);
        
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
     * @param password 
     * @param emailAddress 
     */
    private void displayAdvancedDialog(String emailAddress, String password, EntryEMailDetails oldIMAPDetails, EntryEMailDetails oldSMTPDetails) {
        // Remove
        central.removeAll();
        entryEmailPassword = null;
                
        // Create send and receive panel
        JPanel receiveSendPanel = new JPanel();     
        receiveSendPanel.setLayout(new BoxLayout(receiveSendPanel, BoxLayout.X_AXIS));
        
        // Add IMAP panel
        entryIMAPDetails = new EntryEMailDetailsAdvanced(Resources.getString("EmailConfig.37"), ConnectionIMAPSettings.DEFAULT_PORT_IMAP, oldIMAPDetails, emailAddress, password, this.createMode);
        receiveSendPanel.add(entryIMAPDetails);
        
        // Add box to separate
        receiveSendPanel.add(Box.createRigidArea(new Dimension(6, 0)));
        
        // Add SMTP panel
        entrySMTPDetails = new EntryEMailDetailsAdvanced(Resources.getString("EmailConfig.38"), ConnectionIMAPSettings.DEFAULT_PORT_SMTP, oldSMTPDetails, emailAddress, password, true);
        receiveSendPanel.add(entrySMTPDetails);
        
        // Add to central
        central.add(receiveSendPanel);
        
        // Add listeners
        entryIMAPDetails.setChangeListener(this);
        entrySMTPDetails.setChangeListener(this);
        
        // Repaint
        this.revalidate();
        this.repaint();
        
    }

    /**
     * Show this dialog
     */
    public ConnectionIMAPSettings showDialog() {
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

        // Change between simple and advanced dialog
        if (e.getSource() == radioDialogType) {            
            if (this.radioDialogType.isFirstOptionSelected()) {
                // Display simple dialog
                displaySimpleDialog(entryIMAPDetails.getEmailAddress(),
                                    entryIMAPDetails.getPassword(),
                                    entryIMAPDetails,
                                    entrySMTPDetails);
            } else {
                displayAdvancedDialog(entryEmailPassword != null ? entryEmailPassword.getLeftValue() : entryIMAPDetails.getEmailAddress(),
                                      entryEmailPassword != null ? entryEmailPassword.getRightValue() : entryIMAPDetails.getEmailAddress(),
                                      entryIMAPDetails, entrySMTPDetails);
            }
            
            // Resize dialog
            this.pack();
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
            ConnectionIMAPSettings settings = getConnectionSettings();
            if (!settings.isValid()) { throw new BusException("Connection error"); }
            this.result = settings;
            this.dispose();
        } catch (BusException e) {
            JOptionPane.showMessageDialog(this,
                                          Resources.getString("EmailConfig.14"),
                                          Resources.getString("EmailConfig.12"),
                                          JOptionPane.ERROR_MESSAGE);
            return;
        }
    }

    /**
     * Action determine e-mail configuration
     */
    private void actionGuessConfig() {
        
        // Get an entered e-mail address
        String eMailEntered = this.entryEmailPassword != null
                ? this.entryEmailPassword.getLeftValue()
                : this.entryIMAPDetails.getEmailAddress() != null
                        ? this.entryIMAPDetails.getEmailAddress()
                        : this.entrySMTPDetails.getEmailAddress();
        
        // Check
        if (eMailEntered == null || !Participant.isEmailValid(eMailEntered)) {
            
            // Show error message
            JOptionPane.showMessageDialog(this,
                                          Resources.getString("EmailConfig.9"),
                                          Resources.getString("EmailConfig.10"),
                                          JOptionPane.WARNING_MESSAGE);
            // Return
            return;
        }

        // Try to determine and set
        try {
            ConnectionIMAPSettings settings = new ConnectionIMAPSettings(eMailEntered, new AppPasswordProvider());
            if (settings.guess()) {
                // Delete
                entryEmailPassword = null;
                entryIMAPDetails = null;
                entrySMTPDetails = null;
                
                // Display simple or advanced dialog
                if (!isAdvancedDialogNecessary(settings)) {
                    displaySimpleDialog(settings.getIMAPEmailAddress(),
                                        settings.getIMAPPassword(false),
                                        new EntryEMailDetails(null, 0, settings, true),
                                        new EntryEMailDetails(null, 0, settings, false));
                } else {
                    displayAdvancedDialog(null,
                                          null,
                                          new EntryEMailDetailsAdvanced(null, 0, settings, true, this.createMode),
                                          new EntryEMailDetailsAdvanced(null, 0, settings, false, true));
                }

            } else {
                throw new IllegalArgumentException("Configuration could not be guessed");
            }
        } catch (IllegalArgumentException e) {
            JOptionPane.showMessageDialog(this,
                                          Resources.getString("EmailConfig.11"),
                                          Resources.getString("EmailConfig.10"),
                                          JOptionPane.WARNING_MESSAGE);
        }
    }

    /**
     * Checks string for validity
     * 
     * @return
     */
    private boolean areValuesValid() {
        return  (entryEmailPassword != null ? entryEmailPassword.areValuesValid() : true)
                && entryIMAPDetails.areValuesValid() 
                && entrySMTPDetails.areValuesValid();
    }

    /**
     * Create a new connection settings object from data entries
     * 
     * @return connection settings
     * @throws BusException
     */
    private ConnectionIMAPSettings getConnectionSettings() {
        
        // Init
        ConnectionIMAPSettings result;
        
        // Take data from either entryEMail password or from IMAP and SMTP entries
        if(entryEmailPassword != null) {
            result = new ConnectionIMAPSettings(entryEmailPassword.getLeftValue(), new AppPasswordProvider())
                                                .setIMAPPassword(entryEmailPassword.getRightValue())
                                                .setSMTPPassword(entryEmailPassword.getRightValue());
        }
        else {
            AppPasswordProvider provider = new AppPasswordProvider();
            result = new ConnectionIMAPSettings(entryIMAPDetails.getEmailAddress(), provider, entrySMTPDetails.getEmailAddress())
                    .setIMAPPassword(entryIMAPDetails.getPassword())
                    .setSMTPPassword(entrySMTPDetails.getPassword());
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
              .setSMTPAuthMechanisms(entrySMTPDetails.getAuthMechanisms());            
    }
}

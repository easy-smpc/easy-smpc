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
import org.bihealth.mi.easybus.implementations.email.ConnectionIMAPSettings;
import org.bihealth.mi.easysmpc.resources.Resources;

/**
 * Dialog for entering details of a e-mail box
 * 
 * @author Felix Wirth
 */
public class DialogEmailConfig extends JDialog implements ChangeListener {

    /** SVUID */
    private static final long      serialVersionUID = -5892937473681272650L;
    /** E-Mail and password entry */
    private EntryEMailPassword     emailPasswordEntry;
    /** Button */
    private JButton                buttonOK;
    /** Result */
    private ConnectionIMAPSettings result;
    /** Parent frame */
    private JFrame                 parent;
    /** Radio button group dialog type simple/complex */
    private ComponentRadioEntry    radioDialogType;
    /** Central panel */
    JPanel                         central;
    /** Encryption type panel */
    JPanel                         encryptionTypePanel;
    /** Init finished */
    private boolean                initFinished     = false;
    /** Entry for IMAP details */
    EntryEMailDetails              entryIMAPDetails;
    /** Entry for SMTP details */
    EntryEMailDetails              entrySMTPDetails;
    
    /**
     * Create a new instance
     * 
     * @param settings to fill as default in the fields and deactivate the email field
     * @param parent Component to set the location of JDialog relative to
     */
    public DialogEmailConfig(ConnectionIMAPSettings settings, JFrame parent) {
        this(parent);
        if (settings != null) {
            // TODO set values

            // Deactivate e-mail field
            emailPasswordEntry.setLefttEnabled(false);
        }
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
        
        // Create central panel and simple/complex radio 
        central.setLayout(new BoxLayout(central, BoxLayout.Y_AXIS));
        

        
        this.radioDialogType = new ComponentRadioEntry(null,
                                                       Resources.getString("EmailConfig.26"),
                                                       Resources.getString("EmailConfig.27"),
                                                       new ChangeListener() {

                                                           @Override
                                                           public void stateChanged(ChangeEvent e) {
                                                               DialogEmailConfig.this.stateChanged(e);
                                                           }
                                                       },
                                                       false);               
        
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
        this.stateChanged(new ChangeEvent(this));

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
                actionCheckAndProceed();
            }
        });
        
         // Display simple dialog
         displaySimpleDialog();

        // Set init finished
        this.initFinished = true;
    }

    /**
     * Display simple dialog
     */
    private void displaySimpleDialog() {
        // Remove
        central.removeAll();
        
        // Add e-mail password panel
        JPanel emailPasswordPanel = new JPanel();
        emailPasswordPanel.setLayout(new BoxLayout(emailPasswordPanel, BoxLayout.X_AXIS));
        emailPasswordPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED),
                                                                      Resources.getString("EmailConfig.36"),
                                                                      TitledBorder.LEFT,
                                                                      TitledBorder.DEFAULT_POSITION));
        this.emailPasswordEntry = new EntryEMailPassword(Resources.getString("EmailConfig.1"), Resources.getString("EmailConfig.2"));
        emailPasswordPanel.add(emailPasswordEntry);        
        
        // Create send and receive panel
        JPanel receiveSendPanel = new JPanel();     
        receiveSendPanel.setLayout(new BoxLayout(receiveSendPanel, BoxLayout.X_AXIS));
        
        // Add IMAP panel
        entryIMAPDetails = new EntryEMailDetails(Resources.getString("EmailConfig.37"), ConnectionIMAPSettings.DEFAULT_PORT_IMAP);
        entryIMAPDetails.setChangeListener(this);
        receiveSendPanel.add(entryIMAPDetails);                
        
        // Add box to separate
        receiveSendPanel.add(Box.createRigidArea(new Dimension(6, 0)));
        
        // Add SMTP panel
        entrySMTPDetails = new EntryEMailDetails(Resources.getString("EmailConfig.38"), ConnectionIMAPSettings.DEFAULT_PORT_SMTP);
        entrySMTPDetails.setChangeListener(this);
        receiveSendPanel.add(entrySMTPDetails);
        
        // Add to central
        central.add(emailPasswordPanel);
        central.add(receiveSendPanel);
    }
    
    /**
     * Display advanced dialog
     */
    private void displayAdvancedDialog() {
        // Remove
        central.removeAll();
                
        // Create send and receive panel
        JPanel receiveSendPanel = new JPanel();     
        receiveSendPanel.setLayout(new BoxLayout(receiveSendPanel, BoxLayout.X_AXIS));
        
        // Add IMAP panel
        entryIMAPDetails = new EntryEMailDetailsAdvanced(Resources.getString("EmailConfig.37"), ConnectionIMAPSettings.DEFAULT_PORT_IMAP);
        entryIMAPDetails.setChangeListener(this);
        receiveSendPanel.add(entryIMAPDetails);
        
        // Add box to separate
        receiveSendPanel.add(Box.createRigidArea(new Dimension(6, 0)));
        
        // Add SMTP panel
        entrySMTPDetails = new EntryEMailDetailsAdvanced(Resources.getString("EmailConfig.38"), ConnectionIMAPSettings.DEFAULT_PORT_SMTP);
        entrySMTPDetails.setChangeListener(this);
        receiveSendPanel.add(entrySMTPDetails);
        
        // Add to central
        central.add(receiveSendPanel);
        
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
        
        // Check button enabled
        if (this.buttonOK != null) {
            this.buttonOK.setEnabled(areValuesValid());
        }
        
        // Check init already done
        if (!initFinished) {
            return;
        }
        
        // Store current data input
        // TODO
        


        // Change between simple and complex dialog
        if (e.getSource() == radioDialogType) {
            if (this.radioDialogType.isFirstOptionSelected()) {
                displaySimpleDialog();
            } else {
                displayAdvancedDialog();
            }
        }
        
        // Repaint
        this.revalidate();
        this.repaint();
        this.pack();
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
        // TODO Adapt
        // Check
//        if (this.emailPasswordEntryIMAP.getLeftValue() == null ||
//            !Participant.isEmailValid(this.emailPasswordEntryIMAP.getLeftValue())) {
//            JOptionPane.showMessageDialog(this,
//                                          Resources.getString("EmailConfig.9"),
//                                          Resources.getString("EmailConfig.10"),
//                                          JOptionPane.WARNING_MESSAGE);
//            return;
//        }
//
//        // Try to determine and set
//        try {
//            ConnectionIMAPSettings connectionSettings = new ConnectionIMAPSettings(this.emailPasswordEntryIMAP.getLeftValue(),
//                                                                                   new AppPasswordProvider());
//            if (connectionSettings.guess()) {
//                serversEntry.setLeftValue(connectionSettings.getIMAPServer());
//                serversEntry.setRightValue(connectionSettings.getSMTPServer());
//                serverPortsEntry.setLeftValue(Integer.toString(connectionSettings.getIMAPPort()));
//                serverPortsEntry.setRightValue(Integer.toString(connectionSettings.getSMTPPort()));
//                radioEncryptionIMAP.setFirstOptionSelected(connectionSettings.isSSLTLSIMAP());
//                radioEncryptionSMTP.setFirstOptionSelected(connectionSettings.isSSLTLSSMTP());
//            } else {
//                throw new IllegalArgumentException("Configuration could not be guessed");
//            }
//        } catch (IllegalArgumentException e) {
//            JOptionPane.showMessageDialog(this,
//                                          Resources.getString("EmailConfig.11"),
//                                          Resources.getString("EmailConfig.10"),
//                                          JOptionPane.WARNING_MESSAGE);
//        }
    }

    /**
     * Checks string for validity
     * 
     * @return
     */
    private boolean areValuesValid() {
        return true;
        // TODO
//        return this.emailPasswordEntryIMAP.areValuesValid() && this.serversEntry.areValuesValid() &&
//               this.serverPortsEntry.areValuesValid();
    }

    /**
     * Create a new connection settings object from data entries
     * 
     * @return connection settings
     * @throws BusException
     */
    private ConnectionIMAPSettings getConnectionSettings() throws BusException {
        return null;
        // TODO
//        return new ConnectionIMAPSettings(emailPasswordEntryIMAP.getLeftValue(),
//                                          new AppPasswordProvider()).setIMAPPassword(emailPasswordEntryIMAP.getRightValue())
//                                                                    .setIMAPServer(serversEntry.getLeftValue())
//                                                                    .setIMAPPort(Integer.valueOf(serverPortsEntry.getLeftValue()))
//                                                                    .setSMTPServer(serversEntry.getRightValue())
//                                                                    .setSMTPPort(Integer.valueOf(serverPortsEntry.getRightValue()))
//                                                                    .setSSLTLSIMAP(radioEncryptionIMAP.isFirstOptionSelected())
//                                                                    .setSSLTLSSMTP(radioEncryptionSMTP.isFirstOptionSelected());
    }

    private static class ConnectionSettingsIMAPUnchecked {
        String  imapPassword;
        String  imapServer;
        String  imapPort;
        String  smtpServer;
        String  smtpPort;
        boolean ssltlsIMAP;
        boolean ssltlsSMTP;
        String  smptEmailAddress;
        String  smtpPassword;
        String  imapUserName;
        String  smtpUserName;
        String  imapAuthMechanisms;
        String  smtpAuthMechanisms;

        ConnectionSettingsIMAPUnchecked(String imapEmailAddress,
                                        String imapPassword,
                                        String smptEmailAddress,
                                        String smtpPassword,
                                        String imapServer,
                                        String smtpServer,
                                        String imapPort,
                                        String smtpPort,
                                        boolean ssltlsIMAP,
                                        boolean ssltlsSMTP,
                                        String imapUserName,
                                        String smtpUserName,
                                        String imapAuthMechanisms,
                                        String smtpAuthMechanisms) {
            imapPassword = this.imapPassword;
            imapServer = this.imapServer;
            imapPort = this.imapPort;
            smtpServer = this.smtpServer;
            smtpPort = this.smtpPort;
            ssltlsIMAP = this.ssltlsIMAP;
            ssltlsSMTP = this.ssltlsSMTP;
            smptEmailAddress = this.smptEmailAddress;
            smtpPassword = this.smtpPassword;
            imapUserName = this.imapUserName;
            smtpUserName = this.smtpUserName;
            imapAuthMechanisms = this.imapAuthMechanisms;
            smtpAuthMechanisms = this.smtpAuthMechanisms;
        }
    }
}

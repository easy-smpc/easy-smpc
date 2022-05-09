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
    private static final long      serialVersionUID = -5892937473681272650L;
    /** E-Mail and password entry */
    private EntryEMailPassword     emailPasswordEntry;
    /** E-mail server entry */
    private EntryServers           serversEntry;
    /** Port of e-mail servers entry */
    private EntryServerPorts       serverPortsEntry;
    /** Button */
    private JButton                buttonOK;
    /** Result */
    private ConnectionIMAPSettings result;
    /** Parent frame */
    private JFrame                 parent;
    /** Radio button group for encryption IMAP */
    private ComponentRadioEntry    radioEncryptionIMAP;
    /** Radio button group for encryption STMP */
    private ComponentRadioEntry    radioEncryptionSMTP;

    /**
     * Create a new instance
     * 
     * @param settings to fill as default in the fields and deactivate the email field
     * @param parent Component to set the location of JDialog relative to
     */
    public DialogEmailConfig(ConnectionIMAPSettings settings, JFrame parent) {
        this(parent);
        if (settings != null) {
            emailPasswordEntry.setLeftValue(settings.getIMAPEmailAddress());
            emailPasswordEntry.setRightValue(settings.getIMAPPassword(false));
            serversEntry.setLeftValue(settings.getIMAPServer());
            serversEntry.setRightValue(settings.getSMTPServer());
            serverPortsEntry.setLeftValue(Integer.toString(settings.getIMAPPort()));
            serverPortsEntry.setRightValue(Integer.toString(settings.getSMTPPort()));
            radioEncryptionIMAP.setUpperOptionSelected(settings.isSSLTLSIMAP());
            radioEncryptionSMTP.setUpperOptionSelected(settings.isSSLTLSSMTP());
            
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
        JPanel central = new JPanel();
        central.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED),
                                                                                        Resources.getString("EmailConfig.0"),
                                                                                        TitledBorder.CENTER,
                                                                                        TitledBorder.DEFAULT_POSITION));        
        // Entry boxes
        central.setLayout(new BoxLayout(central, BoxLayout.Y_AXIS));
        this.emailPasswordEntry = new EntryEMailPassword();
        this.emailPasswordEntry.setChangeListener(this);
        this.serversEntry = new EntryServers();
        this.serversEntry.setChangeListener(this);
        this.serverPortsEntry = new EntryServerPorts();
        this.serverPortsEntry.setChangeListener(this);
        
        // Panel for encryption radio buttons
        JPanel encryptionTypePanel = new JPanel();
        encryptionTypePanel.setLayout(new BoxLayout(encryptionTypePanel, BoxLayout.X_AXIS));
        radioEncryptionIMAP = new ComponentRadioEntry(Resources.getString("EmailConfig.20"),
                                Resources.getString("EmailConfig.21"),
                                Resources.getString("EmailConfig.22"),
                                this);
        
        radioEncryptionSMTP = new ComponentRadioEntry(Resources.getString("EmailConfig.23"),
                                                                          Resources.getString("EmailConfig.21"),
                                                                          Resources.getString("EmailConfig.22"),
                                                                          this);
        encryptionTypePanel.add(radioEncryptionIMAP);
        encryptionTypePanel.add(radioEncryptionSMTP);
        
        // Add
        central.add(emailPasswordEntry);
        central.add(serversEntry);
        central.add(serverPortsEntry);
        central.add(encryptionTypePanel);
        this.getContentPane().add(central, BorderLayout.CENTER);        
        
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
    }

    /**
     * Show this dialog
     */
    public ConnectionIMAPSettings showDialog(){        
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
            ConnectionIMAPSettings settings = getConnectionSettings();
            if (!settings.isValid()) {
                throw new BusException("Connection error");
            }
            this.result = settings;
            this.dispose();
        } catch (BusException e) {
            JOptionPane.showMessageDialog(this,Resources.getString("EmailConfig.14"), Resources.getString("EmailConfig.12"), JOptionPane.ERROR_MESSAGE);
            return;
        }        
    }
    
    /**
     * Action determine e-mail configuration
     */
    private void actionGuessConfig() {
        // Check
        if (this.emailPasswordEntry.getLeftValue() == null ||
            !Participant.isEmailValid(this.emailPasswordEntry.getLeftValue())) {
            JOptionPane.showMessageDialog(this,Resources.getString("EmailConfig.9"), Resources.getString("EmailConfig.10"), JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Try to determine and set
        try {
            ConnectionIMAPSettings connectionSettings = new ConnectionIMAPSettings(this.emailPasswordEntry.getLeftValue(), new AppPasswordProvider());
            if (connectionSettings.guess()) {
                serversEntry.setLeftValue(connectionSettings.getIMAPServer());
                serversEntry.setRightValue(connectionSettings.getSMTPServer());
                serverPortsEntry.setLeftValue(Integer.toString(connectionSettings.getIMAPPort()));
                serverPortsEntry.setRightValue(Integer.toString(connectionSettings.getSMTPPort()));
                radioEncryptionIMAP.setUpperOptionSelected(connectionSettings.isSSLTLSIMAP());
                radioEncryptionSMTP.setUpperOptionSelected(connectionSettings.isSSLTLSSMTP());
            }
            else {
                throw new IllegalArgumentException("Configuration could not be guessed");
            }
        } catch (IllegalArgumentException e) {
            JOptionPane.showMessageDialog(this,Resources.getString("EmailConfig.11"), Resources.getString("EmailConfig.10"), JOptionPane.WARNING_MESSAGE);
        }
    }

    /**
     * Checks string for validity
     * @return
     */
    private boolean areValuesValid() {
        return this.emailPasswordEntry.areValuesValid() && this.serversEntry.areValuesValid() && this.serverPortsEntry.areValuesValid();
    }

    /**
     * Create a new connection settings object from data entries
     * 
     * @return connection settings
     * @throws BusException
     */
    private ConnectionIMAPSettings getConnectionSettings() throws BusException {
        return new ConnectionIMAPSettings(emailPasswordEntry.getLeftValue(),
                                          new AppPasswordProvider()).setIMAPPassword(emailPasswordEntry.getRightValue())
                                                                    .setIMAPServer(serversEntry.getLeftValue())
                                                                    .setIMAPPort(Integer.valueOf(serverPortsEntry.getLeftValue()))
                                                                    .setSMTPServer(serversEntry.getRightValue())
                                                                    .setSMTPPort(Integer.valueOf(serverPortsEntry.getRightValue()))
                                                                    .setSSLTLSIMAP(radioEncryptionIMAP.isUpperOptionSelected())
                                                                    .setSSLTLSSMTP(radioEncryptionSMTP.isUpperOptionSelected());
    }
}
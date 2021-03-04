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
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.bihealth.mi.easybus.BusException;
import org.bihealth.mi.easybus.Participant;
import org.bihealth.mi.easybus.implementations.email.ConnectionIMAP;
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

        
    /**
     * Create a new instance
     * 
     * @param connectionsSettings to fill as default in the fields
     * @param parent Component to set the location of JDialog relative to
     */
    public DialogEmailConfig(ConnectionIMAPSettings connectionsSettings, JFrame parent) {
        this(parent);
        setFieldsFromConnectionSettings(connectionsSettings);
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
        JLabel warningText1 = new JLabel(Resources.getString("EmailConfig.20"));
        warningText1.setForeground(Color.RED);
        JLabel warningText2 = new JLabel(Resources.getString("EmailConfig.21"));
        warningText2.setForeground(Color.RED);
        this.emailPasswordEntry = new EntryEMailPassword();
        this.emailPasswordEntry.setChangeListener(this);
        this.serversEntry = new EntryServers();
        this.serversEntry.setChangeListener(this);
        this.serverPortsEntry = new EntryServerPorts();
        this.serverPortsEntry.setChangeListener(this);
        
        // Add
        central.add(warningText1);
        central.add(warningText2);
        central.add(emailPasswordEntry);
        central.add(serversEntry);
        central.add(serverPortsEntry);
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
                DialogEmailConfig.this.result = null;
                DialogEmailConfig.this.dispose();
            }
        });
        
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent windowEvent) {
                DialogEmailConfig.this.result = null;
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
        this.buttonOK.setEnabled(areValuesValid());
    }
    
    /**
     * Action close
     */
    private void actionCheckAndProceed() {
        try {
            new ConnectionIMAP(connectionSettingsFromEntries(), true).checkConnection();
            this.result = connectionSettingsFromEntries();
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
            ConnectionIMAPSettings connectionSettings = new ConnectionIMAPSettings(this.emailPasswordEntry.getLeftValue());
            if (connectionSettings.guess()) {
                serversEntry.setLeftValue(connectionSettings.getIMAPServer());
                serversEntry.setRightValue(connectionSettings.getSMTPServer());
                serverPortsEntry.setLeftValue(Integer.toString(connectionSettings.getIMAPPort()));
                serverPortsEntry.setRightValue(Integer.toString(connectionSettings.getSMTPPort()));
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
    private ConnectionIMAPSettings connectionSettingsFromEntries() throws BusException {
        return new ConnectionIMAPSettings(emailPasswordEntry.getLeftValue()).setPassword(emailPasswordEntry.getRightValue())
                                                                            .setIMAPServer(serversEntry.getLeftValue())
                                                                            .setIMAPPort(Integer.valueOf(serverPortsEntry.getLeftValue()))
                                                                            .setSMTPServer(serversEntry.getRightValue())
                                                                            .setSMTPPort(Integer.valueOf(serverPortsEntry.getRightValue()));
    }
    
    /**
     * Sets fields in the dialog from a connection settings object
     * 
     * @param connectionsSettings
     */
    private void setFieldsFromConnectionSettings(ConnectionIMAPSettings connectionsSettings) {
        if (connectionsSettings != null) {
            emailPasswordEntry.setLeftValue(connectionsSettings.getEmailAddress());
            emailPasswordEntry.setRightValue(connectionsSettings.getPassword());
            serversEntry.setLeftValue(connectionsSettings.getIMAPServer());
            serversEntry.setRightValue(connectionsSettings.getSMTPServer());
            serverPortsEntry.setLeftValue(Integer.toString(connectionsSettings.getIMAPPort()));
            serverPortsEntry.setRightValue(Integer.toString(connectionsSettings.getSMTPPort()));
        }
    } 
}
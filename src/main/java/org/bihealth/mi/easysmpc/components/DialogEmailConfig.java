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
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.prefs.Preferences;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
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
import org.bihealth.mi.easysmpc.resources.Resources.HashMapStringConnectionIMAPSettings;

/**
 * Dialog for entering details of a e-mail box
 * 
 * @author Felix Wirth
 */
public class DialogEmailConfig extends JDialog implements ChangeListener {

    /** SVID */
    private static final long serialVersionUID = -5892937473681272650L;          
    /** E-Mail and password entry*/
    private EntryEMailPassword emailPasswordEntry; //TODO: Make password not visible    
    /** E-mail server entry */
    private EntryServers serversEntry;
    /** Port of e-mail servers entry */
    private EntryServerPorts serverPortsEntry;
    /** Button*/
    private JButton buttonCheckConnection;
    /** Button*/
    private JButton buttonOK;
    /** Button */
    private JButton buttonLoadPrevious;
    /** Result */
    private ConnectionIMAPSettings result;
    /** Parent frame */
    private JFrame parent;

        
    /**
     * Create a new instance
     * 
     * @param parent Component to set the location of JDialog relative to
     */
    public DialogEmailConfig(JFrame parent) {

        // Dialog properties
        this.parent = parent;
        this.setSize(Resources.SIZE_DIALOG_SMALL_X, Resources.SIZE_DIALOG_SMALL_Y);
        this.setLocationRelativeTo(this.parent);
        this.setTitle(Resources.getString("EmailConfig.0"));
        this.getContentPane().setLayout(new BorderLayout());
        this.setIconImage(this.parent.getIconImage());
        
        // Title
        ((JComponent) this.getContentPane()).setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED),
                                                                                        Resources.getString("EmailConfig.0"),
                                                                                        TitledBorder.CENTER,
                                                                                        TitledBorder.DEFAULT_POSITION));
        
        // Entry boxes
        JPanel central = new JPanel();
        central.setLayout(new GridLayout(3, 1));
        this.emailPasswordEntry = new EntryEMailPassword();
        this.emailPasswordEntry.setChangeListener(this);
        this.serversEntry = new EntryServers();
        this.serversEntry.setChangeListener(this);
        this.serverPortsEntry = new EntryServerPorts();
        this.serverPortsEntry.setChangeListener(this);
        // Add
        central.add(emailPasswordEntry);
        central.add(serversEntry);
        central.add(serverPortsEntry);
        this.getContentPane().add(central, BorderLayout.CENTER);        
        
        // Buttons        
        JPanel buttonsPane = new JPanel();
        buttonsPane.setLayout(new GridLayout(4, 1));      
        JPanel okCancelPane = new JPanel();
        okCancelPane.setLayout(new GridLayout(1, 2));
        JButton buttonGuessConfig = new JButton(Resources.getString("EmailConfig.8"));
        buttonLoadPrevious = new JButton(Resources.getString("EmailConfig.17"));
        this.buttonCheckConnection = new JButton(Resources.getString("EmailConfig.5"));
        this.buttonOK = new JButton(Resources.getString("EmailConfig.6"));
        JButton buttonCancel = new JButton(Resources.getString("EmailConfig.7"));
        // Add
        buttonsPane.add(buttonLoadPrevious);
        buttonsPane.add(buttonGuessConfig);
        buttonsPane.add(buttonCheckConnection);
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
        
        buttonLoadPrevious.addActionListener(new ActionListener() {            
            @Override
            public void actionPerformed(ActionEvent e) {
                actionLoadPreviousConfig();
            }
        });
        
        this.buttonCheckConnection.addActionListener(new ActionListener() {            
            @Override
            public void actionPerformed(ActionEvent e) {
                actionCheckConnection();
            }
        });
        
        this.buttonOK.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                actionClose();
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
     * Sets fields from previous configurations
     */
    protected void actionLoadPreviousConfig() {
        try {
            setFieldsFromConnectionSettings(new DialogEmailConfigPrevious(parent).showDialog());
        } catch (ClassNotFoundException | IOException e) {
            JOptionPane.showMessageDialog(this,Resources.getString("EmailConfig.18"), Resources.getString("EmailConfig.17"), JOptionPane.ERROR_MESSAGE);
        }
    }

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

    /**
     * Action check connection
     */
    protected void actionCheckConnection() {
        try {
            new ConnectionIMAP(connectionSettingsFromEntries(), true).checkConnection();
            buttonOK.setEnabled(true);
        } catch (BusException e) {
            JOptionPane.showMessageDialog(this,Resources.getString("EmailConfig.14"), Resources.getString("EmailConfig.12"), JOptionPane.ERROR_MESSAGE);
        }        
    }

    /**
     * Show this dialog
     */
    public ConnectionIMAPSettings showDialog(){        
        this.setModal(true);
        this.setVisible(true);
        return this.result;
    }
    
    /**
     * Reacts to changes
     */
    @Override
    public void stateChanged(ChangeEvent e) {
        this.buttonCheckConnection.setEnabled(this.areValuesValid());
        this.buttonOK.setEnabled(false);
        this.buttonLoadPrevious.setEnabled((arePreviousConfigsExisting()));
    }
      
    /**
     * Check if previous configuration exist
     * @return
     */
    private boolean arePreviousConfigsExisting() {
        return DialogEmailConfigPrevious.getConnectionSettingFromPreferences() != null ? true : false;
    }

    /**
     * Checks string for validity
     * @return
     */
    private boolean areValuesValid() {
        return this.emailPasswordEntry.areValuesValid() && this.serversEntry.areValuesValid() && this.serverPortsEntry.areValuesValid();
    }
    
    /**
     * Action close
     */
    private void actionClose() {
        try {
            this.result = connectionSettingsFromEntries();
            storeConnectionSettingsInPreferences(this.result);
            DialogEmailConfig.this.dispose();
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,Resources.getString("EmailConfig.13"), Resources.getString("EmailConfig.12"), JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Stores the settings in a map in the Preferences
     * 
     * @param connectionSettings
     * @throws IOException 
     * @throws ClassNotFoundException 
     */
    private void storeConnectionSettingsInPreferences(ConnectionIMAPSettings connectionSettings) throws ClassNotFoundException, IOException {
        // Prepare
        Preferences userPreferences = Preferences.userRoot().node(this.getClass().getPackage().getName());
        HashMapStringConnectionIMAPSettings connectionSettingsMap;
        // TODO remove password?
        
        // Load connectionSettingsMap from preference or create
        if (DialogEmailConfigPrevious.getConnectionSettingFromPreferences() != null) {
            ByteArrayInputStream in = new ByteArrayInputStream(userPreferences.getByteArray(Resources.CONNECTION_SETTINGS_MAP, null));
            Object o = new ObjectInputStream(in).readObject();
            if (!(o instanceof HashMapStringConnectionIMAPSettings)) {
                throw new IOException("Existing connection settings map can not be read");
            }
            connectionSettingsMap = (HashMapStringConnectionIMAPSettings) o;
        }
        else {
            connectionSettingsMap = new HashMapStringConnectionIMAPSettings();
        }
        
        // Put connection settings
        connectionSettingsMap.put(connectionSettings.getEmailAddress(), connectionSettings);
        
        // Save in preferences
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(connectionSettingsMap);
        oos.flush();
        userPreferences.putByteArray(Resources.CONNECTION_SETTINGS_MAP , bos.toByteArray());
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
}
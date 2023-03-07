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
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.BackingStoreException;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.AbstractTableModel;

import org.bihealth.mi.easybus.ConnectionSettings;
import org.bihealth.mi.easybus.ConnectionSettings.ExchangeMode;
import org.bihealth.mi.easybus.implementations.email.ConnectionSettingsIMAP;
import org.bihealth.mi.easybus.implementations.http.easybackend.ConnectionSettingsEasyBackend;
import org.bihealth.mi.easysmpc.Perspective1ACreate.ConnectionSettingsRenderer;
import org.bihealth.mi.easysmpc.resources.Connections;
import org.bihealth.mi.easysmpc.resources.Resources;

/**
 * Dialog for selecting an initial message from a backend
 * 
 * @author Felix Wirth
 */
public class DialogMessagePicker extends JDialog implements ChangeListener {

    /** SVUID */
    private static final long             serialVersionUID = -293485237040176324L;
    /** Result */
    private String                        result;
    /** Add configuration e-mail box */
    private JButton                       buttonAddExchangeConfig;

    /** Combo box to select exchange mode */
    private JComboBox<ExchangeMode>       comboExchangeMode;

    /** Combo box to select exchange configuration */
    private JComboBox<ConnectionSettings> comboExchangeConfig;

    /** Add configuration e-mail box */
    private JButton                       buttonRemoveExchangeConfig;

    /** Edit configuration e-mail box */
    private JButton                       buttonEditExchangeConfig;

    /** Button */
    private JButton                       buttonOK;
    /** Parent frame */
    private final JFrame                  parentFrame;
    /** Table model */
    private final TableModelMessages      tableModel       = new TableModelMessages();

    /**
     * Stores the content of a message
     * 
     * @author Felix Wirth
     *
     */
    private static class Message {
        private final String name;
        private final String participants;
        private final String variable;

        public Message(String name, String participants, String variable) {
            super();
            this.name = name;
            this.participants = participants;
            this.variable = variable;
        }

        /**
         * @return the name
         */
        public String getName() {
            return name;
        }

        /**
         * @return the participants
         */
        public String getParticipants() {
            return participants;
        }

        /**
         * @return the variable
         */
        public String getVariable() {
            return variable;
        }
    }
    
    /**
     * Table model for messages
     * 
     * @author Felix Wirth
     *
     */
    private static class TableModelMessages extends AbstractTableModel{
        /** SVUID */
        private static final long serialVersionUID = 59144941823302094L;
        /** Data for table */
        private List<Message> messages = new ArrayList<>();
        /** Column names */
        private final String[] columnNames = { Resources.getString("DialogMessagePicker.2"),
                                 Resources.getString("DialogMessagePicker.3"),
                                 Resources.getString("DialogMessagePicker.4") };

        @Override
        public int getRowCount() {
            return messages.size();
        }

        @Override
        public int getColumnCount() {
            return 3;
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            // Choose correct getter
            switch(columnIndex) {
            case 0:
                return messages.get(rowIndex).getName();
            case 1:
                return messages.get(rowIndex).getParticipants();
            case 2:
                return messages.get(rowIndex).getVariable();
             default:
                 return null;
            }
        }
        
        @Override
        public String getColumnName(int col) {
            return columnNames[col];
        }
        
        /** Update data and repaint table
         * @param messages
         */
        public void changeAndUpdate(List<Message> messages) {
            this.messages = messages;
            this.fireTableDataChanged();
        }
    }
    
    /**
     * Create a new instance
     * @param parent Component to set the location of JDialog relative to
     */
    public DialogMessagePicker(JFrame parent) {

        // Save
        this.parentFrame = parent;

        // Dialog properties
        this.setTitle(Resources.getString("DialogMessagePicker.0"));
        this.getContentPane().setLayout(new BorderLayout());
        this.setIconImage(parent.getIconImage());

        // Title
        ((JComponent) this.getContentPane()).setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED),
                                                                                        Resources.getString("DialogMessagePicker.1"),
                                                                                        TitledBorder.CENTER,
                                                                                        TitledBorder.DEFAULT_POSITION));

        // Panel for exchange config
        JPanel automaticExchangePanel = new JPanel();
        automaticExchangePanel.setLayout(new BoxLayout(automaticExchangePanel, BoxLayout.X_AXIS));

        // Combo boxes for exchange mode & config
        comboExchangeMode = new JComboBox<>(new ExchangeMode[] {ExchangeMode.EASYBACKEND});
        comboExchangeMode.setSelectedItem(ExchangeMode.EASYBACKEND);
        comboExchangeMode.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                stateChanged(new ChangeEvent(comboExchangeMode));
            }
        });

        comboExchangeConfig = new JComboBox<>();
        comboExchangeConfig.setRenderer(new ConnectionSettingsRenderer());
        comboExchangeConfig.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                stateChanged(new ChangeEvent(this));
            }
        });

        // Button to add e-mail config
        buttonAddExchangeConfig = new JButton(Resources.getString("PerspectiveCreate.OpenEMailConfigAdd"));
        buttonAddExchangeConfig.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                actionAddExchangeConf();
            }
        });

        // Button to edit e-mail config
        buttonEditExchangeConfig = new JButton(Resources.getString("PerspectiveCreate.OpenEMailConfigEdit"));
        buttonEditExchangeConfig.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                actionEditExchangeConf();
            }
        });

        // Button to remove e-mail config
        buttonRemoveExchangeConfig = new JButton(Resources.getString("PerspectiveCreate.OpenEMailConfigRemove"));
        buttonRemoveExchangeConfig.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                actionRemoveExchangeConf();
            }
        });

        // Add to exchange panel and north
        automaticExchangePanel.add(comboExchangeMode);
        automaticExchangePanel.add(comboExchangeConfig);
        automaticExchangePanel.add(buttonAddExchangeConfig);
        automaticExchangePanel.add(buttonEditExchangeConfig);
        automaticExchangePanel.add(buttonRemoveExchangeConfig);
        this.getContentPane().add(automaticExchangePanel, BorderLayout.NORTH);

        // Create table
        JTable table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane panelTable = new JScrollPane(table, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        // Add table
        this.getContentPane().add(panelTable, BorderLayout.CENTER);

        // Button
        JPanel buttonsPane = new JPanel();
        buttonsPane.setLayout(new GridLayout(1, 2));
        this.getContentPane().add(buttonsPane, BorderLayout.SOUTH);
        this.buttonOK = new JButton(Resources.getString("PerspectiveParticipate.ok"));
        this.buttonOK.setEnabled(this.areValuesValid());
        JButton buttonCancel = new JButton(Resources.getString("PerspectiveParticipate.cancel"));
        buttonsPane.add(buttonCancel);
        buttonsPane.add(buttonOK);

        // Listeners
        this.buttonOK.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                actionProceed();
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
                DialogMessagePicker.this.result = null;
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

        // Finalize
        stateChanged(new ChangeEvent(comboExchangeMode));
        pack();
        setLocationRelativeTo(parent);
    }

    /**
     * Removes a configuration
     */
    private void actionRemoveExchangeConf() {
        try {
            Connections.remove((ConnectionSettings) this.comboExchangeConfig.getSelectedItem());
        } catch (BackingStoreException e) {
            JOptionPane.showMessageDialog(this,
                                          Resources.getString("PerspectiveCreate.ErrorDeletePreferences"),
                                          Resources.getString("PerspectiveCreate.Error"),
                                          JOptionPane.ERROR_MESSAGE);
        }

        // Reset combo box
        comboExchangeConfig.removeAllItems();
        for (ConnectionSettings settings : getExchangeConfig()) {
            this.comboExchangeConfig.addItem(settings);
        }

        // State changed
        this.stateChanged(new ChangeEvent(this));
    }

    /**
     * Show this dialog
     */
    public String showDialog() {
        this.setModal(true);
        this.setVisible(true);
        return this.result;
    }

    /**
     * Reacts to changes
     */
    @Override
    public void stateChanged(ChangeEvent e) {
        this.buttonOK.setEnabled(this.areValuesValid());

        // Change in combo box exchange
        if (e.getSource() == comboExchangeMode) {
            // Is the combo exchange config enabled?
            if (comboExchangeMode.getSelectedItem() == null ||
                ((ExchangeMode) comboExchangeMode.getSelectedItem()).equals(ExchangeMode.MANUAL)) {
                comboExchangeConfig.setEnabled(false);
                buttonAddExchangeConfig.setEnabled(false);
                comboExchangeConfig.setSelectedItem(null);
                comboExchangeConfig.removeAllItems();
            } else {

                // Set enabled
                comboExchangeConfig.setEnabled(true);
                buttonAddExchangeConfig.setEnabled(true);

                // Reset combo box
                ConnectionSettings currentSetting = (ConnectionSettings) comboExchangeConfig.getSelectedItem();
                comboExchangeConfig.removeAllItems();
                for (ConnectionSettings settings : getExchangeConfig()) {
                    comboExchangeConfig.addItem(settings);

                    // Set selected
                    if (currentSetting != null && settings != null &&
                        settings.getIdentifier().equals(currentSetting.getIdentifier())) {
                        settings.setPasswordStore(currentSetting.getPasswordStore());
                        comboExchangeConfig.setSelectedItem(settings);
                    }
                }
            }
        }
    }

    /**
     * Action cancel and close
     */
    private void actionCancel() {
        this.result = null;
        this.dispose();
    }

    /**
     * Action proceed and close
     */
    private void actionProceed() {
        // TODO set this.result
        this.dispose();
    }

    /**
     * Checks string for validity
     * 
     * @return
     */
    private boolean areValuesValid() {
        // TODO Selected study
        return false;
    }

    /**
     * Returns previous configurations
     * 
     * @return
     */
    private ConnectionSettings[] getExchangeConfig() {
        try {
            // Read from preferences
            ArrayList<ConnectionSettings> configFromPreferences;

            switch ((ExchangeMode) this.comboExchangeMode.getSelectedItem()) {
            case EASYBACKEND:
                configFromPreferences = Connections.list(ConnectionSettingsEasyBackend.class);
                break;
            case EMAIL:
                configFromPreferences = Connections.list(ConnectionSettingsIMAP.class);
                break;
            case MANUAL:
                configFromPreferences = new ArrayList<>();
            default:
                configFromPreferences = new ArrayList<>();
            }

            // Add null for non-automatic
            return configFromPreferences.toArray(new ConnectionSettings[configFromPreferences.size()]);
        } catch (BackingStoreException | ClassNotFoundException | IOException e) {
            JOptionPane.showMessageDialog(this,
                                          Resources.getString("PerspectiveCreate.ErrorLoadingPreferences"),
                                          Resources.getString("PerspectiveCreate.Error"),
                                          JOptionPane.ERROR_MESSAGE);
            return null;
        }
    }

    /**
     * Adds an e-mail configuration
     */
    private void actionAddExchangeConf() {
        // Get new settings
        ConnectionSettings newSettings;
        switch ((ExchangeMode) this.comboExchangeMode.getSelectedItem()) {
        case EASYBACKEND:
            newSettings = new DialogEasyBackendConfig(null, parentFrame).showDialog();
            break;
        default:
            return;
        }

        if (newSettings != null) {
            // Update connections in preferences
            try {
                Connections.addOrUpdate(newSettings);
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this,
                                              Resources.getString("PerspectiveCreate.ErrorStorePreferences"),
                                              Resources.getString("PerspectiveCreate.Error"),
                                              JOptionPane.ERROR_MESSAGE);
            }

            // Reset combo box
            comboExchangeConfig.removeAllItems();
            for (ConnectionSettings settings : getExchangeConfig()) {
                this.comboExchangeConfig.addItem(settings);

                // Set selected
                if (settings != null &&
                    settings.getIdentifier().equals(newSettings.getIdentifier())) {
                    settings.setPasswordStore(newSettings.getPasswordStore());
                    this.comboExchangeConfig.setSelectedItem(settings);
                }
            }
        }
        this.stateChanged(new ChangeEvent(this));
    }

    /**
     * Edits an e-mail configuration
     */
    private void actionEditExchangeConf() {
        // Get new settings
        ConnectionSettings newSettings = null;

        // Is EasyBackend settings object?
        if (this.comboExchangeConfig.getSelectedItem() instanceof ConnectionSettingsEasyBackend) {
            newSettings = new DialogEasyBackendConfig((ConnectionSettingsEasyBackend) this.comboExchangeConfig.getSelectedItem(),
                                                      parentFrame).showDialog();
        }

        // Alter combo box if new settings given
        if (newSettings != null) {
            // Update connections in preferences
            try {
                Connections.addOrUpdate(newSettings);
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this,
                                              Resources.getString("PerspectiveCreate.ErrorStorePreferences"),
                                              Resources.getString("PerspectiveCreate.Error"),
                                              JOptionPane.ERROR_MESSAGE);
            }

            // Reset combo box
            comboExchangeConfig.removeAllItems();
            for (ConnectionSettings settings : getExchangeConfig()) {
                this.comboExchangeConfig.addItem(settings);

                // Set selected
                if (settings != null &&
                    settings.getIdentifier().equals(newSettings.getIdentifier())) {
                    settings.setPasswordStore(newSettings.getPasswordStore());
                    this.comboExchangeConfig.setSelectedItem(settings);
                }
            }
        }

        // Change state
        this.stateChanged(new ChangeEvent(this));
    }
}

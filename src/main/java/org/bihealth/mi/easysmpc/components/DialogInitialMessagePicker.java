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
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.SwingWorker;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.AbstractTableModel;

import org.bihealth.mi.easybus.BusException;
import org.bihealth.mi.easybus.BusMessage;
import org.bihealth.mi.easybus.ConnectionSettings;
import org.bihealth.mi.easybus.InitialMessageManager;
import org.bihealth.mi.easybus.implementations.email.ConnectionSettingsIMAP;
import org.bihealth.mi.easybus.implementations.email.InitialMessageManagerEmail;
import org.bihealth.mi.easybus.implementations.http.easybackend.ConnectionSettingsEasyBackend;
import org.bihealth.mi.easybus.implementations.http.easybackend.InitialMessageManagerEasyBackend;
import org.bihealth.mi.easybus.implementations.local.ConnectionSettingsManual;
import org.bihealth.mi.easysmpc.resources.Resources;

import de.tu_darmstadt.cbs.emailsmpc.Message;
import de.tu_darmstadt.cbs.emailsmpc.MessageInitial;

/**
 * Dialog for selecting an initial message from a backend
 * 
 * @author Felix Wirth
 */
public class DialogInitialMessagePicker extends JDialog implements ChangeListener {

    /** SVUID */
    private static final long                   serialVersionUID = -293485237040176324L;
    /** Result */
    private String                              result;
    /** Button */
    private final JButton                       buttonOK;
    /** Table model */
    private final TableModelMessages            tableModel       = new TableModelMessages();
    /** Initial message manager */
    private InitialMessageManager               messageManager   = null;
    /** Table for messages */
    private final JTable                        table;
    /** Settings */
    private ConnectionSettings                  settings;

    /**
     * Table model for messages
     * @author Felix Wirth
     */
    private static class TableModelMessages extends AbstractTableModel{
        /** SVUID */
        private static final long serialVersionUID = 59144941823302094L;
        /** Data for table */
        private List<BusMessage> messages = new ArrayList<>();
        /** Column names */
        private final String[]                   columnNames      = { Resources.getString("DialogMessagePicker.2"),
                                                                      Resources.getString("DialogMessagePicker.3") };
        /** Table */
        private JTable table;

        @Override
        public int getRowCount() {
            return messages.size();
        }

        @Override
        public int getColumnCount() {
            return 2;
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            

            try {
                // Get message
                String data;
                data = Message.deserializeMessage(messages.get(rowIndex).getMessage()).data;
                MessageInitial messageInitial = MessageInitial.decodeMessage(Message.getMessageData(data));

                // Choose correct data part of message
                switch (columnIndex) {
                case 0:
                    return messageInitial.name;
                case 1:
                    return messageInitial.participants[0].name;
                default:
                    return null;
                }
            } catch (ClassNotFoundException | IOException e) {
                JOptionPane.showMessageDialog(null, Resources.getString("DialogMessagePicker.4"), Resources.getString("DialogMessagePicker.7"), JOptionPane.ERROR_MESSAGE);
                throw new IllegalStateException("Unable to understand message", e);
            }
        }
        
        @Override
        public String getColumnName(int col) {
            return columnNames[col];
        }
        
        /** Update data and repaint table
         * @param messages
         */
        public void changeAndUpdate(List<BusMessage> messages) {
            this.messages = messages;
            int selectectRow = this.table.getSelectedRow();
            this.fireTableDataChanged();
            if (this.table != null && selectectRow >= 0) {
                this.table.setRowSelectionInterval(selectectRow, selectectRow);
            }
        }
        
        /**
         * Return ID of selected message
         * @return
         */
        public BusMessage getSelectedMessage() {
            return messages.get(table.getSelectedRow());
        }
        
        
        /**
         * Set table
         * @param table
         */
        public void setTable(JTable table) {
            this.table = table;
        }
    }
    
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
            } else {
                label.setText(Resources.getString("DialogMessagePicker.8"));
            } 
            return label;
        }
    }
    
    /**
     * Create a new instance
     * @param parent Component to set the location of JDialog relative to
     * @param connectionSettings
     */
    public DialogInitialMessagePicker(JFrame parent, ConnectionSettings settings) {
        // Check
        if(settings instanceof ConnectionSettingsManual) {
            throw new IllegalStateException("Connection settings can not be of type manual");
        }
        
        // Save
        this.settings = settings;

        // Dialog properties
        this.setTitle(Resources.getString("DialogMessagePicker.0"));
        this.getContentPane().setLayout(new BorderLayout());
        this.setIconImage(parent.getIconImage());

        // Title
        ((JComponent) this.getContentPane()).setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED),
                                                                                        Resources.getString("DialogMessagePicker.1"),
                                                                                        TitledBorder.CENTER,
                                                                                        TitledBorder.DEFAULT_POSITION));

        // Create table
        table = new JTable(tableModel);
        tableModel.setTable(table);
        table.addMouseListener(new MouseListener() {
            
            @Override
            public void mouseReleased(MouseEvent e) {
                // Empty
            }
            
            @Override
            public void mousePressed(MouseEvent e) {
                // Empty
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                // Empty
            }
            
            @Override
            public void mouseEntered(MouseEvent e) {
                // Empty
            }
            
            @Override
            public void mouseClicked(MouseEvent e) {
                stateChanged(new ChangeEvent(this));
            }
        });
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
                DialogInitialMessagePicker.this.result = null;
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
        
        // Start receiving
        startReceiving();
        
        // Finalize
        stateChanged(new ChangeEvent(this));
        pack();
        setLocationRelativeTo(parent);
    }

    /**
     * Starts message receiving
     */
    private void startReceiving() {
        
        // Stop current manager
        if(messageManager != null) {
           messageManager.stop();
        }
        
        // Type EasyBackend
        if(settings instanceof ConnectionSettingsEasyBackend) {
            
            // Create message manager
            messageManager = new InitialMessageManagerEasyBackend(new Consumer<List<BusMessage>>() {
                
                @Override
                public void accept(List<BusMessage> messages) {
                    tableModel.changeAndUpdate(messages);
                }
            }, new Consumer<Exception>() {

                @Override
                public void accept(Exception e) {
                    JOptionPane.showMessageDialog(null, Resources.getString("DialogMessagePicker.5"), Resources.getString("DialogMessagePicker.7"), JOptionPane.ERROR_MESSAGE);
                }
            }
            , (ConnectionSettingsEasyBackend) settings, 5000);
        }
        
        // Type e-mail
        if(settings instanceof ConnectionSettingsIMAP) {
            messageManager = new InitialMessageManagerEmail(new Consumer<List<BusMessage>>() {

                @Override
                public void accept(List<BusMessage> messages) {
                    tableModel.changeAndUpdate(messages);
                }
            }, new Consumer<Exception>() {

                @Override
                public void accept(Exception e) {
                    JOptionPane.showMessageDialog(null, Resources.getString("DialogMessagePicker.5"), Resources.getString("DialogMessagePicker.7"), JOptionPane.ERROR_MESSAGE);
                }
            }, (ConnectionSettingsIMAP) settings, 5000);
        }
        
        // Start message manager
        new SwingWorker<>() {

            @Override
            protected Void doInBackground() throws Exception {
                messageManager.start();
                return null;
            }
        }.execute();
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
    }

    /**
     * Action cancel and close
     */
    private void actionCancel() {
        if (messageManager != null) {
            messageManager.stop();
        }
        this.result = null;
        this.dispose();
    }

    /**
     * Action proceed and close
     */
    private void actionProceed() {
        // Set result
        this.result = this.tableModel.getSelectedMessage().getMessage();
        
        // Delete message
        try {
            this.tableModel.getSelectedMessage().delete();
            this.tableModel.getSelectedMessage().expunge();
        } catch (BusException e) {
            JOptionPane.showMessageDialog(null, Resources.getString("DialogMessagePicker.6"), Resources.getString("DialogMessagePicker.7"), JOptionPane.ERROR_MESSAGE);
        }
        
        // Stop and close dialog
        if(this.messageManager != null) {
            messageManager.stop();
        }
        this.dispose();
    }

    /**
     * Checks string for validity
     * 
     * @return
     */
    private boolean areValuesValid() {
        return !table.getSelectionModel().isSelectionEmpty();
    }
}

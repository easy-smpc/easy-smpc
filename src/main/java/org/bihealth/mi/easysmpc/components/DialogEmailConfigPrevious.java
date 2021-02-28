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
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.prefs.Preferences;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

import org.bihealth.mi.easybus.implementations.email.ConnectionIMAPSettings;
import org.bihealth.mi.easysmpc.resources.Resources;
import org.bihealth.mi.easysmpc.resources.Resources.HashMapStringConnectionIMAPSettings;

/**
 * Dialog for entering details of a e-mail box
 * 
 * @author Felix Wirth
 */
public class DialogEmailConfigPrevious extends JDialog {

    /** SVUID */
    private static final long serialVersionUID = 640267685135868545L;
    /** List to select */
    private JList<ConnectionIMAPSettings> list;
    /** Result */
    private ConnectionIMAPSettings result;

    /** Allows to set a custom text for each object in the list */
    private class CustomRenderer extends DefaultListCellRenderer {
        /** SVUID */
        private static final long serialVersionUID = 1L;

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
            label.setText(((ConnectionIMAPSettings) value).getEmailAddress());
            return label;
        }
    };
    
        
    /**
     * Create a new instance
     * 
     * @param parent Component to set the location of JDialog relative to
     * @throws IOException 
     * @throws ClassNotFoundException 
     */
    public DialogEmailConfigPrevious(JFrame parent) throws ClassNotFoundException, IOException {

        // Dialog properties
        this.setSize(Resources.SIZE_DIALOG_SMALL_X - 125, Resources.SIZE_DIALOG_SMALL_Y);
        this.setLocationRelativeTo(parent);
        this.setTitle(Resources.getString("EmailConfig.17"));
        this.getContentPane().setLayout(new BorderLayout());
        this.setIconImage(parent.getIconImage());
        
        // Title
        ((JComponent) this.getContentPane()).setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED),
                                                                                        Resources.getString("EmailConfig.17"),
                                                                                        TitledBorder.CENTER,
                                                                                        TitledBorder.DEFAULT_POSITION));
        // Central panel
        JPanel central = new JPanel();
        list = new JList<>(getConnectionSettingFromPreferences());
        list.setCellRenderer(new CustomRenderer());
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);        
        // Add
        central.add(new JScrollPane(list,
                                    JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                                    JScrollPane.HORIZONTAL_SCROLLBAR_NEVER));
        this.getContentPane().add(central, BorderLayout.CENTER);        
        
        // Buttons             
        JPanel okCancelPane = new JPanel();
        okCancelPane.setLayout(new GridLayout(1, 2));
        JButton buttonOK = new JButton(Resources.getString("EmailConfig.6"));
        JButton buttonCancel = new JButton(Resources.getString("EmailConfig.7"));
        // Add
        okCancelPane.add(buttonCancel);
        okCancelPane.add(buttonOK);
        getContentPane().add(okCancelPane, BorderLayout.SOUTH);                
        
        buttonOK.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                actionClose();
            }
        });
        
        buttonCancel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                DialogEmailConfigPrevious.this.result = null;
                DialogEmailConfigPrevious.this.dispose();
            }
        });
        
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent windowEvent) {
                DialogEmailConfigPrevious.this.result = null;
            }
        });
    }

    /**
     * Return all existing connection settings from preferences
     * 
     * @return
     * @throws IOException 
     * @throws ClassNotFoundException 
     */
    public static ConnectionIMAPSettings[] getConnectionSettingFromPreferences() {
        // Prepare
        Preferences userPreferences = Preferences.userRoot().node(DialogEmailConfigPrevious.class.getPackage().getName());
        HashMapStringConnectionIMAPSettings connectionSettingsMap;
        
        // Check
        if (userPreferences.getByteArray(Resources.CONNECTION_SETTINGS_MAP, null) == null){
            return null;
        }
        
        // Read from preferences
        ByteArrayInputStream in = new ByteArrayInputStream(userPreferences.getByteArray(Resources.CONNECTION_SETTINGS_MAP, null));
        Object o;
        try {
            o = new ObjectInputStream(in).readObject();
            if (!(o instanceof HashMapStringConnectionIMAPSettings)) {
                throw new IllegalArgumentException("HashMap of connections settings is not an instance of expected object");
            }
            connectionSettingsMap = (HashMapStringConnectionIMAPSettings) o;

            // Return
            return connectionSettingsMap.values().toArray(new ConnectionIMAPSettings[connectionSettingsMap.size()]);

        } catch (ClassNotFoundException | IOException e) {
            // If something goes wrong just return
            return null;
        }
    }

    /**
     * Shows this dialog
     */
    public ConnectionIMAPSettings showDialog(){        
        this.setModal(true);
        this.setVisible(true);
        return this.result;
    }
    
    /**
     * Action close
     */
    private void actionClose() {
            this.result = list.getSelectedValue();
            DialogEmailConfigPrevious.this.dispose();
    }
}
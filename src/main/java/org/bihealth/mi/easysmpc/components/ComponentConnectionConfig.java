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
import java.io.IOException;
import java.util.prefs.BackingStoreException;

import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.ListSelectionModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.bihealth.mi.easybus.ConnectionSettings;
import org.bihealth.mi.easysmpc.resources.Connections;
import org.bihealth.mi.easysmpc.resources.Resources;

/**
 * A component for the connection settings config
 * 
 * @author Felix Wirth
 *
 */
public abstract class ComponentConnectionConfig extends JPanel {
    
    /** SVUID */
    private static final long                          serialVersionUID = 1455265049057046097L;
    /** Config list */
    private final JList<ConnectionSettings>            configList;
    /** List data model */
    private final DefaultListModel<ConnectionSettings> configListModel  = new DefaultListModel<>();
    /** Change config is allowed? */
    private final boolean                              changeConfigAllowed;
    /** Parent */
    private final JDialog                              parent;
    /** Set change listener */
    private ChangeListener                             listener;

    /**
     * Creates a new instance
     * @param parent
     * @param settings
     * @param changeConfigAllowed
     */
    public ComponentConnectionConfig(JDialog parent, ConnectionSettings settings, boolean changeConfigAllowed) {
       // Super
        super();
       
       // Store
       this.parent = parent;
       this.changeConfigAllowed = changeConfigAllowed;
       
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
       
       // Add
       this.add(configList, BorderLayout.WEST);
       
       // Change state and set settings
       updateList();
       configList.setSelectedValue(settings, true);
       this.configList.setEnabled(changeConfigAllowed);
    }
    
    /**
     * Reacts on changes
     * @param e
     */
    public void stateChanged(ChangeEvent e) {
        // Display currently selected value
        if (e.getSource() == configList && changeConfigAllowed) {
            displaySettings(configList.getSelectedValue());
        }
        
        if (getChangeListener() != null) {
            getChangeListener().stateChanged(e);
        }
    }

    /**
     * Creates a new editable instance with no settings selected
     * @param parent
     */
    public ComponentConnectionConfig(JDialog parent){
        this(parent, null, true);
    }
    
    /**
     * Is proceeding possible?
     * @return
     */
    public abstract boolean isProceedPossible();
    
    /**
     * Is delete possible?
     * @return
     */
    public abstract boolean isAddPossible();
    
    /**
     * Returns the configured connection
     * @return
     */
    public abstract ConnectionSettings getConnectionSettings();
    
    /**
     * Displays config settings
     * @param settings
     */
    public abstract void displaySettings(ConnectionSettings settings);
    
    /**
     * Returns the class type of the settings clas
     * @return
     */
    public abstract Class<?> getSettingsClass();

    /**
     * Update list to select config
     */
    public void updateList() {
        ConnectionSettings currentSetting = configList.getSelectedValue();
        configListModel.removeAllElements();
    
        try {
            for (ConnectionSettings settings : Connections.list(getSettingsClass())) {
                configListModel.addElement(settings);
    
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

    /** Is removing an item possible?
     * @return
     */
    public boolean isRemovePossible() {
        return configList.getSelectedValue() != null;
    }

    /**
     * Add a new entry 
     */
    public void actionAdd() {
        displaySettings(null);
        configList.setSelectedValue(null, true);
    }

    /**
     * Removes a config
     */
    public void actionRemove() {
        try {
            Connections.remove(configList.getSelectedValue() );
        } catch (BackingStoreException e) {
            JOptionPane.showMessageDialog(this.parent, Resources.getString("PerspectiveCreate.ErrorDeletePreferences"), Resources.getString("PerspectiveCreate.Error"), JOptionPane.ERROR_MESSAGE);
        }
        updateList();
        displaySettings(null);
    }
    
    /**
     * Returns the parent
     */
    public JDialog getParentDialog() {
        return this.parent;
    }
    
    /**
     * Sets the change listener 
     * @param listener
     */
    public void setChangeListener(ChangeListener listener) {
        this.listener = listener;
    }
    
    /**
     * @return the change listener
     */
    protected ChangeListener getChangeListener() {
        return this.listener;
    }
}

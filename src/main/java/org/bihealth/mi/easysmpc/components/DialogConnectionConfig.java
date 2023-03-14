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

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.KeyStroke;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.bihealth.mi.easybus.ConnectionSettings;
import org.bihealth.mi.easysmpc.resources.Connections;
import org.bihealth.mi.easysmpc.resources.Resources;

/**
 * Dialog for entering details of a e-mail box
 * 
 * @author Felix Wirth
 */
public class DialogConnectionConfig extends JDialog implements ChangeListener {
    
    /**
     * Tabbed pane for connection config
     * 
     * @author Felix Wirth
     *
     */
    public static class JTabbedPaneConnectionConfig extends JTabbedPane {
        
        /** SVUID */
        private static final long serialVersionUID = -2007743049110040581L;

        /**
         * Creates a new instance 
         */
        JTabbedPaneConnectionConfig(){
            super();
        }
        
        public ComponentConnectionConfig getCurrentSelectedComponent() {
            return (ComponentConnectionConfig) getSelectedComponent();
        }
    };
    
    /** SVUID */
    private static final long           serialVersionUID = -5892937473681272650L;
    /** Button */
    private JButton                     buttonOK;
    /** Button */
    private JButton                     buttonAdd;
    /** Button */
    private JButton                     buttonRemove;
    /** Result */
    private ConnectionSettings          result;
    /** Parent frame */
    private JFrame                      parent;
    /** Central panel */
    private JPanel                      center;
    /** Tabbed pane */
    private JTabbedPaneConnectionConfig tabbedPane       = new JTabbedPaneConnectionConfig();

    /**
     * Create a new instance
     * 
     * @param parent Component to set the location of JDialog relative to
     */
    public DialogConnectionConfig(JFrame parent) {
        // Dialog properties
        this.parent = parent;
        this.setTitle(Resources.getString("EmailConfig.0"));
        this.getContentPane().setLayout(new BorderLayout());
        this.setIconImage(this.parent.getIconImage());
        this.setResizable(false);
        
        // Base settings panes
        center = new JPanel();
        center.setLayout(new BorderLayout());
        center.add(tabbedPane, BorderLayout.CENTER);
        
        // Tabbed pane
        tabbedPane.addChangeListener(this);
        EntryConnectionConfigEmail emailTab = new EntryConnectionConfigEmail(this);
        emailTab.setChangeListener(this);
        tabbedPane.add(new EntryConnectionConfigManual(), Resources.getString("ConnectionConfig.0"));
        tabbedPane.add(emailTab, Resources.getString("ConnectionConfig.4"));
        
        
        // Buttons
        JPanel buttonsPane = new JPanel();
        buttonsPane.setLayout(new GridLayout(1, 4));
        this.buttonOK = new JButton(Resources.getString("ConnectionConfig.1"));
        this.buttonAdd = new JButton(Resources.getString("ConnectionConfig.2"));
        this.buttonRemove = new JButton(Resources.getString("ConnectionConfig.3"));
        JButton buttonCancel = new JButton(Resources.getString("EmailConfig.7"));

        // Add
        buttonsPane.add(buttonCancel);
        buttonsPane.add(buttonAdd);
        buttonsPane.add(buttonRemove);
        buttonsPane.add(buttonOK);
        getContentPane().add(center, BorderLayout.CENTER);
        getContentPane().add(buttonsPane, BorderLayout.SOUTH);

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
        
        this.buttonAdd.addActionListener(new ActionListener() {
            
            @Override
            public void actionPerformed(ActionEvent e) {
                tabbedPane.getCurrentSelectedComponent().actionAdd();
            }
        });
        
        this.buttonRemove.addActionListener(new ActionListener() {
            
            @Override
            public void actionPerformed(ActionEvent e) {
                tabbedPane.getCurrentSelectedComponent().actionRemove();
            }
        });

        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent windowEvent) {
                DialogConnectionConfig.this.result = null;
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

        this.stateChanged(new ChangeEvent(this));
    }

    /**
     * Show this dialog
     */
    public ConnectionSettings showDialog() {
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
        
        // Check buttons enabled
        if (this.buttonOK != null) {
            this.buttonOK.setEnabled(areValuesValid());
        }
        
        if(this.buttonAdd != null) {
            this.buttonAdd.setEnabled(tabbedPane.getCurrentSelectedComponent().isAddPossible());
        }
        
        if(this.buttonRemove != null) {
            this.buttonRemove.setEnabled(tabbedPane.getCurrentSelectedComponent().isRemovePossible());
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
        
        // Get config
        ConnectionSettings settings = tabbedPane.getCurrentSelectedComponent().getConnectionSettings();
        // Test config
        if (!settings.isValid(false)) {
            JOptionPane.showMessageDialog(this, Resources.getString("EmailConfig.14"), Resources.getString("EmailConfig.12"), JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Update connections in preferences
        try {
            Connections.addOrUpdate(settings);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, Resources.getString("PerspectiveCreate.ErrorStorePreferences"), Resources.getString("PerspectiveCreate.Error"), JOptionPane.ERROR_MESSAGE);
        }

        // Set result and dispose
        this.result = settings;
        this.dispose();
    }

    /**
     * Checks string for validity
     * 
     * @return
     */
    private boolean areValuesValid() {
        return  tabbedPane.getCurrentSelectedComponent().isProceedPossible();
    }
}

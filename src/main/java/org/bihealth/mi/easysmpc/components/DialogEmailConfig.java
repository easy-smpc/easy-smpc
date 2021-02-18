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

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.bihealth.mi.easysmpc.dataexport.EmailConnection;
import org.bihealth.mi.easysmpc.resources.Resources;

// TODO One might consider to create an abstract ComponentDialog class for the dialogs
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
    private EntryServers imapPopServerEntry;
    /** Button*/
    private JButton buttonCheckConnection;
    /** Button*/
    private JButton buttonOK;
    /** Result */
    private EmailConnection result;
        
    /**
     * Create a new instance
     * @param parent Component to set the location of JDialog relative to
     * @param additionalAction  Action which will be performed when clicking the okButton
     */
    public DialogEmailConfig(JFrame parent) {

        // Dialog properties
        this.setSize(Resources.SIZE_DIALOG_SMALL_X, Resources.SIZE_DIALOG_SMALL_Y);
        this.setLocationRelativeTo(parent);
        this.setTitle(Resources.getString("EmailConfig.0"));
        this.getContentPane().setLayout(new BorderLayout());
        this.setIconImage(parent.getIconImage());
        
        // Title
        ((JComponent) this.getContentPane()).setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED),
                                                                                        Resources.getString("EmailConfig.0"),
                                                                                        TitledBorder.CENTER,
                                                                                        TitledBorder.DEFAULT_POSITION));
        
        // Entry boxes
        JPanel central = new JPanel();
        central.setLayout(new GridLayout(2, 1));
        this.emailPasswordEntry = new EntryEMailPassword();
        this.emailPasswordEntry.setChangeListener(this);
        this.add(emailPasswordEntry);
        this.imapPopServerEntry = new EntryServers();
        this.imapPopServerEntry.setChangeListener(this);
        // Add
        central.add(emailPasswordEntry);
        central.add(imapPopServerEntry);
        this.getContentPane().add(central, BorderLayout.CENTER);        
        
        // Buttons        
        JPanel buttonsPane = new JPanel();
        buttonsPane.setLayout(new GridLayout(2, 1));      
        JPanel okCancelPane = new JPanel();
        okCancelPane.setLayout(new GridLayout(1, 2));   
        this.buttonCheckConnection = new JButton(Resources.getString("EmailConfig.5"));
        this.buttonCheckConnection.setEnabled(this.areValuesValid());
        this.buttonOK = new JButton(Resources.getString("EmailConfig.6"));
        this.buttonOK.setEnabled(this.areValuesValid());
        JButton buttonCancel = new JButton(Resources.getString("EmailConfig.7"));
        // Add
        buttonsPane.add(buttonCheckConnection);
        okCancelPane.add(buttonCancel);
        okCancelPane.add(buttonOK);
        buttonsPane.add(okCancelPane);
        getContentPane().add(buttonsPane, BorderLayout.SOUTH);
        
        // Listeners
        this.buttonCheckConnection.addActionListener(new ActionListener() {            
            @Override
            public void actionPerformed(ActionEvent e) {
                boolean connectionCheckSuccessful = true; //TODO integrate easybus
                buttonOK.setEnabled(connectionCheckSuccessful);
            }
        });
        
        this.buttonOK.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                DialogEmailConfig.this.result = new EmailConnection(imapPopServerEntry.getLeftValue(), imapPopServerEntry.getRightValue(), emailPasswordEntry.getLeftValue(), emailPasswordEntry.getRightValue());
                DialogEmailConfig.this.dispose();
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
    public EmailConnection showDialog(){        
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
    }
      
    /**
     * Checks string for validity
     * @return
     */
    private boolean areValuesValid() {
        return this.emailPasswordEntry.areValuesValid() &&
               this.imapPopServerEntry.areValuesValid();
    } 
}
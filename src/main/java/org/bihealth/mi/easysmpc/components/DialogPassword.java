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
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.bihealth.mi.easysmpc.resources.Resources;

/**
 * Dialog for entering a string
 * 
 * @author Felix Wirth
 * @author Fabian Prasser
 */
public class DialogPassword extends JDialog implements ChangeListener {


    /** SVUID */
    private static final long serialVersionUID = 2844321619003751907L;
    /** Component to enter string */
    private ComponentEntryOne passwordEntry;
    /** Result */
    private String            result;
    /** Button*/
    private JButton           buttonOK;
    /** Parent */
    private JFrame parent;
        
    /**
     * Create a new instance
     * 
     * @param textDefault
     * @param validator
     * @param parent
     */
    public DialogPassword(JFrame parent) {
        
        // Store
        this.parent = parent;

        // Dialog properties        
        this.setTitle(Resources.getString("EmailConfig.24"));
        this.getContentPane().setLayout(new BorderLayout());
        this.setIconImage(parent.getIconImage());
        this.setResizable(false);
        
        // Title
        ((JComponent) this.getContentPane()).setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED),
                                                                                        Resources.getString("EmailConfig.24"),
                                                                                        TitledBorder.CENTER,
                                                                                        TitledBorder.DEFAULT_POSITION));
        
        // Password disclaimer 
        this.add(new JLabel(Resources.getString("EmailConfig.25")), BorderLayout.NORTH);
        
        // Password entry
        this.passwordEntry = new ComponentEntryOne(Resources.getString("EmailConfig.2"),
                                                   "",
                                                   true,
                                                   new ComponentTextFieldValidator() {
                                                       @Override
                                                       public boolean validate(String text) {
                                                           if (text == null ||
                                                               text.equals("")) { return false; }
                                                           return true;
                                                       }
                                                   },
                                                   true,
                                                   false);
        this.add(this.passwordEntry, BorderLayout.CENTER);
        passwordEntry.setChangeListener(this);   
        
        
        // Buttons 
        JPanel buttonsPane = new JPanel();
        buttonsPane.setLayout(new GridLayout(1, 2));
        this.buttonOK = new JButton(Resources.getString("PerspectiveParticipate.ok"));
        JButton buttonCancel = new JButton(Resources.getString("EmailConfig.7"));
        buttonsPane.add(buttonCancel);
        buttonsPane.add(buttonOK);
        this.add(buttonsPane, BorderLayout.SOUTH);
        this.stateChanged(new ChangeEvent(this));
        
        // Listeners
        this.buttonOK.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                DialogPassword.this.result = passwordEntry.getValue();
                DialogPassword.this.dispose();
            }
        });
        buttonCancel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                DialogPassword.this.result = null;
                DialogPassword.this.dispose();
            }
        });
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent windowEvent) {
                DialogPassword.this.result = null;
            }
        });
        
        // Pack and set location
        this.pack();
        this.setLocationRelativeTo(parent);
    }

    /**
     * Checks string for validity
     * @return
     */
    private boolean areValuesValid() {
        return this.passwordEntry.isValueValid();
    }
    
    /**
     * Show this dialog
     */
    public String showDialog(){
        this.setModal(true);
        this.setLocationRelativeTo(this.parent);
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
}
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
package de.tu_darmstadt.cbs.app.components;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import de.tu_darmstadt.cbs.app.resources.Resources;

/**
 * Dialog for entering a string
 * 
 * @author Felix Wirth
 * @author Fabian Prasser
 */
public class DialogStringPicker extends JDialog implements ChangeListener {

    /** SVID */
    private static final long serialVersionUID = -2803385597185044215L;
    /** Component to enter string */
    private ComponentTextArea text;
    /** Result */
    private String            result;
    /** Button*/
    private JButton           buttonOK;
        
    /**
     * Create a new instance
     * @param parent Component to set the location of JDialog relative to
     * @param additionalAction  Action which will be performed when clicking the okButton
     */
    public DialogStringPicker(ComponentTextFieldValidator validator, Component parent) {

        // Dialog properties
        this.setSize(Resources.SIZE_TEXTAREA_X, Resources.SIZE_TEXTAREA_Y);
        this.setLocationRelativeTo(parent);
        this.setTitle(Resources.getString("PerspectiveParticipate.PickerTitle"));
        this.getContentPane().setLayout(new BorderLayout());
        this.setIconImage(((JFrame)SwingUtilities.getWindowAncestor(parent)).getIconImage());
        
        // Text
        this.text = new ComponentTextArea(validator);
        JLabel pickerText = new JLabel(Resources.getString("PerspectiveParticipate.PickerText"));
        this.add(pickerText, BorderLayout.NORTH);
        this.text.setChangeListener(this);
        this.add(text, BorderLayout.CENTER);
        
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
                DialogStringPicker.this.result = text.getText();
                DialogStringPicker.this.dispose();
            }
        });
        buttonCancel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                DialogStringPicker.this.result = null;
                DialogStringPicker.this.dispose();
            }
        });
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent windowEvent) {
                DialogStringPicker.this.result = null;
            }
        });
    }

    /**
     * Reacts to changes
     */
    @Override
    public void stateChanged(ChangeEvent e) {
        this.buttonOK.setEnabled(this.areValuesValid());
    }
    
    /**
     * Show this dialog
     */
    public String showDialog(){        
        this.setModal(true);
        this.setVisible(true);
        return this.result;
    }
      
    /**
     * Checks string for validity
     * @return
     */
    private boolean areValuesValid() {
        return this.text.isValueValid();
    } 
}
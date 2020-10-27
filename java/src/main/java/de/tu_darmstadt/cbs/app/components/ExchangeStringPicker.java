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
 * Component to enter exchangeString
 * 
 * @author Felix Wirth
 *
 */
public class ExchangeStringPicker extends JDialog implements ChangeListener {

    /** SVID */
    private static final long serialVersionUID = -2803385597185044215L;
    /** Component to enter exchangeString */
    private ComponentTextArea text;
    /** okButton */
    private JButton           button;
    /** Result */
    private String            result;
        
    /**
     * Create a new instance
     * @param parent Component to set the location of JDialog relative to
     * @param additionalAction  Action which will be performed when clicking the okButton
     */
    public ExchangeStringPicker(ComponentTextFieldValidator validator, Component parent) {
        super();
        this.text = new ComponentTextArea(validator);
        this.setSize(Resources.SIZE_TEXTAREA_X, Resources.SIZE_TEXTAREA_Y);
        this.setLocationRelativeTo(parent);
        this.setTitle(Resources.getString("PerspectiveParticipate.PickerTitle"));
        this.getContentPane().setLayout(new BorderLayout());
        this.setIconImage(((JFrame)SwingUtilities.getWindowAncestor(parent)).getIconImage());
        JLabel pickerText = new JLabel(Resources.getString("PerspectiveParticipate.PickerText"));
        this.getContentPane().add(pickerText, BorderLayout.NORTH);
        this.text.setChangeListener(this);
        this.getContentPane().add(text, BorderLayout.CENTER);
        JPanel buttonsPane = new JPanel();
        buttonsPane.setLayout(new GridLayout(1, 2));
        this.getContentPane().add(buttonsPane, BorderLayout.SOUTH);
        button = new JButton(Resources.getString("PerspectiveParticipate.ok"));
        button.setEnabled(this.areValuesValid());
        JButton cancelButton = new JButton(Resources.getString("PerspectiveParticipate.cancel"));
        buttonsPane.add(cancelButton);
        buttonsPane.add(button);
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ExchangeStringPicker.this.result = text.getText();
                ExchangeStringPicker.this.dispose();
            }
        });
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ExchangeStringPicker.this.result = null;
                ExchangeStringPicker.this.dispose();
            }
        });
        // Set value also when closed by cross in upper right corner
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                ExchangeStringPicker.this.result = null;
            }
        });
    }

    /**
     * Reacts to changes
     */
    @Override
    public void stateChanged(ChangeEvent e) {
        this.button.setEnabled(this.areValuesValid());
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
     * Checks exchangeString for validity
     * @return
     */
    private boolean areValuesValid() {
        return this.text.isValueValid();
    } 
}
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
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import de.tu_darmstadt.cbs.app.Resources;

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
    private ComponentTextArea exchangeStringTextArea;
    /** okButton*/
    private JButton okButton;
    /** Result of dialog */
    private boolean result=true;
        
    /**
     * Create a new instance
     * @param componentRelativePosition Component to set the location of JDialog relative to
     * @param additionalAction  Action which will be performed when clicking the okButton
     */
    public ExchangeStringPicker(ComponentTextFieldValidator textAreaValidator , Component componentRelativePosition) {
        super();
        this.exchangeStringTextArea = new ComponentTextArea(textAreaValidator);
        this.setSize(Resources.SIZE_TEXTAREA_X,Resources.SIZE_TEXTAREA_Y); 
        this.setLocationRelativeTo(componentRelativePosition);                       
        this.setTitle(Resources.getString("PerspectiveParticipate.PickerTitle"));
        this.getContentPane().setLayout(new BorderLayout());
        JLabel pickerText = new JLabel(Resources.getString("PerspectiveParticipate.PickerText"));
        this.getContentPane().add(pickerText, BorderLayout.NORTH);                                                                     
        this.exchangeStringTextArea.setChangeListener(this);
        this.getContentPane().add(exchangeStringTextArea, BorderLayout.CENTER);
        JPanel buttonsPane = new JPanel();
        buttonsPane.setLayout(new GridLayout(1, 2));
        this.getContentPane().add(buttonsPane, BorderLayout.SOUTH);
        okButton = new JButton(Resources.getString("PerspectiveParticipate.ok"));
        okButton.setEnabled(this.areValuesValid());
        JButton cancelButton = new JButton(Resources.getString("PerspectiveParticipate.cancel"));
        buttonsPane.add(cancelButton);
        buttonsPane.add(okButton);        
        okButton.addActionListener(new ActionListener() {            
            @Override
            public void actionPerformed(ActionEvent e) {
                ExchangeStringPicker.this.result=true;
                ExchangeStringPicker.this.dispose();           
            }
        });
        cancelButton.addActionListener(new ActionListener() {            
            @Override
            public void actionPerformed(ActionEvent e) {
                ExchangeStringPicker.this.result=false;
                ExchangeStringPicker.this.dispose();           
            }
        });
        // Set value also when closed by cross in upper right corner
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                ExchangeStringPicker.this.result=false;
            }
        });
    }

    /**
     * Reacts to changes
     */
    @Override
    public void stateChanged(ChangeEvent e) {
        this.okButton.setEnabled(this.areValuesValid());
    }
    
    /**
     * Show this dialog
     */
    public boolean showDialog(){        
        this.setModal(true);
        this.setVisible(true);
        return this.result;
    }
    
    
    /**
     * Checks exchangeString for validity
     * @return
     */
    private boolean areValuesValid() {
        return this.exchangeStringTextArea.isValueValid();
    } 
    
}
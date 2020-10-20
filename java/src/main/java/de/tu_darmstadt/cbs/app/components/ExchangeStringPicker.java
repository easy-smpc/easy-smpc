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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import de.tu_darmstadt.cbs.app.Resources;
import de.tu_darmstadt.cbs.app.SMPCServices;

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
    ComponentTextArea exchangeStringTextArea;
    /** okButton*/
    JButton okButton;
    /** Validates entry in  exchangeString*/
    ComponentTextFieldValidator validator;
    /** Additional action for okButton */
    private ComponentAdditionalAction additionalAction;

    

    /**
     * Create a new instance
     * @param componentRelativePosition Component to set the location of JDialog relative to
     * @param additionalAction  Action which will be performed when clicking the okButton
     */
    public ExchangeStringPicker(Component componentRelativePosition,
                                ComponentAdditionalAction additionalAction) {
        super();
        this.setSize(Resources.SIZE_TEXTAREA_X,Resources.SIZE_TEXTAREA_Y);        
        this.setModal(true);
        this.setLocationRelativeTo(componentRelativePosition);
        this.getContentPane().setLayout(new BorderLayout());
        this.additionalAction = additionalAction;
        // -------
        // Textarena exchangeString
        // -------  
        this.exchangeStringTextArea = new ComponentTextArea (new ComponentTextFieldValidator() {          
            @Override
            public boolean validate(String text) {
                try {
                    SMPCServices.getServicesSMPC().initalizeAsNewStudyParticipation(text);
                    return true;
                    }
            catch (IllegalArgumentException e) {
                return false;
                    }
                }
            });     
        this.exchangeStringTextArea.setChangeListener(this);     
        // -------
        // Ok Button
        // -------        
        this.getContentPane().add(exchangeStringTextArea, BorderLayout.CENTER);
        okButton = new JButton(Resources.getString("PerspectiveParticipate.ok"));
        okButton.addActionListener(new ActionListener() {            
            @Override
            public void actionPerformed(ActionEvent e) {
                ExchangeStringPicker.this.additionalAction.additionalAction();
                ExchangeStringPicker.this.dispose();
            }
        });
        this.getContentPane().add(okButton, BorderLayout.SOUTH);
        this.setVisible(true);
    }

    /**
     * Reacts to changes
     */
    @Override
    public void stateChanged(ChangeEvent e) {
        this.okButton.setEnabled(this.areValuesValid());
    }
    
    /**
     * Checks exchangeString for validity
     * @return
     */
    private boolean areValuesValid() {
        return this.exchangeStringTextArea.isValueValid();
    } 
    
}
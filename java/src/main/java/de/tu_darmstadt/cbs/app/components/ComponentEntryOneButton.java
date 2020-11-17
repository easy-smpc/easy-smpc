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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JPanel;

import de.tu_darmstadt.cbs.emailsmpc.Participant;

/**
 * Display participants with one button only
 * 
 * @author Felix Wirth
 *
 */

public abstract class ComponentEntryOneButton extends ComponentEntry {

    /** SVID */
    private static final long serialVersionUID = -2630607826943052651L;

    /** Remove */
    protected JButton           button;
    
    /** Change listener */
    private ActionListener    listener;


    /**
     * Creates a new instance
     * @param leftString
     * @param leftValue
     * @param rightString
     * @param rightValue
     * @param additionalControlsEnabled
     */
    public ComponentEntryOneButton(String leftString,
                                   String leftValue,
                                   String rightString,
                                   String rightValue,
                                   boolean additionalControlsEnabled) {
        super(leftString, //$NON-NLS-1$
              leftValue,
              false,
              new ComponentTextFieldValidator() {
                @Override
                public boolean validate(String text) {
                    // TODO: Must ensure that no two bins have the same name
                    return !text.trim().isEmpty();
                }
              },
              rightString, //$NON-NLS-1$
              rightValue,
              false,
              new ComponentTextFieldValidator() {
                  @Override
                  public boolean validate(String text) {
                      return Participant.validEmail(text);
                  }
                },
              additionalControlsEnabled
              );        
    }
    

    /**
     * Creates an additional control panel
     */
    @Override
    protected JPanel createAdditionalControls() {

        // Panels
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        
        // Buttons
        this.button = new JButton(this.getText());
        panel.add(button);
        
        // Listeners
        this.button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                buttonAction();
            }
        });
        
        // Done
        return panel;
    }

    /**
     * Button action
     */
    private void buttonAction() {
        if (listener != null) {
            listener.actionPerformed(new ActionEvent(this, 0, null));
        }
    }

    /**
     * Sets a change listener
     * @param listener
     */
    public void setButtonListener(ActionListener listener) {
        this.listener = listener;
    }
    
    /**
     * Implement this to return the text for the button
     * @return
     */
    protected abstract String getText();
    
}

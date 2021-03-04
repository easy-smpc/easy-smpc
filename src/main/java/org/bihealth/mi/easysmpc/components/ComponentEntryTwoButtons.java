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

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JPanel;

import de.tu_darmstadt.cbs.emailsmpc.Participant;

/**
 * Display participants with two buttons
 * 
 * @author Armin Müller
 */
public abstract class ComponentEntryTwoButtons extends ComponentEntry {

	/** SVID */
	private static final long serialVersionUID = -1068258367945180263L;

    /** Button 1 */
    protected JButton           button1;
    /** Button 2 */
    protected JButton           button2;
    
    /** Change listener for button 1*/
    private ActionListener    listener1;
    /** Change listener for button 2*/
    private ActionListener    listener2;


    /**
     * Creates a new instance
     * @param leftString
     * @param leftValue
     * @param rightString
     * @param rightValue
     * @param additionalControlsEnabled
     */
    public ComponentEntryTwoButtons(String leftString,
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
     * Sets a change listener for the first button
     * @param listener
     */
    public void setButton1Listener(ActionListener listener) {
        this.listener1 = listener;
    }
    
    /**
     * Sets a change listener for the second button
     * @param listener
     */
    public void setButton2Listener(ActionListener listener) {
        this.listener2 = listener;
    }

    /**
     * First button action
     */
    private void button1Action() {
        if (listener1 != null) {
            listener1.actionPerformed(new ActionEvent(this, 0, null));
        }
    }
    
    /**
     * Second button action
     */
    private void button2Action() {
        if (listener2 != null) {
            listener2.actionPerformed(new ActionEvent(this, 0, null));
        }
    }

    /**
     * Creates an additional control panel
     */
    @Override
    protected JPanel createAdditionalControls() {

        // Panels
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(1, 2));
        
        // Buttons
        this.button1 = new JButton(this.getButton1Text());
        panel.add(button1);
        
        this.button2 = new JButton(this.getButton2Text());
        panel.add(button2);
        
        // Listeners
        this.button1.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                button1Action();
            }
        });
        
        this.button2.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                button2Action();
            }
        });
        
        // Done
        return panel;
    }
    
    /**
     * Implement this to return the text for the first button
     * @return
     */
    protected abstract String getButton1Text();
    
    /**
     * Implement this to return the text for the second button
     * @return
     */
    protected abstract String getButton2Text();
    
}

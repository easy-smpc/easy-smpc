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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JPanel;

import org.bihealth.mi.easysmpc.resources.Resources;

/**
 * Display participants for sending mail only
 * 
 * @author Felix Wirth
 *
 */
public class EntryParticipantCheckmarkSendMail extends EntryParticipantCheckmark {

    /** SVID */
    private static final long serialVersionUID = 3050897911405040557L;
    
    /** Remove */
    protected JButton           button;
    
    /** Change listener */
    private ActionListener    listener;

    /**
     * Creates a new instance
     * @param name
     * @param email
     * @param isownparticipant
     */
    public EntryParticipantCheckmarkSendMail(String name, String email, boolean isOwnParticipant) {
        super(name,
              email,
              isOwnParticipant);

        // Deactivate button if own participant
        this.button.setEnabled(!isOwnParticipant);
    }
    
    /**
     * Creates an additional control panel
     */
    @Override
    protected JPanel createAdditionalControls() {
        
        // Panels
        JPanel panelCheckmark = super.createAdditionalControls();
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        
        // Buttons and addition to panels
        this.button = new JButton(this.getText());
        panel.add(button, BorderLayout.CENTER);
        panel.add(panelCheckmark, BorderLayout.EAST);
        
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
     * Returns text for button (label)
     * @return button text
     */
    protected String getText() {
        return Resources.getString("PerspectiveSend.sendEmailButton");
    }
    
    /**
     * Sets a change listener
     * @param listener
     */
    public void setButtonListener(ActionListener listener) {
        this.listener = listener;
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
     * Disables the button
     */
    public void setButtonEnabled(boolean enabled) {
        this.button.setEnabled(enabled);
    }
}
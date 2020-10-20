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

import de.tu_darmstadt.cbs.app.Resources;
import de.tu_darmstadt.cbs.emailsmpc.Participant;

/**
 * Display participants for sending mail only
 * 
 * @author Felix Wirth
 *
 */
public class EntryParticipantSendMail extends ComponentEntry {

    /** SVID */
    private static final long serialVersionUID = 7357425826027805679L;

    /** Remove */
    private JButton           send;

    /** Change listener */
    private ActionListener    sendListener;

    /**
     * Creates a new instance
     * @param name
     * @param email
     */
    public EntryParticipantSendMail(String name, String email) {
        this(name, email, false);
    }
    
    /**
     * Creates a new instance
     * @param name
     * @param email
     * @param buttonEnabled
     */
    public EntryParticipantSendMail(String name, String email, boolean buttonEnabled) {
        super(Resources.getString("Participant.0"), //$NON-NLS-1$
              name,
              buttonEnabled,
              new ComponentTextFieldValidator() {
                @Override
                public boolean validate(String text) {
                    // TODO: Must ensure that no two bins have the same name
                    return !text.trim().isEmpty();
                }
              },
              Resources.getString("Participant.1"), //$NON-NLS-1$
              email,
              buttonEnabled,
              new ComponentTextFieldValidator() {
                  @Override
                  public boolean validate(String text) {
                      return Participant.validEmail(text);
                  }
                },
              buttonEnabled);
    }
    

    @Override
    protected JPanel createAdditionalControls() {

        // Panels
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        
        // Buttons
        this.send = new JButton(Resources.getString("PerspectiveSend.sendEmailButton"));
        panel.add(send);
        
        // Listeners
        this.send.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                send();
            }
        });
        
        // Done
        return panel;
    }

    /**
     * Send action
     */
    private void send() {
        if (sendListener != null) {
            sendListener.actionPerformed(new ActionEvent(this, 0, null));
        }
    }

    /**
     * Sets a change listener
     * @param listener
     */
    public void setSendListener(ActionListener listener) {
        this.sendListener = listener;
    }
}

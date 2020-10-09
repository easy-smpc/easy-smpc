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
package de.tu_darmstadt.cbs.app.templates;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import de.tu_darmstadt.cbs.app.Resources;
import de.tu_darmstadt.cbs.app.SMPCServices;
import de.tu_darmstadt.cbs.emailsmpc.Participant;
import lombok.Getter;

/**
 * @author Felix Wirth
 *
 */

public class NameEmailParticipantEntry extends JPanel {
    /** SVUID */
    private static final long serialVersionUID = -6907197312982454382L;
    /** Text field to enter the participant's name */
    /**
     * Returns participantTextField
     * @return
     */
    @Getter
    private JTextField   participantTextField;
    /** Text field to enter the participant's e-mail */
    /** Get emailTextField
     * @return
     */
    @Getter
    private JTextField   emailTextField;
    /** Radio button indicating which participants number the current user has (no relevance for study creator) */
    /**
     * Returns isCurrentParticipantRadioButton
     * @return
     */
    @Getter
    private JRadioButton isCurrentParticipantRadioButton;
    /** Listener to check for changes in the text fields for validation*/
    DocumentListener textfieldsChangeDocumentListener = new DocumentListener() {
        
        @Override
        public void removeUpdate(DocumentEvent e) {
            validateEnteredData();
        }
        @Override
        public void insertUpdate(DocumentEvent e) {
            validateEnteredData();
        }
        @Override
        public void changedUpdate(DocumentEvent e) {
            validateEnteredData();
        }
    };
      

    /**
     * Creates a new instances
     */
    public NameEmailParticipantEntry() {
        this.setLayout(new GridLayout(0, 6, 0, 0));
        this.setMaximumSize(new Dimension(Integer.MAX_VALUE, Resources.ROW_HEIGHT));
        JLabel participantLabel = new JLabel(Resources.getString("Participant.0")); //$NON-NLS-1$
        this.add(participantLabel);
        participantTextField = new JTextField();
        this.getParticipantTextField().getDocument().addDocumentListener(textfieldsChangeDocumentListener);
        this.add(participantTextField);
        JPanel emptyPanel = new JPanel();
        this.add(emptyPanel);
        JLabel emailLabel = new JLabel(Resources.getString("Participant.1")); //$NON-NLS-1$  
        this.add(emailLabel);
        this.emailTextField = new JTextField();
        this.getEmailTextField().getDocument().addDocumentListener(textfieldsChangeDocumentListener);
        this.add(emailTextField);
        this.isCurrentParticipantRadioButton = new JRadioButton("", false); //$NON-NLS-1$
        this.add(isCurrentParticipantRadioButton);
        this.isCurrentParticipantRadioButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // If current participant is set, unselect radio button for all
                // others
                if (isCurrentParticipantRadioButton.isSelected()) {
                    NameEmailParticipantEntry.this.getParent().getComponents();
                    for (int i = 1; i < NameEmailParticipantEntry.this.getParent()
                                                                      .getComponentCount(); i++) {
                        NameEmailParticipantEntry currentNameEmailParticipant = (NameEmailParticipantEntry) NameEmailParticipantEntry.this.getParent()
                                                                                                                                          .getComponent(i);
                        if (currentNameEmailParticipant.equals(NameEmailParticipantEntry.this)) {
                            NameEmailParticipantEntry.this.isCurrentParticipantRadioButton.setSelected(true);
                            NameEmailParticipantEntry.this.isCurrentParticipantRadioButton.setText(Resources.getString("Participant.2")); //$NON-NLS-1$
                            SMPCServices.getServicesSMPC().getAppModel().numberOwnPartcipation = i;
                        } else {
                            currentNameEmailParticipant.getIsCurrentParticipantRadioButton()
                                                       .setSelected(false);
                            currentNameEmailParticipant.getIsCurrentParticipantRadioButton()
                                                       .setText(""); //$NON-NLS-1$
                        }
                    }

                }
            }
        });
    }

    /**
     * Creates a new instances with pre-filled values
     * @param name Pre-filled name of participant
     * @param email Pre-filled e-mail of participant
     * @param enabled Indicates whether name and e-mail are editable 
     */
    public NameEmailParticipantEntry(String name, String email, boolean enabled) {
        this();
        this.participantTextField.setText(name);
        this.emailTextField.setText(email);
        this.participantTextField.setEnabled(enabled);
        this.emailTextField.setEnabled(enabled);
        // Set isCurrentParticipantRadioButton only when the rest is disabled
        this.isCurrentParticipantRadioButton.setVisible(!enabled);
    }
   
    
    /**
     * Validates entered data
     * @return
     */
    public boolean validateEnteredData() {
        boolean dataValid = true;
        //validate E-Mail
        if (!Participant.validEmail(this.getEmailTextField().getText()))
        {
            dataValid = false;
            this.getEmailTextField().setBorder(BorderFactory.createLineBorder(Color.RED));
        }
        else {
            this.getEmailTextField().setBorder(BorderFactory.createEmptyBorder());
        }
        //validate Name
        if (this.getParticipantTextField().getText().isBlank() || this.getParticipantTextField().getText().isEmpty() )
        {
            dataValid = false;
            this.getParticipantTextField().setBorder(BorderFactory.createLineBorder(Color.RED));
        }
        else
        {
            this.getParticipantTextField().setBorder(BorderFactory.createEmptyBorder());
        }
        this.revalidate();
        this.repaint();
        return dataValid;
    }
}
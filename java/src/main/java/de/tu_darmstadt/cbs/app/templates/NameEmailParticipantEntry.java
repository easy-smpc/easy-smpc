/**
 * 
 */
//TODO: GPL licence
package de.tu_darmstadt.cbs.app.templates;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

import de.tu_darmstadt.cbs.app.Resources;
import de.tu_darmstadt.cbs.app.SMPCServices;
import lombok.Getter;

/**
 * @author Felix Wirth
 *
 */
public class NameEmailParticipantEntry extends JPanel {
    @Getter
    private JTextField   participantTextField;
    @Getter
    private JTextField   emailTextField;
    @Getter
    private JRadioButton isCurrentParticipantRadioButton;

    public NameEmailParticipantEntry() {
        this.setLayout(new GridLayout(0, 6, 0, 0));
        this.setMaximumSize(new Dimension(Integer.MAX_VALUE, Resources.ROW_HEIGHT));
        JLabel participantLabel = new JLabel(Resources.getString("Participant.0")); //$NON-NLS-1$
        this.add(participantLabel);
        participantTextField = new JTextField();
        this.add(participantTextField);

        JPanel emptyPanel = new JPanel();
        this.add(emptyPanel);

        JLabel emailLabel = new JLabel(Resources.getString("Participant.1")); //$NON-NLS-1$
        this.add(emailLabel);

        this.emailTextField = new JTextField();
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
                            NameEmailParticipantEntry.this.isCurrentParticipantRadioButton.setText(Resources.getString("Participant.3")); //$NON-NLS-1$
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

    public NameEmailParticipantEntry(String name, String email, boolean enabled) {
        this();
        this.participantTextField.setText(name);
        this.emailTextField.setText(email);
        this.participantTextField.setEnabled(enabled);
        this.emailTextField.setEnabled(enabled);
        // Set isCurrentParticipantRadioButton only when the rest is disabled
        this.isCurrentParticipantRadioButton.setVisible(!enabled);

    }
}

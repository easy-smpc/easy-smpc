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

import java.awt.Desktop;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.URI;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;

import de.tu_darmstadt.cbs.app.Resources;
import lombok.Getter;

/**
 * @author Felix Wirth
 *
 */
public class DataSharingDisplayEntry extends JPanel {

    /** SVUID */
    private static final long serialVersionUID = -8114494722419799605L;
    /** Prints the name of the participant */
    /**
     * returns participantNameLabel
     * @return
     */
    @Getter
    private JLabel    participantNameLabel;
    /** Prints the e-mail of the participant */
    /**
     * Returns participantEmailLabel
     * @return
     */
    @Getter
    private JLabel    participantEmailLabel;
    /** Box to share data (copy or paste string) */
    /**
     * Returns dataToShareTextArea
     * @return
     */
    @Getter
    private JTextArea dataToShareTextArea;
    /** Button to send an e-mail with the string from dataToShareTextArea*/
    private JButton   sendMailButton;

    /**
     * Creates a new instance
     */
    public DataSharingDisplayEntry() {
        this.setLayout(new GridBagLayout());
        this.participantNameLabel = new JLabel(Resources.getString("Participant.0"));
        GridBagConstraints gbc_participantLabel = new GridBagConstraints();
        gbc_participantLabel.insets = new Insets(0, 0, 5, 5);
        gbc_participantLabel.gridx = 0;
        gbc_participantLabel.gridy = 0;
        this.add(this.participantNameLabel, gbc_participantLabel);

        this.participantEmailLabel = new JLabel(Resources.getString("Participant.1"));
        this.participantEmailLabel.setHorizontalAlignment(SwingConstants.LEFT);
        GridBagConstraints gbc_emailLabel = new GridBagConstraints();
        gbc_emailLabel.gridx = 1;
        gbc_emailLabel.gridy = 0;
        this.add(this.participantEmailLabel, gbc_emailLabel);

        this.dataToShareTextArea = new JTextArea(Resources.ROW_GAP, Resources.COLOUMNS_TEXTAREA);
        GridBagConstraints gbc_dumpedDataToSendTextArea = new GridBagConstraints();
        gbc_dumpedDataToSendTextArea.gridx = 0;
        gbc_dumpedDataToSendTextArea.gridy = 1;
        gbc_dumpedDataToSendTextArea.gridwidth = 2;
        this.dataToShareTextArea.setLineWrap(true);
        this.add(this.dataToShareTextArea, gbc_dumpedDataToSendTextArea);

        this.sendMailButton = new JButton(Resources.getString("DataSharingDisplayEntry.sendEmailButtion"));
        GridBagConstraints gbc_sendMailButton = new GridBagConstraints();
        gbc_sendMailButton.gridx = 2;
        gbc_sendMailButton.gridy = 1;
        gbc_sendMailButton.anchor = GridBagConstraints.WEST;
        this.add(this.sendMailButton, gbc_sendMailButton);
    }

    /**
     * Create a new instances with pre-filled values
     * @param participantName pre-filled name of participant
     * @param participantEmail pre-filled e-mail of participant
     * @param dataToShare pre-filled string to share
     * @param enabled Indicates whether is dataToShareTextArea enabled? 
     * @param showButton Indicates whether the button to send emails (sendMailButton) is displayed
     */
    public DataSharingDisplayEntry(String participantName,
                                   String participantEmail,
                                   String dataToShare,
                                   boolean enabled,
                                   boolean showButton) {
        this();
        this.participantNameLabel.setText(participantName);
        this.participantEmailLabel.setText(participantEmail);
        this.dataToShareTextArea.setText(dataToShare);
        this.dataToShareTextArea.setEditable(enabled);
        this.sendMailButton.setVisible(showButton);
        if (showButton) {
            URI mailToURI = URI.create(String.format(Resources.getString("DataSharingDisplayEntry.mailToString"),
                                                     participantEmail,
                                                     participantName,
                                                     dataToShare));
            this.sendMailButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    try {
                        Desktop.getDesktop().mail(mailToURI);
                    } catch (IOException e1) {
                        // TODO Add error log
                        e1.printStackTrace();
                    }
                }
            });
        }
    }

}

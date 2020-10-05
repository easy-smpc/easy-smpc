/**
 * 
 */
package de.tu_darmstadt.cbs.app.templates;

import java.awt.Desktop;
import java.awt.Dimension;
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

import lombok.Getter;

/**
 * @author Felix Wirth
 *
 */
public class DataSharingDisplayEntry extends JPanel {

    @Getter
    private JLabel    participantNameLabel;
    @Getter
    private JLabel    participantEmailLabel;
    @Getter
    private JTextArea dataToShareTextArea;
    private JButton   sendMailButton;

    public DataSharingDisplayEntry() {
        this.setLayout(new GridBagLayout());
        this.participantNameLabel = new JLabel("Participants name");
        GridBagConstraints gbc_participantLabel = new GridBagConstraints();
        gbc_participantLabel.insets = new Insets(0, 0, 5, 5);
        gbc_participantLabel.gridx = 0;
        gbc_participantLabel.gridy = 0;
        this.add(this.participantNameLabel, gbc_participantLabel);

        this.participantEmailLabel = new JLabel("Participants Email");
        this.participantEmailLabel.setHorizontalAlignment(SwingConstants.LEFT);
        GridBagConstraints gbc_emailLabel = new GridBagConstraints();
        gbc_emailLabel.insets = new Insets(0, 0, 5, 5);
        gbc_emailLabel.gridx = 1;
        gbc_emailLabel.gridy = 0;
        this.add(this.participantEmailLabel, gbc_emailLabel);

        this.dataToShareTextArea = new JTextArea(5, 90);
        GridBagConstraints gbc_dumpedDataToSendTextArea = new GridBagConstraints();
        gbc_dumpedDataToSendTextArea.insets = new Insets(0, 0, 0, 5);
        gbc_dumpedDataToSendTextArea.gridx = 0;
        gbc_dumpedDataToSendTextArea.gridy = 1;
        gbc_dumpedDataToSendTextArea.gridwidth = 2;
        this.dataToShareTextArea.setMinimumSize(new Dimension(400, 100));
        this.dataToShareTextArea.setLineWrap(true);
        this.add(this.dataToShareTextArea, gbc_dumpedDataToSendTextArea);

        this.sendMailButton = new JButton("Send Email");
        GridBagConstraints gbc_sendMailButton = new GridBagConstraints();
        gbc_sendMailButton.gridx = 2;
        gbc_sendMailButton.gridy = 1;
        gbc_sendMailButton.anchor = GridBagConstraints.WEST;
        this.add(this.sendMailButton, gbc_sendMailButton);
    }

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
            URI mailToURI = URI.create(String.format("mailto:%s?subject=Dataexchange%s&body=%s",
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

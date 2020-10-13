package de.tu_darmstadt.cbs.app;

import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;

import de.tu_darmstadt.cbs.app.components.EntryDataSharing;
import de.tu_darmstadt.cbs.emailsmpc.Message;
import de.tu_darmstadt.cbs.emailsmpc.Participant;

/**
 * A perspective
 * 
 * @author Fabian Prasser
 */

public class PerspectiveContinue extends Perspective {
    /**
     * Button to proceed
     */
    private JButton proceedButton;
    /**
     * Panel to be filled dynamically
     */
    private JPanel  dataToSharePanel;

    /**
     * Creates new perspective
     * 
     * @param app
     */
    protected PerspectiveContinue(App app) {
        super(app, Resources.getString("PerspectiveContinue.0")); //$NON-NLS-1$
    }

    /**
     * Creates and adds UI elements
     */
    @Override
    protected void createContents(JPanel panel) {
        // ------
        // Layout
        // ------
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        JPanel workingpanel = new JPanel();
        workingpanel.setLayout(new BoxLayout(workingpanel, BoxLayout.Y_AXIS));
        JScrollPane scrollpane = new JScrollPane(workingpanel,
                                                 ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                                                 ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        panel.add(scrollpane);

        // ------
        // Data box with data to share (either to display or to enter)
        // TODO: copy button
        // ------
        this.dataToSharePanel = new JPanel();
        this.dataToSharePanel.setLayout(new BoxLayout(dataToSharePanel, BoxLayout.Y_AXIS));
        workingpanel.add(dataToSharePanel);

        // ------
        // Button box TODO: All mails at once
        // ------
        Box buttonBox = Box.createHorizontalBox();
        this.proceedButton = new JButton("Proceed");
        buttonBox.add(this.proceedButton);
        workingpanel.add(buttonBox);
    }

    /**
     * Fills the dataToSharePanel with one entry per participant to display data
     */
    public void setDataDisplay() {
        // TODO: Better explanation what user should do (F1!)
        dataToSharePanel.removeAll();
        for (int i = 0; i < SMPCServices.getServicesSMPC().getAppModel().numParticipants; i++) {
            if (i != SMPCServices.getServicesSMPC().getAppModel().ownId) {
                // Only process fields related to other participants, not
                // related to the own id

                Message messageToSend = SMPCServices.getServicesSMPC()
                                                    .getAppModel()
                                                    .getUnsentMessageFor(i);
                String messageToSendAsString = messageToSend.data;
                dataToSharePanel.add(new EntryDataSharing(SMPCServices.getServicesSMPC()
                                                                             .getAppModel().participants[i].name,
                                                                 SMPCServices.getServicesSMPC()
                                                                             .getAppModel().participants[i].emailAddress,
                                                                 messageToSendAsString,
                                                                 false,
                                                                 true));
            } else {
                dataToSharePanel.add(new EntryDataSharing(SMPCServices.getServicesSMPC()
                                                                             .getAppModel().participants[i].name,
                                                                 SMPCServices.getServicesSMPC()
                                                                             .getAppModel().participants[i].emailAddress,
                                                                 "This is your data - no action needed",
                                                                 false,
                                                                 false));
            }
        }
        dataToSharePanel.revalidate();
        dataToSharePanel.repaint();
    }

    /**
     * Fills the dataToSharePanel with one entry per participant to have user
     * entered data
     */
    public void setDataEntry() {
        dataToSharePanel.removeAll();
        for (int i = 0; i < SMPCServices.getServicesSMPC().getAppModel().numParticipants; i++) {
            // TODO- Validation mit Ampel
            if (i != SMPCServices.getServicesSMPC().getAppModel().ownId) {
                this.dataToSharePanel.add(new EntryDataSharing(SMPCServices.getServicesSMPC()
                                                                                  .getAppModel().participants[i].name,
                                                                      SMPCServices.getServicesSMPC()
                                                                                  .getAppModel().participants[i].emailAddress,
                                                                      "",
                                                                      true,
                                                                      false));
            } else {
                this.dataToSharePanel.add(new EntryDataSharing(SMPCServices.getServicesSMPC()
                                                                                  .getAppModel().participants[i].name,
                                                                      SMPCServices.getServicesSMPC()
                                                                                  .getAppModel().participants[i].emailAddress,
                                                                      "",
                                                                      false,
                                                                      false));
            }
        }
        dataToSharePanel.revalidate();
        dataToSharePanel.repaint();
    }

    /**
     * Reads data entered in dataToSharePanel and writes the data to the
     * appModel instance
     */
    public void digestDataEntry() {

        for (int i = 0; i < this.dataToSharePanel.getComponentCount(); i++) {
            if (i != SMPCServices.getServicesSMPC().getAppModel().ownId) {
                EntryDataSharing currentDataSharingDisplayEntry = (EntryDataSharing) this.dataToSharePanel.getComponent(i);
                Message messageToReceive = new Message(currentDataSharingDisplayEntry.getParticipantNameLabel()
                                                                                     .getText(),
                                                       currentDataSharingDisplayEntry.getParticipantEmailLabel()
                                                                                     .getText(),
                                                       currentDataSharingDisplayEntry.getDataToShareTextArea()
                                                                                     .getText(),
                                                       true);

                Participant currentParticipant = SMPCServices.getServicesSMPC()
                                                             .getAppModel()
                                                             .getParticipantFromId(i);
                try {
                    SMPCServices.getServicesSMPC()
                                .getAppModel()
                                .setShareFromMessage(messageToReceive, currentParticipant);
                } catch (Exception e) {
                    // TODO Auto-generated catch block, Box
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Convenience method to change behavior of proceed button
     * 
     * @param new
     *            ActionListener for Button
     */
    public void setActionListener(ActionListener newActionListener) {
        // remove only action listener and add new one
        if (this.proceedButton.getActionListeners().length > 0) this.proceedButton.removeActionListener(this.proceedButton.getActionListeners()[0]);
        this.proceedButton.addActionListener(newActionListener);
    }
}

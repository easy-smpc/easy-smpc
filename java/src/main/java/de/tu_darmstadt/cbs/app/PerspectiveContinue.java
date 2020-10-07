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
package de.tu_darmstadt.cbs.app;

import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;

import de.tu_darmstadt.cbs.app.templates.DataSharingDisplayEntry;
import de.tu_darmstadt.cbs.emailsmpc.Message;
import de.tu_darmstadt.cbs.emailsmpc.Participant;

/**
 * A perspective containing the strings a user sends manually and enter manually
 * 
 * @author Fabian Prasser
 */

public class PerspectiveContinue extends Perspective {
    /** Button to proceed */
    private JButton proceedButton;
    /** Panel to be filled dynamically */
    private JPanel  dataToSharePanel;
    
    /**
     * Creates the perspective
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
        // ------
        this.dataToSharePanel = new JPanel();
        this.dataToSharePanel.setLayout(new BoxLayout(dataToSharePanel, BoxLayout.Y_AXIS));
        workingpanel.add(dataToSharePanel);

        // ------
        // Button box
        // ------
        Box buttonBox = Box.createHorizontalBox();
        this.proceedButton = new JButton(Resources.getString("PerspectiveContinue.proceedButton"));
        buttonBox.add(this.proceedButton);
        workingpanel.add(buttonBox);
    }

    /**
     * Fills the dataToSharePanel with one entry per participant to display data
     */
    public void setDataDisplay() {
        dataToSharePanel.removeAll();
        for (int i = 0; i < SMPCServices.getServicesSMPC().getAppModel().numParticipants; i++) {
            if (i != SMPCServices.getServicesSMPC().getAppModel().numberOwnPartcipation) {
                // Only process fields related to other participants, not
                // related to the own id

                Message messageToSend = SMPCServices.getServicesSMPC()
                                                    .getAppModel()
                                                    .getUnsentMessageFor(i);
                String messageToSendAsString = messageToSend.data;
                dataToSharePanel.add(new DataSharingDisplayEntry(SMPCServices.getServicesSMPC()
                                                                             .getAppModel().participants[i].name,
                                                                 SMPCServices.getServicesSMPC()
                                                                             .getAppModel().participants[i].emailAddress,
                                                                 messageToSendAsString,
                                                                 false,
                                                                 true));
            } else {
                dataToSharePanel.add(new DataSharingDisplayEntry(SMPCServices.getServicesSMPC()
                                                                             .getAppModel().participants[i].name,
                                                                 SMPCServices.getServicesSMPC()
                                                                             .getAppModel().participants[i].emailAddress,
                                                                             Resources.getString("PerspectiveContinue.noActionNecessary"),
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
            if (i != SMPCServices.getServicesSMPC().getAppModel().numberOwnPartcipation) {
                this.dataToSharePanel.add(new DataSharingDisplayEntry(SMPCServices.getServicesSMPC()
                                                                                  .getAppModel().participants[i].name,
                                                                      SMPCServices.getServicesSMPC()
                                                                                  .getAppModel().participants[i].emailAddress,
                                                                      "",
                                                                      true,
                                                                      false));
            } else {
                this.dataToSharePanel.add(new DataSharingDisplayEntry(SMPCServices.getServicesSMPC()
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
            if (i != SMPCServices.getServicesSMPC().getAppModel().numberOwnPartcipation) {
                DataSharingDisplayEntry currentDataSharingDisplayEntry = (DataSharingDisplayEntry) this.dataToSharePanel.getComponent(i);
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
     * @param new ActionListener for Button        
     */
    public void setActionListener(ActionListener newActionListener) {
        // remove only action listener and add new one
        if (this.proceedButton.getActionListeners().length > 0) this.proceedButton.removeActionListener(this.proceedButton.getActionListeners()[0]);
        this.proceedButton.addActionListener(newActionListener);
    }
}

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

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

import de.tu_darmstadt.cbs.app.components.ComponentTextField;
import de.tu_darmstadt.cbs.app.components.ComponentTextFieldValidator;
import de.tu_darmstadt.cbs.app.components.EntryParticipantEnterExchangeString;
import de.tu_darmstadt.cbs.app.components.ExchangeStringPicker;
import de.tu_darmstadt.cbs.emailsmpc.AppState;
import de.tu_darmstadt.cbs.emailsmpc.Participant;

/**
 * A perspective
 * 
 * @author Fabian Prasser
 */

public class PerspectiveReceive extends Perspective {

    /** Panel for participants */
    private JPanel             participants;
    /** Text field containing title of study */
    private ComponentTextField title;
    /** Save button */
    private JButton            save;
    /** Central panel */
    private JPanel central;


    /**
     * Creates the perspective
     * @param app
     */
    protected PerspectiveReceive(App app) {
        super(app, Resources.getString("PerspectiveReceive.receive")); //$NON-NLS-1$
    }

    /**
     * Sets data from appModel
     */
    public void setDataAndShowPerspective() {
        this.title.setText(SMPCServices.getServicesSMPC().getAppModel().name);
        int i = 0; // index count for participants to access messages
        for (Participant currentParticipant : SMPCServices.getServicesSMPC().getAppModel().participants) {
            EntryParticipantEnterExchangeString entry = new EntryParticipantEnterExchangeString(currentParticipant.name, 
                                                                        currentParticipant.emailAddress,
                                                                        i != SMPCServices.getServicesSMPC().getAppModel().ownId //Only set buttons when not the actual user...
                                                                        && !( i == 0 && SMPCServices.getServicesSMPC().getAppModel().state == AppState.RECIEVING_SHARE)); // ... and not for participant to initiator in first round
            entry.setSendListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (new ExchangeStringPicker(new ComponentTextFieldValidator() {
                        @Override
                        public boolean validate(String text) {
                            return PerspectiveReceive.this.setMessageFromString(text, entry);
                        }
                    }, PerspectiveReceive.this.central).showDialog()) {
                        // TODO: Setze häkchen?
                    }
                }
            });
            i++;
            participants.add(entry);
        }
        this.getApp().showPerspective(PerspectiveReceive.class);
    }
    
     
     /**
     * @param text
     * @param entry
     * @return
     */
    protected boolean setMessageFromString(String text, EntryParticipantEnterExchangeString entry) {
        // TODO Set messages actually
        return true;
    }

    /**
      * Returns the exchange string for the given entry
      * @param entry
      */
     private String getExchangeString(EntryParticipantEnterExchangeString entry) {
         int id = Arrays.asList(participants.getComponents()).indexOf(entry);
         return SMPCServices.getServicesSMPC().getAppModel().getUnsentMessageFor(id).data;
     }
     
     /**
      * Returns whether this is the own entry
      * @param entry
      * @return
      */
     private boolean isOwnEntry(Component entry) {
         return Arrays.asList(participants.getComponents()).indexOf(entry) == SMPCServices.getServicesSMPC().getAppModel().ownId;
     }
     
//     /**
//      * Checks if all messages are sent
//      * @param entry
//      * @return
//      */
//     private void validateSendMessages() {
//         //this.save.setEnabled(SMPCServices.getServicesSMPC().getAppModel().messagesUnsent());
//     }

    /**
     * Save the project
     * 
     */
    private void save() {
//        SMPCServices.getServicesSMPC().getAppModel().toRecievingShares();
//        try {
//          SMPCServices.getServicesSMPC().getAppModel().saveProgram();
//
//      } catch (IOException e) {
//          JOptionPane.showMessageDialog(null, Resources.getString("PerspectiveCreate.saveError") + e.getMessage());
//      }
    }

    /**
     *Creates and adds UI elements
     */
    @Override
    protected void createContents(JPanel panel) {

        // Layout
        panel.setLayout(new BorderLayout());

        // -------
        // Study title
        // -------
        JPanel title = new JPanel();
        panel.add(title, BorderLayout.NORTH);
        title.setLayout(new BorderLayout());
        title.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED),
                                                         Resources.getString("PerspectiveCreate.studyTitle"),
                                                         TitledBorder.LEFT,
                                                         TitledBorder.DEFAULT_POSITION));
        this.title = new ComponentTextField(new ComponentTextFieldValidator() {
            @Override
            public boolean validate(String text) {
                return true; //no actual validation as field is not set by user
            }
        });
        this.title.setEnabled(false);
        title.add(this.title, BorderLayout.CENTER);
        
        // Central panel
        central = new JPanel();
        central.setLayout(new GridLayout(2, 1));
        panel.add(central, BorderLayout.CENTER);        
        
        // ------
        // Participants
        // ------
        this.participants = new JPanel();
        this.participants.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED),
                                                                     Resources.getString("PerspectiveReceive.participants"),
                                                                     TitledBorder.LEFT,
                                                                     TitledBorder.DEFAULT_POSITION));
        this.participants.setLayout(new BoxLayout(this.participants, BoxLayout.Y_AXIS));
        JScrollPane pane = new JScrollPane(participants);
        pane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        central.add(pane, BorderLayout.NORTH);    
           
        // ------
        // save button
        // ------
        JPanel buttonsPane = new JPanel();
        
        save = new JButton(Resources.getString("PerspectiveReceive.save"));
        save.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                save();
            }
        });
        panel.add(save, BorderLayout.SOUTH);
    }
}

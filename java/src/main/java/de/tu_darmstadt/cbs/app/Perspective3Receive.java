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
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.Arrays;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import de.tu_darmstadt.cbs.app.components.ComponentTextField;
import de.tu_darmstadt.cbs.app.components.ComponentTextFieldValidator;
import de.tu_darmstadt.cbs.app.components.EntryParticipantEnterExchangeString;
import de.tu_darmstadt.cbs.app.components.ExchangeStringPicker;
import de.tu_darmstadt.cbs.app.resources.Resources;
import de.tu_darmstadt.cbs.emailsmpc.AppState;
import de.tu_darmstadt.cbs.emailsmpc.Bin;
import de.tu_darmstadt.cbs.emailsmpc.Message;
import de.tu_darmstadt.cbs.emailsmpc.Participant;

/**
 * A perspective
 * 
 * @author Fabian Prasser
 * @author Felix Wirth
 */

public class Perspective3Receive extends Perspective implements ChangeListener, ActionListener {

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
    protected Perspective3Receive(App app) {
        super(app, Resources.getString("PerspectiveReceive.receive")); //$NON-NLS-1$
    }

    /**
     * Initialize perspective based on model
     */
    @Override
    protected void initialize() {
        this.title.setText(SMPCServices.getServicesSMPC().getAppModel().name);
        participants.removeAll();
        int i = 0; // index count for participants to access messages
        for (Participant currentParticipant : SMPCServices.getServicesSMPC().getAppModel().participants) {
            EntryParticipantEnterExchangeString entry = new EntryParticipantEnterExchangeString(currentParticipant.name, 
                                                                        currentParticipant.emailAddress,
                                                                        i != SMPCServices.getServicesSMPC().getAppModel().ownId //Only set buttons when not the actual user...
                                                                        && !( i == 0 && SMPCServices.getServicesSMPC().getAppModel().state == AppState.RECIEVING_SHARE)); // ... and not for participant to initiator in first round
            entry.setButtonListener(this);
            i++;
            participants.add(entry);
        }
        this.stateChanged(new ChangeEvent(this));
    }
    
     
     /**
     * @param text
     * @param entry
     * @return
     */
    protected boolean setMessageFromString(String exchangeString, EntryParticipantEnterExchangeString entry) {

        try {
            SMPCServices.getServicesSMPC()
                        .getAppModel()
                        .setShareFromMessage(Message.deserializeMessage(exchangeString),
                                             SMPCServices.getServicesSMPC()
                                                         .getAppModel()
                                                         .getParticipantFromId(Arrays.asList(participants.getComponents())
                                                                                     .indexOf(entry)));
            return true;
        } catch (IllegalStateException | IllegalArgumentException | ClassNotFoundException
                | IOException e) {
            System.out.println(e.toString());
            return false;
        }
    }
     
    /**
     * Reacts on all changes in any components
     */
    @Override
    public void stateChanged(ChangeEvent e) {
        this.save.setEnabled(this.areSharesComplete());
    }
    
    /**
     * Checks if all bins are complete
     * @return
     */
    private boolean areSharesComplete() {
        for (Bin b : SMPCServices.getServicesSMPC().getAppModel().bins) {
            if (!b.isComplete()) return false;
        }
        return true;
    }

    /**
     * Save the project
     * 
     */
    private void save() {
        try {
            switch (SMPCServices.getServicesSMPC().getAppModel().state) {
            case RECIEVING_SHARE:
                SMPCServices.getServicesSMPC().getAppModel().toSendingResult();
                SMPCServices.getServicesSMPC().getAppModel().saveProgram();
                this.getApp().getPerspective(Perspective2Send.class).initialize();
                this.getApp().showPerspective(Perspective2Send.class);
                break;
            case RECIEVING_RESULT:
                SMPCServices.getServicesSMPC().getAppModel().toFinished();
                SMPCServices.getServicesSMPC().getAppModel().saveProgram();
                this.getApp().getPerspective(Perspective6Finalize.class).initialize();
                this.getApp().showPerspective(Perspective6Finalize.class);
                break;
            default:
                throw new Exception(String.format(Resources.getString("PerspectiveReceive.wrongState"),
                                                  SMPCServices.getServicesSMPC()
                                                              .getAppModel().state));
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null,
                                          Resources.getString("PerspectiveReceive.saveError") +
                                                e.getMessage());
        }
    }

    /**
     *Creates and adds UI elements
     */
    @Override
    protected void createContents(JPanel panel) {

        // Layout
        panel.setLayout(new BorderLayout());

        // Study title
        JPanel title = new JPanel();
        panel.add(title, BorderLayout.NORTH);
        title.setLayout(new BorderLayout());
        title.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED),
                                                         Resources.getString("PerspectiveCreate.studyTitle"),
                                                         TitledBorder.LEFT,
                                                         TitledBorder.DEFAULT_POSITION));
        this.title = new ComponentTextField(null); //no validation necessary
        this.title.setEnabled(false);
        title.add(this.title, BorderLayout.CENTER);
        
        // Central panel
        central = new JPanel();
        central.setLayout(new GridLayout(2, 1));
        panel.add(central, BorderLayout.CENTER);        
        
        // Participants
        this.participants = new JPanel();
        this.participants.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED),
                                                                     Resources.getString("PerspectiveReceive.participants"),
                                                                     TitledBorder.LEFT,
                                                                     TitledBorder.DEFAULT_POSITION));
        this.participants.setLayout(new BoxLayout(this.participants, BoxLayout.Y_AXIS));
        JScrollPane pane = new JScrollPane(participants);
        pane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        central.add(pane, BorderLayout.NORTH);    
           
        // save button
        
        save = new JButton(Resources.getString("PerspectiveReceive.save"));
        save.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                save();
            }
        });
        panel.add(save, BorderLayout.SOUTH);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        EntryParticipantEnterExchangeString entry = (EntryParticipantEnterExchangeString) e.getSource();
        String message = new ExchangeStringPicker(new ComponentTextFieldValidator() {
            @Override
            public boolean validate(String text) {
                return SMPCServices.getServicesSMPC()
                                   .isMessageShareResultValid(text,
                                                              Arrays.asList(participants.getComponents())
                                                                    .indexOf(entry));
            }
        }, Perspective3Receive.this.central).showDialog();

        if (message != null) {
            Perspective3Receive.this.setMessageFromString(message, entry);
            Perspective3Receive.this.stateChanged(new ChangeEvent(this));
        }
    }
}

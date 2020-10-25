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
import java.awt.Desktop;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.URI;
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
import de.tu_darmstadt.cbs.app.components.EntryParticipantSendMail;
import de.tu_darmstadt.cbs.app.resources.Resources;
import de.tu_darmstadt.cbs.emailsmpc.Message;
import de.tu_darmstadt.cbs.emailsmpc.Participant;

/**
 * A perspective
 * 
 * @author Fabian Prasser
 * @author Felix Wirth
 */

public class Perspective2Send extends Perspective implements ChangeListener {

    /** Panel for participants */
    private JPanel             participants;
    
    /** Text field containing title of study */
    private ComponentTextField title;
    
    /** send all emails at once button */
    private JButton            sendAllEmailsButton;
    
    /** Save button */
    private JButton            save;

    /**
     * Creates the perspective
     * @param app
     */
    protected Perspective2Send(App app) {
        super(app, Resources.getString("PerspectiveSend.send"), 2); //$NON-NLS-1$
    }

    /**
     * Creates the perspective
     * @param app
     * @param progress
     */
    protected Perspective2Send(App app, int progress) {
        super(app, Resources.getString("PerspectiveSend.send"), progress); //$NON-NLS-1$
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
            EntryParticipantSendMail entry = new EntryParticipantSendMail(currentParticipant.name, 
                                                                         currentParticipant.emailAddress,
                                                                         i != SMPCServices.getServicesSMPC().getAppModel().ownId);
            entry.setButtonListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    Perspective2Send.this.sendMail(entry);
                }
            });
            i++;
            participants.add(entry);
        }
        this.stateChanged(new ChangeEvent(this));
        this.getApp().showPerspective(Perspective2Send.class);
    }
    
    /**
     * Sends an e-mail to the participant entry
     * @param entry
     */
     protected void sendMail(EntryParticipantSendMail entry) {
        URI mailToURI;
        try {
            mailToURI = URI.create(String.format(Resources.mailToString,
                                                     entry.getRightValue(), //E-mail address
                                                     String.format(Resources.getString("PerspectiveSend.mailSubject"),//Generate subject 
                                                                   SMPCServices.getServicesSMPC().getAppModel().name),                                                                 
                                                     String.format(Resources.getString("PerspectiveSend.mailBody") //Generate body
                                                                   ,entry.getLeftValue()
                                                                   ,getExchangeString(entry))
                                                     ).replaceAll(" ", "%20"));         
            Desktop.getDesktop().mail(mailToURI);
          //Send a dialog to confirm mail sending
            if (JOptionPane.showConfirmDialog(null,
                                              String.format( Resources.getString("PerspectiveSend.confirmSendMail"), entry.getRightValue()),
                                              "",
                                              JOptionPane.OK_CANCEL_OPTION) == 0) {            
            SMPCServices.getServicesSMPC().getAppModel().markMessageSent(Arrays.asList(participants.getComponents()).indexOf(entry));
            this.stateChanged(new ChangeEvent(this));
            }
            
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, Resources.getString("PerspectiveSend.mailToError") + e.getMessage());
        }    
    }
     
     /**
      * Reacts on all changes in any components
      */
     @Override
     public void stateChanged(ChangeEvent e) {
         this.save.setEnabled(!this.validateSentMessages());
     }
     
    /**
     * Returns the exchange string for the given entry
     * 
     * @param entry
     * @throws IOException
     */
    private String getExchangeString(EntryParticipantSendMail entry) throws IOException {
        int id = Arrays.asList(participants.getComponents()).indexOf(entry);
        return Message.serializeMessage(SMPCServices.getServicesSMPC()
                                                    .getAppModel()
                                                    .getUnsentMessageFor(id));
    }
    
     /**
      * Returns whether this is the own entry
      * @param entry
      * @return
      */
    private boolean isOwnEntry(Component entry) {
        return Arrays.asList(participants.getComponents())
                     .indexOf(entry) == SMPCServices.getServicesSMPC().getAppModel().ownId;
    }
     
     /**
      * Checks if all messages are sent
      * @param entry
      * @return
      */
     private boolean validateSentMessages() {
        return SMPCServices.getServicesSMPC().getAppModel().messagesUnsent();
    }

    /**
     * Save the project
     * 
     */
    private void save() {
        try {
            switch (SMPCServices.getServicesSMPC().getAppModel().state) {
            case INITIAL_SENDING:
                SMPCServices.getServicesSMPC().getAppModel().toRecievingShares();
                break;
            case SENDING_SHARE:
                SMPCServices.getServicesSMPC().getAppModel().toRecievingShares();
                break;
            case SENDING_RESULT:
                SMPCServices.getServicesSMPC().getAppModel().toRecievingResult();
                break;
            default:
                throw new Exception(String.format(Resources.getString("PerspectiveSend.wrongState"),
                                                  SMPCServices.getServicesSMPC()
                                                              .getAppModel().state));
            }
            SMPCServices.getServicesSMPC().getAppModel().saveProgram();
            this.getApp().getPerspective(Perspective3Receive.class).initialize();
            this.getApp().showPerspective(Perspective3Receive.class);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null,
                                          Resources.getString("PerspectiveSend.saveError") +
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
        this.title = new ComponentTextField(null); //no validation
        this.title.setEnabled(false);
        title.add(this.title, BorderLayout.CENTER);
        
        // Central panel
        JPanel central = new JPanel();
        central.setLayout(new GridLayout(2, 1));
        panel.add(central, BorderLayout.CENTER);        
        
        // Participants
        this.participants = new JPanel();
        this.participants.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED),
                                                                     Resources.getString("PerspectiveSend.participants"),
                                                                     TitledBorder.LEFT,
                                                                     TitledBorder.DEFAULT_POSITION));
        this.participants.setLayout(new BoxLayout(this.participants, BoxLayout.Y_AXIS));
        JScrollPane pane = new JScrollPane(participants);
        pane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        central.add(pane, BorderLayout.NORTH);    
           
        // send all e-mails button and save button
        JPanel buttonsPane = new JPanel();
        buttonsPane.setLayout(new GridLayout(2, 1));
        sendAllEmailsButton = new JButton(Resources.getString("PerspectiveSend.sendAllEmailsButton"));
        sendAllEmailsButton.addActionListener(new ActionListener() {            
            @Override
            public void actionPerformed(ActionEvent e) {
                for (Component c : Perspective2Send.this.participants.getComponents()) {
                    if (!isOwnEntry(c)) {
                        Perspective2Send.this.sendMail((EntryParticipantSendMail) c);
                    }
                }
            }
        });
        buttonsPane.add(sendAllEmailsButton, 0,0);
        save = new JButton(Resources.getString("PerspectiveSend.save"));
        save.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                save();
            }
        });
        buttonsPane.add(save, 0,1);
        panel.add(buttonsPane, BorderLayout.SOUTH);
    }
}

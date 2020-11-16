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
import java.net.URISyntaxException;
import java.util.ArrayList;
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

import org.apache.http.client.utils.URIBuilder;

import de.tu_darmstadt.cbs.app.components.ComponentTextField;
import de.tu_darmstadt.cbs.app.components.EntryParticipantSendMail;
import de.tu_darmstadt.cbs.app.resources.Resources;
import de.tu_darmstadt.cbs.emailsmpc.AppState;
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
    private JPanel                                 participants;

    /** Text field containing title of study */
    private ComponentTextField                     title;

    /** Save button */
    private JButton                                save;

    private JButton send;

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
     * Creates the perspective
     * @param app
     * @param progress
     */
    protected Perspective2Send(App app, String title , int progress) {
        super(app, title, progress); //$NON-NLS-1$
    }
    

    /**
      * Reacts on all changes in any components
      */
     @Override
     public void stateChanged(ChangeEvent e) {
         boolean messagesUnsent = getApp().getModel().messagesUnsent();
         this.save.setEnabled(!messagesUnsent);
         this.send.setEnabled(messagesUnsent);
     }
    
    /**
     * Returns the exchange string for the given entry
     * 
     * @param entry
     * @throws IOException
     */
    private String getExchangeString(EntryParticipantSendMail entry) throws IOException {
        int id = Arrays.asList(participants.getComponents()).indexOf(entry);
        return Message.serializeMessage(getApp().getModel().getUnsentMessageFor(id));
    }
    
     /**
      * Returns whether this is the own entry
      * @param entry
      * @return
      */
    private boolean isOwnEntry(Component entry) {
        return Arrays.asList(participants.getComponents()).indexOf(entry) == getApp().getModel().ownId;
    }
    
    /**
     * Returns whether there are unsent message for the entry
     * @param entry
     * @return
     */
   private boolean unsentMessages(Component entry) {
       return getApp().getModel().getUnsentMessageFor(Arrays.asList(participants.getComponents()).indexOf(entry)) != null;                     
   }
     
    /**
     * Save action
     */
    protected void actionSave() {
        getApp().actionFirstSendingDone();
    }
    
     /**
     * Sends an e-mail to the participant entry
     * @param list
     */
    protected void actionSendMail(ArrayList<EntryParticipantSendMail> list) {
        try {
            for (EntryParticipantSendMail entry : list) {
                URIBuilder emailURIBuilder = new URIBuilder().setScheme("mailto");
                emailURIBuilder.setPath(entry.getRightValue()); // E-mail
                                                                // address
                emailURIBuilder.addParameter("subject",
                                             String.format(Resources.getString("PerspectiveSend.mailSubject"),
                                                           getApp().getModel().name,
                                                           getApp().getModel().state == AppState.SENDING_RESULT
                                                                   ? 2
                                                                   : 1));
                String formatedExchangeString = Resources.exchangeStringStartTag +
                                                getExchangeString(entry) +
                                                Resources.exchangeStringEndTag;
                formatedExchangeString = formatedExchangeString.replaceAll("(.{" +
                                                                           Resources.exchangeStringNewLineCountChar +
                                                                           "})",
                                                                           "$1\n");
                emailURIBuilder.addParameter("body",
                                             String.format(Resources.getString("PerspectiveSend.mailBody"),
                                                           entry.getLeftValue(), // Name
                                                                                 // of
                                                                                 // participant
                                                           getApp().getModel().state == AppState.SENDING_RESULT
                                                                   ? 5
                                                                   : 3, // Step
                                                                        // number
                                                           formatedExchangeString,
                                                           getApp().getModel().participants[getApp().getModel().ownId].name));
                Desktop.getDesktop()
                       .mail(new URI(emailURIBuilder.toString()
                                                    .replace("+", "%20")
                                                    .replace(":/", ":")));
            }

            // Send a dialog to confirm mail sending          
            if (JOptionPane.showConfirmDialog(this.getPanel(),
                                              String.format(Resources.getString("PerspectiveSend.confirmSendMailGeneric")),
                                              "",
                                              JOptionPane.OK_CANCEL_OPTION) == 0) {
                for (EntryParticipantSendMail entry : list) {
                    int index = Arrays.asList(this.participants.getComponents()).indexOf(entry);
                    getApp().actionMarkMessageSent(index);
                    entry.disableButton();
                }
            }

        } catch (IOException | URISyntaxException e) {
            JOptionPane.showMessageDialog(this.getPanel(),
                                          Resources.getString("PerspectiveSend.mailToError") +
                                                           e.getMessage());
        }
        this.stateChanged(new ChangeEvent(this));

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
        
        // Participants
        this.participants = new JPanel();
        this.participants.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED),
                                                                     Resources.getString("PerspectiveSend.participants"),
                                                                     TitledBorder.LEFT,
                                                                     TitledBorder.DEFAULT_POSITION));
        this.participants.setLayout(new BoxLayout(this.participants, BoxLayout.Y_AXIS));
        JScrollPane pane = new JScrollPane(participants);
        pane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        panel.add(pane, BorderLayout.CENTER);    
           
        // send all e-mails button and save button
        JPanel buttonsPane = new JPanel();
        buttonsPane.setLayout(new GridLayout(2, 1));
        send = new JButton(Resources.getString("PerspectiveSend.sendAllEmailsButton"));
        send.addActionListener(new ActionListener() {            
            @Override
            public void actionPerformed(ActionEvent e) {
                ArrayList<EntryParticipantSendMail> list = new ArrayList<>();
                for (Component c : participants.getComponents()) {
                    if (!isOwnEntry(c) && unsentMessages(c) ) {
                        list.add((EntryParticipantSendMail)  c);                        
                    }
                }
                actionSendMail(list);
            }
        });
        buttonsPane.add(send, 0, 0);
        save = new JButton(Resources.getString("PerspectiveSend.save"));
        save.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                actionSave();
            }
        });
        buttonsPane.add(save, 0, 1);
        panel.add(buttonsPane, BorderLayout.SOUTH);
    }

    /**
     * Initialize perspective based on model
     */
    @Override
    protected void initialize() {
        this.title.setText(getApp().getModel().name);
        this.participants.removeAll();
        int i = 0; // index count for participants to access messages
        for (Participant currentParticipant : getApp().getModel().participants) {
            EntryParticipantSendMail entry = new EntryParticipantSendMail(currentParticipant.name, currentParticipant.emailAddress, i != getApp().getModel().ownId);
            participants.add(entry);
            entry.setButtonListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    ArrayList<EntryParticipantSendMail> list = new ArrayList<>();
                    list.add(entry);
                    actionSendMail(list);
                }
            });
            i++;
        }
        this.stateChanged(new ChangeEvent(this));
    }
}

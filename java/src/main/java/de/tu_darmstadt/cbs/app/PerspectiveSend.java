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

import de.tu_darmstadt.cbs.app.components.ComponentTextField;
import de.tu_darmstadt.cbs.app.components.ComponentTextFieldValidator;
import de.tu_darmstadt.cbs.app.components.EntryParticipantSendMail;
import de.tu_darmstadt.cbs.emailsmpc.Participant;

/**
 * A perspective
 * 
 * @author Fabian Prasser
 */

public class PerspectiveSend extends Perspective {

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
    protected PerspectiveSend(App app) {
        super(app, Resources.getString("PerspectiveSend.send")); //$NON-NLS-1$
    }

    /**
     * Sets data from appModel
     */
    public void setDataAndShowPerspective() {
        this.title.setText(SMPCServices.getServicesSMPC().getAppModel().name);
        int i = 0; // index count for participants to access messages
        for (Participant currentParticipant : SMPCServices.getServicesSMPC().getAppModel().participants) {
            EntryParticipantSendMail entry = new EntryParticipantSendMail(currentParticipant.name, currentParticipant.emailAddress, i != SMPCServices.getServicesSMPC().getAppModel().ownId);
            entry.setSendListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    PerspectiveSend.this.sendMail(entry);
                }
            });
            i++;
            participants.add(entry);
        }
        this.getApp().showPerspective(PerspectiveSend.class);
    }
    
    /**
     * Sends an e-mail to the participant entry
     * @param entry
     */
     protected void sendMail(EntryParticipantSendMail entry) {
        URI mailToURI = URI.create(String.format(Resources.mailToString,
                                                 entry.getRightValue(), //E-mail address
                                                 String.format(Resources.getString("PerspectiveSend.mailSubject"),//Generate subject 
                                                               SMPCServices.getServicesSMPC().getAppModel().name),                                                                 
                                                 String.format(Resources.getString("PerspectiveSend.mailBody") //Generate body
                                                               ,entry.getLeftValue()
                                                               ,getExchangeString(entry).replace("%", "%%"))
                                                 ).replace(" ", "%20"));
        try {
            Desktop.getDesktop().mail(mailToURI);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, Resources.getString("PerspectiveSend.mailToError") + e.getMessage());
        }
    }
     
     /**
      * Returns the exchange string for the given entry
      * @param entry
      */
     private String getExchangeString(EntryParticipantSendMail entry) {
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

    /**
     * Save the project
     * 
     */
    private void save() {
//        // File
//        File file = null;
//        JFileChooser fileChooser = new JFileChooser();
//        if (fileChooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
//            file = fileChooser.getSelectedFile();
//        }
//        
//        // Check
//        if (file == null) {
//            return;
//        }
//        
//      BigInteger[] secretValuesofParticipant = new BigInteger[SMPCServices.getServicesSMPC()
//                                                                         .getAppModel().bins.length];
//      Integer i = 0;
//      for(Component currentBinEntry: this.bins.getComponents())
//      {
//          
//          secretValuesofParticipant[i] = new BigInteger(( (EntryBin) currentBinEntry).getRightValue());
//          i++;
//      }
//      SMPCServices.getServicesSMPC().getAppModel().toSendingShares(secretValuesofParticipant);
//        // Try to save file
//        try {
//            SMPCServices.getServicesSMPC().getAppModel().filename = file;
//            SMPCServices.getServicesSMPC().getAppModel().saveProgram();
//            SMPCServices.getServicesSMPC().setWorkflowState(AppState.SENDING_SHARE); //interim
//            SMPCServices.getServicesSMPC().commandAndControl(); //interim
//
//        } catch (IOException e) {
//            JOptionPane.showMessageDialog(null, Resources.getString("PerspectiveCreate.saveError") + e.getMessage());
//        }
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
        JPanel central = new JPanel();
        central.setLayout(new GridLayout(2, 1));
        panel.add(central, BorderLayout.CENTER);        
        
        // ------
        // Participants
        // ------
        this.participants = new JPanel();
        this.participants.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED),
                                                                     Resources.getString("PerspectiveCreate.participants"),
                                                                     TitledBorder.LEFT,
                                                                     TitledBorder.DEFAULT_POSITION));
        this.participants.setLayout(new BoxLayout(this.participants, BoxLayout.Y_AXIS));
        JScrollPane pane = new JScrollPane(participants);
        pane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        central.add(pane, BorderLayout.NORTH);    
           
        // ------
        // enterExchangeString button and save button
        // ------
        JPanel buttonsPane = new JPanel();
        buttonsPane.setLayout(new GridLayout(2, 1));
        sendAllEmailsButton = new JButton(Resources.getString("PerspectiveSend.sendAllEmailsButton"));
        sendAllEmailsButton.addActionListener(new ActionListener() {            
            @Override
            public void actionPerformed(ActionEvent e) {
                for (Component c : PerspectiveSend.this.participants.getComponents()) {
                    if (!isOwnEntry(c)) {
                        PerspectiveSend.this.sendMail((EntryParticipantSendMail) c);
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

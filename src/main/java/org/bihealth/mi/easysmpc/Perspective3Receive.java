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
package org.bihealth.mi.easysmpc;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

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

import org.bihealth.mi.easybus.BusException;
import org.bihealth.mi.easybus.Message;
import org.bihealth.mi.easybus.MessageListener;
import org.bihealth.mi.easybus.Scope;
import org.bihealth.mi.easysmpc.components.ComponentTextField;
import org.bihealth.mi.easysmpc.components.EntryParticipantCheckmark;
import org.bihealth.mi.easysmpc.components.ScrollablePanel;
import org.bihealth.mi.easysmpc.dataimport.ImportClipboard;
import org.bihealth.mi.easysmpc.resources.Resources;

import de.tu_darmstadt.cbs.emailsmpc.Bin;
import de.tu_darmstadt.cbs.emailsmpc.Participant;
import de.tu_darmstadt.cbs.emailsmpc.Study.StudyState;

/**
 * A perspective
 * 
 * @author Fabian Prasser
 * @author Felix Wirth
 */
public class Perspective3Receive extends Perspective implements ChangeListener, ActionListener, MessageListener {
    
    /** Panel for participants */
    private ScrollablePanel    participants;
    
    /** Text field containing title of study */
    private ComponentTextField title;
    
    /** Proceed button */
    private JButton            proceed;
    
    /** Receive button */
    private JButton receive;

    /**
     * Creates the perspective
     * @param app
     */
    protected Perspective3Receive(App app) {
        this(app, Resources.getString("PerspectiveReceive.receive"), 3); //$NON-NLS-1$
    }

    /**
     * Creates the perspective
     * @param app
     * @param progress
     */
    protected Perspective3Receive(App app, int progress) {
        this(app, Resources.getString("PerspectiveReceive.receive"), progress); //$NON-NLS-1$
    }
    
    /**
     * Creates the perspective
     * @param app
     * @param progress
     */
    protected Perspective3Receive(App app, String title, int progress) {
        super(app, title, progress, true); //$NON-NLS-1$
        // Register execution periodically
        new ImportClipboard(this);
    }    
    
    @Override
    public void actionPerformed(ActionEvent e) {
        getApp().actionReceiveMessage();
        this.stateChanged(new ChangeEvent(this));
    }
    
    @Override
    public void receive(Message message) {
        String messageStripped = ImportClipboard.getStrippedExchangeMessage((String) message.getMessage());
        if (getApp().isMessageShareResultValid(messageStripped)) {
            getApp().setMessageShare(messageStripped);
            stateChanged(new ChangeEvent(this));
            getApp().actionSave();
        }
    }

    /**
     * Reacts on all changes in any components
     */
    @Override
    public void stateChanged(ChangeEvent e) {
        checkmarkParticipantEntries();
        this.receive.setEnabled(!this.areSharesComplete());
        this.proceed.setEnabled(this.areSharesComplete());
    }
     
    /**
     * Checks if all bins are complete
     * @return
     */
    private boolean areSharesComplete() {
        for (Bin b : getApp().getModel().bins) {
            if (!b.isComplete()) return false;
        }
        return true;
    }
    
    /**
     * Checks if all bins for a certain user id are complete
     * @return
     */
    private boolean areSharesCompleteForParticipantId(int participantId) {
        for (Bin b : getApp().getModel().bins) {
            if (!b.isCompleteForParticipantId(participantId)) return false;
        }
        return true;
    }

    /**
     * Check participant entries visually if complete
     */
    private void checkmarkParticipantEntries() {
        int i=0;
        for (Component c : this.participants.getComponents()) {
            ((EntryParticipantCheckmark) c).setCheckmarkEnabled(i == getApp().getModel().ownId || //Always mark own id as "received"
                                                                (getApp().getModel().state != StudyState.RECIEVING_RESULT  &&  i == 0) || //Mark first entry in first round as received
                                                                areSharesCompleteForParticipantId(i)); //Mark if share complete
            i++;
        }
    }
    
    /**
     * Indicates whether the automatic processing enabled
     * 
     * @return enabled
     */
    private boolean isAutomaticProcessingEnabled() {
        return getApp().getModel().connectionIMAPSettings != null;
    }
    
    /**
     * Start the automatic import of e-mails if necessary
     */
    private void startAutomatedMailImport() {        
        try {
            getApp().getModel().getBus().receive(new Scope(getApp().getModel().studyUID + getRoundIdentifier()),
                        new org.bihealth.mi.easybus.Participant(getApp().getModel().getParticipantFromId(getApp().getModel().ownId).name,
                                                                getApp().getModel().getParticipantFromId(getApp().getModel().ownId).emailAddress),
                                                                this);
        } catch (IllegalArgumentException | BusException e) {
            JOptionPane.showMessageDialog(getPanel(),
                                          Resources.getString("PerspectiveReceive.AutomaticEmailErrorRegistering"),
                                          Resources.getString("PerspectiveReceive.AutomaticEmail"),
                                          JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Proceed the project
     * 
     */
    protected void actionProceed() {
        getApp().actionFirstReceivingDone();
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
        
        // Participants
        this.participants = new ScrollablePanel();
        this.participants.setLayout(new BoxLayout(this.participants, BoxLayout.Y_AXIS));
        JScrollPane pane = new JScrollPane(participants, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        pane.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED),
                                                                     Resources.getString("PerspectiveReceive.participants"),
                                                                     TitledBorder.LEFT,
                                                                     TitledBorder.DEFAULT_POSITION));
        panel.add(pane, BorderLayout.CENTER);
        
        
        // Receive button and save button
        JPanel buttonsPane = new JPanel();
        buttonsPane.setLayout(new GridLayout(2, 1));
        receive = new JButton(Resources.getString("PerspectiveReceive.receive"));
        receive.addActionListener(this);       
        buttonsPane.add(receive, 0, 0);
        
        proceed = new JButton(Resources.getString("PerspectiveReceive.proceed"));
        proceed.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                actionProceed();
            }
        });
        buttonsPane.add(proceed, 0, 1);
        panel.add(buttonsPane, BorderLayout.SOUTH);
    }

    /**
     * Returns an identifier for the current round of EasySMPC 
     * This is needed to make sure the correct message are sent to the correct receivers
     * 
     * @return round
     */
    protected String getRoundIdentifier() {
        return Resources.ROUND_1;
    }

    /**
     * Initialize perspective based on model
     */
    @Override
    protected void initialize() {
        this.title.setText(getApp().getModel().name);
        this.participants.removeAll();
        for (Participant currentParticipant : getApp().getModel().participants) {
            EntryParticipantCheckmark entry = new EntryParticipantCheckmark(currentParticipant.name,
                                                                            currentParticipant.emailAddress);
            participants.add(entry);
        }
        
        // Start import reading e-mails automatically if enabled 
        if (isAutomaticProcessingEnabled()) {
            startAutomatedMailImport();
        }
        
        // Update GUI
        this.stateChanged(new ChangeEvent(this));
        getPanel().revalidate();
        getPanel().repaint(); 
    } 
}
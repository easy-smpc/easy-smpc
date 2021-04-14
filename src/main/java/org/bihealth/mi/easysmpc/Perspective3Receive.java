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
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
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
    private ScrollablePanel    panelParticipants;

    /** Text field containing title of study */
    private ComponentTextField fieldTitle;

    /** Proceed button */
    private JButton            buttonProceed;

    /** Receive button */
    private JButton            buttonReceive;
    
    /** Buttons pane */
    private JPanel buttonsPane;
    
    /** Button poll manually*/
    private JButton buttonPollManually;

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
        getApp().actionSave();
        getApp().setStatusMessage( String.format(Resources.getString("PerspectiveReceive.displaySuccess")
                                                        , numberSharesComplete()
                                                        , numberExpectedMessages())
                                          , false, true);

        this.stateChanged(new ChangeEvent(this));
    }
    
    @Override
    public void receive(Message message) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                String messageStripped = ImportClipboard.getStrippedExchangeMessage((String) message.getMessage());
                if (getApp().isMessageShareResultValid(messageStripped)) {
                    getApp().setMessageShare(messageStripped);
                    getApp().actionSave();
                    getApp().setStatusMessage( String.format(Resources.getString("PerspectiveReceive.displaySuccess")
                                                                    , numberSharesComplete()
                                                                    , numberExpectedMessages())
                                                      , false, true);
                    stateChanged(new ChangeEvent(this));
                }
            }
        });
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        
        // Update
        updateCheckmarks();
        boolean sharesComplete = this.areSharesComplete();
        this.buttonReceive.setEnabled(!sharesComplete);
        this.buttonProceed.setEnabled(sharesComplete);
        
        // If no more messages and automatic processing proceed automatically
        if (sharesComplete && isAutomaticProcessingEnabled()) {
            actionProceed();
        }
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
     * Checks the number of complete messages
     * 
     * @return
     */
    public int numberSharesComplete() {
        int numberComplete = 0;
        for (int i = 0; i < getApp().getModel().numParticipants; i++) {
            if (areSharesCompleteForParticipantId(i)) numberComplete++;
        }
        return numberComplete - 1;
        }
    
    /**
     * Returns number of expected external messages
     * 
     * @return
     */
    public int numberExpectedMessages() {
        return getApp().getModel().numParticipants - 1;
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
     * Check participant entries visually if complete
     */
    private void updateCheckmarks() {
        int i=0;
        for (Component c : this.panelParticipants.getComponents()) {
            ((EntryParticipantCheckmark) c).setCheckmarkEnabled(i == getApp().getModel().ownId || // Always mark own id as "received"
                                                                (getApp().getModel().state != StudyState.RECIEVING_RESULT  &&  i == 0) || // Mark first entry in first round as received
                                                                areSharesCompleteForParticipantId(i)); // Mark if share complete
            i++;
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

        // General data data of study
        JPanel generalDataPanel = new JPanel();
        generalDataPanel.setLayout(new GridLayout(1 , 1, Resources.ROW_GAP, Resources.ROW_GAP));
        panel.add(generalDataPanel, BorderLayout.NORTH);
        generalDataPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED),
                                                                    Resources.getString("PerspectiveCreate.General"),
                                                                    TitledBorder.LEFT,
                                                                    TitledBorder.DEFAULT_POSITION));
        
        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new BorderLayout(Resources.ROW_GAP, Resources.ROW_GAP));
        titlePanel.add(new JLabel(Resources.getString("PerspectiveCreate.studyTitle")), BorderLayout.WEST);
        this.fieldTitle = new ComponentTextField(null);
        this.fieldTitle.setEnabled(false);
        this.fieldTitle.setChangeListener(this);
        titlePanel.add(this.fieldTitle, BorderLayout.CENTER);
        
        // Add loading visualization
        generalDataPanel.add(titlePanel);
        
        // Participants
        this.panelParticipants = new ScrollablePanel();
        this.panelParticipants.setLayout(new BoxLayout(this.panelParticipants, BoxLayout.Y_AXIS));
        JScrollPane pane = new JScrollPane(panelParticipants, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        pane.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED),
                                                                     Resources.getString("PerspectiveReceive.participants"),
                                                                     TitledBorder.LEFT,
                                                                     TitledBorder.DEFAULT_POSITION));
        panel.add(pane, BorderLayout.CENTER);
                
        // Buttons pane
        buttonsPane = new JPanel();

        buttonPollManually = new JButton(Resources.getString("PerspectiveReceive.PollManually"));
        buttonPollManually.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                getApp().getModel().stopBus();
                startAutomatedMailImport();
            }
        });
        
        buttonReceive = new JButton(Resources.getString("PerspectiveReceive.receiveButton"));
        buttonReceive.addActionListener(this);       
        
        buttonProceed = new JButton(Resources.getString("PerspectiveReceive.proceed"));
        buttonProceed.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                actionProceed();
            }
        });
        
        // Adds for south panel
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
        this.fieldTitle.setText(getApp().getModel().name);
        this.panelParticipants.removeAll();
        int i = 0;
        for (Participant currentParticipant : getApp().getModel().participants) {
            EntryParticipantCheckmark entry = new EntryParticipantCheckmark(currentParticipant.name,
                                                                            currentParticipant.emailAddress,
                                                                            i == getApp().getModel().ownId);
            panelParticipants.add(entry);
            i++;
        }
        
        // Add elements and actions if automatic processing is enabled
        if (isAutomaticProcessingEnabled()) {
            
            // Start import reading e-mails automatically if enabled 
            startAutomatedMailImport();
            
            // Set message accordingly
            getApp().setStatusMessage(Resources.getString("PerspectiveReceive.LoadingInProgress"), false, true);
        }
        
        // Hide or show button to receive automatically
        updateButtonsPane(isAutomaticProcessingEnabled());
        
        // Update GUI
        this.stateChanged(new ChangeEvent(this));
        getPanel().revalidate();
        getPanel().repaint(); 
    }
    
    /**
     * Draws the buttons pane with or without the poll button to receive e-mail automatically
     * 
     * @param automaticProcessingEnabled
     */
    private void updateButtonsPane(boolean showPollManually) {
        // Remove
        this.buttonsPane.removeAll();
        
        // Create with two or three rows and add resend button if necessary
        if (showPollManually) {
            this.buttonsPane.setLayout(new GridLayout(3, 1));
            this.buttonsPane.add(this.buttonPollManually, 0, 0);
            this.buttonsPane.add(this.buttonReceive, 0, 1);
            this.buttonsPane.add(this.buttonProceed, 0, 2);
            
        } else {
            this.buttonsPane.setLayout(new GridLayout(2, 1));
            this.buttonsPane.add(this.buttonReceive, 0, 0);
            this.buttonsPane.add(this.buttonProceed, 0, 1);
        }
    }
}
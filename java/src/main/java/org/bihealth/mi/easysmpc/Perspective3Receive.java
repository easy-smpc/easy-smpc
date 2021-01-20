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
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.bihealth.mi.easysmpc.components.ComponentTextField;
import org.bihealth.mi.easysmpc.components.EntryParticipantCheckmark;
import org.bihealth.mi.easysmpc.dataimport.ImportClipboard;
import org.bihealth.mi.easysmpc.resources.Resources;

import de.tu_darmstadt.cbs.emailsmpc.AppState;
import de.tu_darmstadt.cbs.emailsmpc.Bin;
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
    
    /** Proceed button */
    private JButton            proceed;


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
     
    /**
     * Reacts on all changes in any components
     */
    @Override
    public void stateChanged(ChangeEvent e) {
        checkmarkParticipantEntries();
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
                                                                (getApp().getModel().state != AppState.RECIEVING_RESULT  &&  i == 0) || //Mark first entry in first round as received
                                                                areSharesCompleteForParticipantId(i)); //Mark if share complete
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
        this.participants = new JPanel();
        this.participants.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED),
                                                                     Resources.getString("PerspectiveReceive.participants"),
                                                                     TitledBorder.LEFT,
                                                                     TitledBorder.DEFAULT_POSITION));
        this.participants.setLayout(new BoxLayout(this.participants, BoxLayout.Y_AXIS));
        JScrollPane pane = new JScrollPane(participants, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        panel.add(pane, BorderLayout.CENTER);
        
        
        // Receive button and save button
        JPanel buttonsPane = new JPanel();
        buttonsPane.setLayout(new GridLayout(2, 1));
        JButton receive = new JButton(Resources.getString("PerspectiveReceive.receive"));
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
        // Update GUI
        this.stateChanged(new ChangeEvent(this));
        getPanel().revalidate();
        getPanel().repaint(); 
    }
}
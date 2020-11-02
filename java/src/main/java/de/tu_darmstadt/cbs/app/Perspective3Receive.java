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
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import de.tu_darmstadt.cbs.app.components.ComponentTextField;
import de.tu_darmstadt.cbs.app.components.EntryParticipantEnterExchangeString;
import de.tu_darmstadt.cbs.app.resources.Resources;
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
    
    /** Save button */
    private JButton            save;

    /**
     * Creates the perspective
     * @param app
     */
    protected Perspective3Receive(App app) {
        super(app, Resources.getString("PerspectiveReceive.receive"), 3); //$NON-NLS-1$
    }

    /**
     * Creates the perspective
     * @param app
     * @param progress
     */
    protected Perspective3Receive(App app, int progress) {
        super(app, Resources.getString("PerspectiveReceive.receive"), progress); //$NON-NLS-1$
    }
    
    /**
     * Creates the perspective
     * @param app
     * @param progress
     */
    protected Perspective3Receive(App app, String title , int progress) {
        super(app, title, progress); //$NON-NLS-1$
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        EntryParticipantEnterExchangeString entry = (EntryParticipantEnterExchangeString) e.getSource();
        int index = Arrays.asList(participants.getComponents()).indexOf(entry);
        if (getApp().actionReceiveMessage(index)) {
            this.stateChanged(new ChangeEvent(this));
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
        for (Bin b : getApp().getModel().bins) {
            if (!b.isComplete()) return false;
        }
        return true;
    }

    /**
     * Save the project
     * 
     */
    protected void actionSave() {
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
        JScrollPane pane = new JScrollPane(participants);
        pane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        panel.add(pane, BorderLayout.CENTER);
           
        // save button
        save = new JButton(Resources.getString("PerspectiveReceive.save"));
        save.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                actionSave();
            }
        });
        panel.add(save, BorderLayout.SOUTH);
    }

    /**
     * Initialize perspective based on model
     */
    @Override
    protected void initialize() {
        this.title.setText(getApp().getModel().name);
        this.participants.removeAll();
        int i = 0; 
        for (Participant currentParticipant : getApp().getModel().participants) {
            
            //Only set buttons when not the actual user and not for participant to initiator in first round
            EntryParticipantEnterExchangeString entry = new EntryParticipantEnterExchangeString(currentParticipant.name, 
                                                                        currentParticipant.emailAddress,
                                                                        i != getApp().getModel().ownId && !( i == 0 && getApp().getModel().state == AppState.RECIEVING_SHARE));
            entry.setButtonListener(this);
            i++;
            participants.add(entry);
        }
        this.stateChanged(new ChangeEvent(this));
    }
}

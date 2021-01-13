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

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

import de.tu_darmstadt.cbs.app.components.ComponentTextField;
import de.tu_darmstadt.cbs.app.components.EntryBinNoButton;
import de.tu_darmstadt.cbs.app.components.EntryParticipantNoButton;
import de.tu_darmstadt.cbs.app.resources.Resources;
import de.tu_darmstadt.cbs.emailsmpc.BinResult;
import de.tu_darmstadt.cbs.emailsmpc.Participant;

/**
 * A perspective
 * 
 * @author Fabian Prasser
 * @author Felix Wirth
 */
public class Perspective6Result extends Perspective {

    /** Panel for participants */
    private JPanel             participants;
    
    /** Panel for bins */
    private JPanel             bins;
    
    /** Text field containing title of study */
    private ComponentTextField title;    
    
    /**
     * Creates the perspective
     * @param app
     */
    protected Perspective6Result(App app) {
        super(app, Resources.getString("PerspectiveFinalize.0"), 6, false); //$NON-NLS-1$
    }
    
    /**
     * Initialize perspective based on model
     */
    @Override
    public void initialize() {
        participants.removeAll();
        bins.removeAll();
        this.title.setText(getApp().getModel().name);
        for (Participant currentParticipant : getApp().getModel().participants) {
            participants.add(new EntryParticipantNoButton(currentParticipant.name, currentParticipant.emailAddress));
        }
        for (BinResult binResult : getApp().getModel().getAllResults()) {
            bins.add(new EntryBinNoButton(binResult.name, binResult.value.toString()));
        }
        participants.revalidate();
        participants.repaint();
        bins.revalidate();
        bins.repaint();
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
        
        this.title = new ComponentTextField(null); // No validation
        this.title.setEnabled(false);
        title.add(this.title, BorderLayout.CENTER);
        
        // Central panel
        JPanel central = new JPanel();
        central.setLayout(new GridLayout(2, 1));
        panel.add(central, BorderLayout.CENTER);        
        
        // Participants
        this.participants = new JPanel();
        this.participants.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED),
                                                                     Resources.getString("PerspectiveParticipate.participants"),
                                                                     TitledBorder.LEFT,
                                                                     TitledBorder.DEFAULT_POSITION));
        this.participants.setLayout(new BoxLayout(this.participants, BoxLayout.Y_AXIS));
        JScrollPane pane = new JScrollPane(participants);
        central.add(pane, BorderLayout.NORTH);    
                        
        // Bins
        this.bins = new JPanel();
        this.bins.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED),
                                                             Resources.getString("PerspectiveParticipate.bins"),
                                                             TitledBorder.LEFT,
                                                             TitledBorder.DEFAULT_POSITION));
        this.bins.setLayout(new BoxLayout(this.bins, BoxLayout.Y_AXIS));
        pane = new JScrollPane(bins);
        central.add(pane, BorderLayout.SOUTH);
    }   
}

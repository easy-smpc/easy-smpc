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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

import org.bihealth.mi.easysmpc.components.ComponentTextField;
import org.bihealth.mi.easysmpc.components.EntryBinNoButton;
import org.bihealth.mi.easysmpc.components.EntryParticipantNoButton;
import org.bihealth.mi.easysmpc.components.ScrollablePanel;
import org.bihealth.mi.easysmpc.resources.Resources;

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
    private ScrollablePanel    panelParticipants;

    /** Panel for bins */
    private JPanel             panelBins;

    /** Text field containing title of study */
    private ComponentTextField fieldTitle;

    /** Export data button */
    private JButton            buttonExport;

    /**
     * Creates the perspective
     * @param app
     */
    protected Perspective6Result(App app) {
        super(app, Resources.getString("PerspectiveResult.0"), 6, false); //$NON-NLS-1$
    }
    
    /**
     * 
     * @param Exports data
     */
    protected void actionExportData() {
        // Create list from bins
        List<List<String>> list = new ArrayList<>();
        for (Component c : panelBins.getComponents()) {
            list.add(new ArrayList<String>(Arrays.asList(((EntryBinNoButton) c).getLeftValue(),
                                                         ((EntryBinNoButton) c).getRightValue())));
        }
        
        // Export data
        getApp().exportData(list);
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
        
        this.fieldTitle = new ComponentTextField(null); // No validation
        this.fieldTitle.setEnabled(false);
        title.add(this.fieldTitle, BorderLayout.CENTER);
        
        // Central panel
        JPanel central = new JPanel();
        central.setLayout(new GridLayout(2, 1));
        panel.add(central, BorderLayout.CENTER);        
        
        // Participants
        this.panelParticipants = new ScrollablePanel();
        this.panelParticipants.setLayout(new BoxLayout(this.panelParticipants, BoxLayout.Y_AXIS));
        JScrollPane pane = new JScrollPane(panelParticipants, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        pane.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED),
                                                                           Resources.getString("PerspectiveParticipate.participants"),
                                                                           TitledBorder.LEFT,
                                                                           TitledBorder.DEFAULT_POSITION));
        central.add(pane, BorderLayout.NORTH);    
                        
        // Bins
        this.panelBins = new JPanel();
        this.panelBins.setLayout(new BoxLayout(this.panelBins, BoxLayout.Y_AXIS));
        pane = new JScrollPane(panelBins, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        pane.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED),
                                                             Resources.getString("PerspectiveParticipate.bins"),
                                                             TitledBorder.LEFT,
                                                             TitledBorder.DEFAULT_POSITION));

        central.add(pane, BorderLayout.SOUTH);
        
        // Export button
        JPanel buttonsPane = new JPanel();
        buttonsPane.setLayout(new BorderLayout());
        buttonExport = new JButton(Resources.getString("PerspectiveResult.1"));
        buttonExport.addActionListener(new ActionListener() {            
            @Override
            public void actionPerformed(ActionEvent e) {
                actionExportData();
            }
        });
        buttonsPane.add(buttonExport, BorderLayout.CENTER);        
        panel.add(buttonsPane, BorderLayout.SOUTH);
    }

    /**
     * Initialize perspective based on model
     */
    @Override
    public void initialize() {
        // Remove previous content
        panelParticipants.removeAll();
        panelBins.removeAll();
        
        // Set new content
        this.fieldTitle.setText(getApp().getModel().getName());
        int i = 0;
        for (Participant currentParticipant : getApp().getModel().getParticipants()) {
            panelParticipants.add(new EntryParticipantNoButton(currentParticipant.name, currentParticipant.emailAddress, i == getApp().getModel().getOwnId()));
            i++;
        }
        for (BinResult binResult : getApp().getModel().getAllResults()) {
            panelBins.add(new EntryBinNoButton(binResult.name, binResult.value.toString()));
        }
        
        // Update GUI
        getPanel().revalidate();
        getPanel().repaint(); 
    }
}

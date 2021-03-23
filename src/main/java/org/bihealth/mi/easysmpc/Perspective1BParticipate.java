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
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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

import org.bihealth.mi.easysmpc.components.ComponentTextField;
import org.bihealth.mi.easysmpc.components.EntryBin;
import org.bihealth.mi.easysmpc.components.EntryParticipant;
import org.bihealth.mi.easysmpc.components.ScrollablePanel;
import org.bihealth.mi.easysmpc.resources.Resources;

import de.tu_darmstadt.cbs.emailsmpc.Bin;
import de.tu_darmstadt.cbs.emailsmpc.Participant;

/**
 * A perspective
 * 
 * @author Fabian Prasser
 * @author Felix Wirth
 */
public class Perspective1BParticipate extends Perspective implements ChangeListener {

    /** Panel for participants */
    private ScrollablePanel    panelParticipants;

    /** Panel for bins */
    private ScrollablePanel    panelBins;

    /** Text field containing title of study */
    private ComponentTextField fieldTitle;

    /** Save button */
    private JButton            buttonSave;

    /** Central panel */
    private JPanel             panelCentral;

    /**
     * Creates the perspective
     * @param app
     */
    protected Perspective1BParticipate(App app) {
        super(app, Resources.getString("PerspectiveParticipate.participate"), 1, false); //$NON-NLS-1$
    }    
    
    /**
     * Reacts on all changes in any components
     */
    @Override
    public void stateChanged(ChangeEvent e) {
        this.buttonSave.setEnabled(this.areValuesValid());
    }

    /**
     * Loads and sets bin names and data from a file
     */
    private void actionLoadFromFile() {
        Map<String, String> data = getApp().getDataFromFile();
        List<String> binsWithoutValue = new ArrayList<>();
        if (data != null) {
            for (Component c : this.panelBins.getComponents()) {
                String value = data.get(((EntryBin) c).getLeftValue());
                if (value == null) {
                    binsWithoutValue.add(((EntryBin) c).getLeftValue());
                }
                ((EntryBin) c).setRightValue(value);
            }
            
            // Error message if a value for at least one bin was not found
            if (binsWithoutValue.size() > 0){                
                JOptionPane.showMessageDialog(getPanel(),
                                              String.format(Resources.getString("PerspectiveParticipate.BinsWithoutValues"),
                                                            binsWithoutValue.toString().substring(1, binsWithoutValue.toString().length() - 1)),
                                              Resources.getString("App.11"),
                                              JOptionPane.ERROR_MESSAGE);
            }            
            this.stateChanged(new ChangeEvent(this));
        }
    }

    /**
     * Save the project
     * 
     * @return Saving actually performed?
     */
    private void actionSave() {
        BigInteger[] secret = new BigInteger[getApp().getModel().bins.length];
        for (int i = 0; i < this.panelBins.getComponents().length; i++) {
            secret[i] = new BigInteger(((EntryBin) this.panelBins.getComponents()[i]).getRightValue());
        }
        getApp().actionParticipateDone(secret);
    }

    /**
     * Checks bins for validity
     * @return
     */
    private boolean areValuesValid() {
        // Check bins
        for (Component c : this.panelBins.getComponents()) {
            if (!((EntryBin) c).isFieldRightValueValid()) { 
                return false; 
            }
        }        
        // Done
        return true;
    }

    /**
     *Creates and adds UI elements
     */
    @Override
    protected void createContents(JPanel panel) {

        // Layout
        panel.setLayout(new BorderLayout());

        // Study title
        JPanel titlePanel = new JPanel();
        panel.add(titlePanel, BorderLayout.NORTH);
        titlePanel.setLayout(new BorderLayout(Resources.ROW_GAP, Resources.ROW_GAP));
        titlePanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED),
                                                         Resources.getString("PerspectiveCreate.studyTitle"),
                                                         TitledBorder.LEFT,
                                                         TitledBorder.DEFAULT_POSITION));
        this.fieldTitle = new ComponentTextField(null); //no validation
        this.fieldTitle.setEnabled(false);
        titlePanel.add(this.fieldTitle, BorderLayout.CENTER);
        
        // Central panel
        panelCentral = new JPanel();
        panelCentral.setLayout(new GridLayout(2, 1));
        panel.add(panelCentral, BorderLayout.CENTER);        
        
        // Participants
        this.panelParticipants = new ScrollablePanel();
        this.panelParticipants.setLayout(new BoxLayout(this.panelParticipants, BoxLayout.Y_AXIS));
        JScrollPane pane = new JScrollPane(panelParticipants, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        pane.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED),
                                                        Resources.getString("PerspectiveParticipate.participants"),
                                                        TitledBorder.LEFT,
                                                        TitledBorder.DEFAULT_POSITION));
        panelCentral.add(pane, BorderLayout.NORTH);    
                        
        // Bins
        this.panelBins = new ScrollablePanel();
        this.panelBins.setLayout(new BoxLayout(this.panelBins, BoxLayout.Y_AXIS));
        pane = new JScrollPane(panelBins, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        pane.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED),
                                                        Resources.getString("PerspectiveParticipate.bins"),
                                                        TitledBorder.LEFT,
                                                        TitledBorder.DEFAULT_POSITION));
        panelCentral.add(pane, BorderLayout.SOUTH);

        // Buttons pane
        JPanel buttonsPane = new JPanel();
        buttonsPane.setLayout(new GridLayout(2, 1));
        
        // Load from file button      
        JButton loadFromFile = new JButton(Resources.getString("PerspectiveCreate.LoadFromFile"));
        loadFromFile.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                actionLoadFromFile();
            }
        });
        buttonsPane.add(loadFromFile, 0, 0);   

        // Save button
        buttonSave = new JButton(Resources.getString("PerspectiveParticipate.save"));
        buttonSave.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                actionSave();
            }
        });
        buttonsPane.add(buttonSave, 0, 1);
        panel.add(buttonsPane, BorderLayout.SOUTH);
    }

    /**
     * Initialize perspective based on model
     */
    @Override
    protected void initialize() {

        // Clear
        panelParticipants.removeAll();
        panelBins.removeAll();
        
        // Title
        this.fieldTitle.setText(getApp().getModel().name);
        
        // Add participants
        for (Participant currentParticipant : getApp().getModel().participants) {
            EntryParticipant newNameEmailParticipantEntry = new EntryParticipant(currentParticipant.name, currentParticipant.emailAddress, false, false);
            panelParticipants.add(newNameEmailParticipantEntry);
        }
        for (Bin currentBin : getApp().getModel().bins) {
            EntryBin newBin = new EntryBin(currentBin.name, false, "", true, false);
            newBin.setChangeListener(this);
            panelBins.add(newBin);
        }     
        // Update GUI
        this.stateChanged(new ChangeEvent(this));
        getPanel().revalidate();
        getPanel().repaint();        
    }
    
    @Override
    protected void uninitialize() {
        // Empty by design  
    }
}

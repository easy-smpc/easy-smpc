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
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.math.BigInteger;
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

import de.tu_darmstadt.cbs.app.components.ComponentTextField;
import de.tu_darmstadt.cbs.app.components.EntryBin;
import de.tu_darmstadt.cbs.app.components.EntryParticipant;
import de.tu_darmstadt.cbs.app.resources.Resources;
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
    private JPanel             participants;
    
    /** Panel for bins */
    private JPanel             bins;
    
    /** Text field containing title of study */
    private ComponentTextField title;
    
    /** Save button */
    private JButton            save;
    
    /** Central panel */
    private JPanel central;
    
    /** Is interim saving in this perspective possible */
    private final boolean      interimSavingPossible = false;

    /**
     * Creates the perspective
     * @param app
     */
    protected Perspective1BParticipate(App app) {
        super(app, Resources.getString("PerspectiveParticipate.participate"), 1); //$NON-NLS-1$
    }
    
    @Override
    protected boolean isInterimSavingPossible() {
        return interimSavingPossible;
    }
    
    /**
     * Reacts on all changes in any components
     */
    @Override
    public void stateChanged(ChangeEvent e) {
        this.save.setEnabled(this.areValuesValid());
    }

    /**
     * Save the project
     * 
     * @return Saving actually performed?
     */
    private void actionSave() {
        BigInteger[] secret = new BigInteger[getApp().getModel().bins.length];
        for (int i = 0; i < this.bins.getComponents().length; i++) {
            secret[i] = new BigInteger(((EntryBin) this.bins.getComponents()[i]).getRightValue());
        }
        getApp().actionParticipateDone(secret);
    }
    
    /**
     * Loads and sets bin names and data from an Excel-file
     */
    private void actionLoadExcel() {
        setBinValues(getApp().getExcelData());           
    }
    
    /**
     * Loads and sets bin names and data from an CSV-file
     */
    private void actionLoadCSV() {
        setBinValues(getApp().getCSVData());           
    }
    
    /**
     * Sets bin values
     */
    private void setBinValues(Map<String,String> data ) {
        // TODO remove header line
        if (data != null) {
            if (data.size() == this.bins.getComponentCount()) {
                int index = 0;
                if (data != null) {
                    for (Component c : this.bins.getComponents()) {
                        String value = data.get(((EntryBin) c).getLeftValue());
                        if (value == null) {
                            value = (String) data.values().toArray()[index];
                            
                        }
                        ((EntryBin) c).setRightValue(value);
                        index++;
                    }
                }
                this.stateChanged(new ChangeEvent(this));
            } else {
                JOptionPane.showMessageDialog(getPanel(),
                                              Resources.getString("PerspectiveCreate.BinsLengthUnequalData"), //$NON-NLS-1$
                                              Resources.getString("App.11"),
                                              JOptionPane.ERROR_MESSAGE);
            }
        } 
    }

    /**
     * Checks bins for validity
     * @return
     */
    private boolean areValuesValid() {
        // Check bins
        for (Component c : this.bins.getComponents()) {
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
        central = new JPanel();
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
        pane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        central.add(pane, BorderLayout.NORTH);    
                        
        // Bins
        this.bins = new JPanel();
        this.bins.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED),
                                                             Resources.getString("PerspectiveParticipate.bins"),
                                                             TitledBorder.LEFT,
                                                             TitledBorder.DEFAULT_POSITION));
        this.bins.setLayout(new BoxLayout(this.bins, BoxLayout.Y_AXIS));
        pane = new JScrollPane(bins);
        pane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        central.add(pane, BorderLayout.SOUTH);
           
        
        // Load csv button
        JPanel loadbuttonsPane = new JPanel();
        loadbuttonsPane.setLayout(new GridLayout(1, 2));
        JButton loadCSV = new JButton(Resources.getString("PerspectiveCreate.loadCSVFile"));
        loadCSV.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                actionLoadCSV();
            }

        });
        loadbuttonsPane.add(loadCSV, 0, 0); 
        
        JPanel buttonsPane = new JPanel();
        buttonsPane.setLayout(new GridLayout(2, 1));
        
        // Load excel button
        JButton loadExcel = new JButton(Resources.getString("PerspectiveCreate.loadExcelFile"));
        loadExcel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                actionLoadExcel();
            }
        });
        loadbuttonsPane.add(loadExcel, 1, 0);        
        buttonsPane.add(loadbuttonsPane, 0, 0);

        // Save button
        save = new JButton(Resources.getString("PerspectiveParticipate.save"));
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

        // Clear
        participants.removeAll();
        bins.removeAll();
        
        // Title
        this.title.setText(getApp().getModel().name);
        
        // Add participants
        for (Participant currentParticipant : getApp().getModel().participants) {
            EntryParticipant newNameEmailParticipantEntry = new EntryParticipant(currentParticipant.name, currentParticipant.emailAddress, false, false);
            participants.add(newNameEmailParticipantEntry);
        }
        for (Bin currentBin : getApp().getModel().bins) {
            EntryBin newBin = new EntryBin(currentBin.name, false, "", true, false);
            newBin.setChangeListener(this);
            bins.add(newBin);
        }
        
        // Zpdate
        this.stateChanged(new ChangeEvent(this));
        
        // Redraw
        participants.revalidate();
        participants.repaint();
        bins.revalidate();
        bins.repaint();
    }
    
}

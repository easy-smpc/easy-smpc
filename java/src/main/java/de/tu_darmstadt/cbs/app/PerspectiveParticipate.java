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
import java.io.File;
import java.io.IOException;
import java.math.BigInteger;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import de.tu_darmstadt.cbs.app.components.ComponentTextField;
import de.tu_darmstadt.cbs.app.components.ComponentTextFieldValidator;
import de.tu_darmstadt.cbs.app.components.EntryBin;
import de.tu_darmstadt.cbs.app.components.EntryParticipant;
import de.tu_darmstadt.cbs.app.components.ExchangeStringPicker;
import de.tu_darmstadt.cbs.emailsmpc.Bin;
import de.tu_darmstadt.cbs.emailsmpc.Message;
import de.tu_darmstadt.cbs.emailsmpc.Participant;

/**
 * A perspective
 * 
 * @author Fabian Prasser
 */

public class PerspectiveParticipate extends Perspective implements ChangeListener {

    /** Panel for participants */
    private JPanel             participants;
    /** Panel for bins */
    private JPanel             bins;
    /** Text field containing title of study */
    private ComponentTextField title;
    /** enterExchangeString button */
    private JButton            enterExchangeStringButton;
    /** Save button */
    private JButton            save;


    /**
     * Creates the perspective
     * @param app
     */
    protected PerspectiveParticipate(App app) {
        super(app, Resources.getString("PerspectiveParticipate.participate")); //$NON-NLS-1$
    }

    /**
     * Reacts on all changes in any components
     */
    @Override
    public void stateChanged(ChangeEvent e) {
        this.save.setEnabled(this.areValuesValid());
    }

    /**
     * Sets data for a participant derived from the string entered
     */
    public void setDataForParticipant() {
            participants.removeAll();
            bins.removeAll();
            this.title.setText(SMPCServices.getServicesSMPC().getAppModel().name);            
            for (Participant currentParticipant : SMPCServices.getServicesSMPC()
                                                              .getAppModel().participants) {
                EntryParticipant newNameEmailParticipantEntry = new EntryParticipant(currentParticipant.name,
                                                                                                       currentParticipant.emailAddress,
                                                                                                       false);
                participants.add(newNameEmailParticipantEntry);
            }
            for (Bin currentBin : SMPCServices.getServicesSMPC().getAppModel().bins) {
                EntryBin newBin = new EntryBin(currentBin.name, false, true, false);
                newBin.setChangeListener(this);
                bins.add(newBin);
            }
            this.stateChanged(new ChangeEvent(this));
            participants.revalidate();
            participants.repaint();
            bins.revalidate();
            bins.repaint();        
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
     * Save the project
     * 
     * @return Saving actually performed?
     */
    private void save() {
        // File
        File file = null;
        JFileChooser fileChooser = new JFileChooser();
        if (fileChooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
            file = fileChooser.getSelectedFile();
        }
        
        // Check
        if (file == null) {
            return;
        }
        
      BigInteger[] secretValuesofParticipant = new BigInteger[SMPCServices.getServicesSMPC()
                                                                         .getAppModel().bins.length];
      Integer i = 0;
      for(Component currentBinEntry: this.bins.getComponents())
      {
          
          secretValuesofParticipant[i] = new BigInteger(( (EntryBin) currentBinEntry).getRightValue());
          i++;
      }
      SMPCServices.getServicesSMPC().getAppModel().toSendingShares(secretValuesofParticipant);
        // Try to save file
        try {
            SMPCServices.getServicesSMPC().getAppModel().filename = file;
            SMPCServices.getServicesSMPC().getAppModel().saveProgram();
            ((PerspectiveSend) this.getApp().getPerspective(PerspectiveSend.class)).setDataAndShowPerspective();
            this.getApp().showPerspective(PerspectiveSend.class);

        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, Resources.getString("PerspectiveParticipate.saveError") + e.getMessage());
        }
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
                                                                     Resources.getString("PerspectiveParticipate.participants"),
                                                                     TitledBorder.LEFT,
                                                                     TitledBorder.DEFAULT_POSITION));
        this.participants.setLayout(new BoxLayout(this.participants, BoxLayout.Y_AXIS));
        JScrollPane pane = new JScrollPane(participants);
        pane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        central.add(pane, BorderLayout.NORTH);    
                        
        // ------
        // Bins
        // ------
        this.bins = new JPanel();
        this.bins.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED),
                                                             Resources.getString("PerspectiveParticipate.bins"),
                                                             TitledBorder.LEFT,
                                                             TitledBorder.DEFAULT_POSITION));
        this.bins.setLayout(new BoxLayout(this.bins, BoxLayout.Y_AXIS));
        pane = new JScrollPane(bins);
        pane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        central.add(pane, BorderLayout.SOUTH);
           
        // ------
        // EnterExchangeString button and save button
        // ------
        JPanel buttonsPane = new JPanel();
        buttonsPane.setLayout(new GridLayout(2, 1));
        enterExchangeStringButton = new JButton(Resources.getString("PerspectiveParticipate.enterExchangeString"));
        enterExchangeStringButton.addActionListener(new ActionListener() {            
            @Override
            public void actionPerformed(ActionEvent e) {
                if (new ExchangeStringPicker(new ComponentTextFieldValidator() {
                    @Override
                    public boolean validate(String text) {
                        return SMPCServices.getServicesSMPC().isInitialParticipationMessageValid(text);
                    }
                }, central).showDialog()) {
                    PerspectiveParticipate.this.setDataForParticipant();
                }
            }
        });
        
        buttonsPane.add(enterExchangeStringButton, 0,0);
        save = new JButton(Resources.getString("PerspectiveParticipate.save"));
        save.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                save();
            }
        });
        save.setEnabled(false);
        buttonsPane.add(save, 0,1);
        panel.add(buttonsPane, BorderLayout.SOUTH);
    }
}

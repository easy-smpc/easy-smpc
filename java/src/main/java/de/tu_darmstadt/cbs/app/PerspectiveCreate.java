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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
import de.tu_darmstadt.cbs.emailsmpc.Bin;
import de.tu_darmstadt.cbs.emailsmpc.Participant;

/**
 * A perspective
 * 
 * @author Fabian Prasser
 */

public class PerspectiveCreate extends Perspective implements ChangeListener {

    /** Panel for participants */
    private JPanel             participants;
    /** Panel for bins */
    private JPanel             bins;
    /** Text field containing title of study */
    private ComponentTextField title;
    /** Save button */
    private JButton            save;
//    /** Text field containing data entered by a participant (which was received by user) */
//    private JTextArea  participantDumpedData;
//    /** Swing box for participantDumpedData */
//    private Box        boxParticipantDumpedData;

    /**
     * Creates the perspective
     * @param app
     */
    protected PerspectiveCreate(App app) {
        super(app, Resources.getString("PerspectiveCreate.0")); //$NON-NLS-1$
    }

    /**
     * Reacts on all changes in any components
     */
    public void stateChanged(ChangeEvent e) {
        this.save.setEnabled(this.areValuesValid());
    }

//    /**
//     * Sets data for a participant derived from the string entered
//     */
//    protected void setDataForParticipant() {
//        try {
//            SMPCServices.getServicesSMPC()
//                        .initalizeAsNewStudyParticipation(this.participantDumpedData.getText());
//            participantsBox.removeAll();
//            binsBox.removeAll();
//            this.studyTitle.setText(SMPCServices.getServicesSMPC().getAppModel().name);
//            this.participantDumpedData.setBorder(BorderFactory.createEmptyBorder());
//            for (Participant currentParticipant : SMPCServices.getServicesSMPC()
//                                                              .getAppModel().participants) {
//                EntryParticipant newNameEmailParticipantEntry = new EntryParticipant(currentParticipant.name,
//                                                                                                       currentParticipant.emailAddress,
//                                                                                                       false);
//                participantsBox.add(newNameEmailParticipantEntry);
//
//            }
//            // TODO: Commented this out
//            // Set default for radio button isCurrentParticipant
//            // ((NameEmailParticipantEntry) participantsBox.getComponent(0)).getIsCurrentParticipantRadioButton().setSelected(false);
//            // ((NameEmailParticipantEntry) participantsBox.getComponent(0)).getIsCurrentParticipantRadioButton().setEnabled(false);
//            // ((NameEmailParticipantEntry) participantsBox.getComponent(1)).getIsCurrentParticipantRadioButton().setSelected(true);
//            // ((NameEmailParticipantEntry) participantsBox.getComponent(1)).getIsCurrentParticipantRadioButton().setText(Resources.getString("Participant.2"));
//            // SMPCServices.getServicesSMPC().getAppModel().numberOwnPartcipation = 1;
//            for (Bin currentBin : SMPCServices.getServicesSMPC().getAppModel().bins) {
//                binsBox.add(new EntryBin(currentBin.name, false));
//            }
//            this.saveButton.setEnabled(true);
//            participantsBox.revalidate();
//            participantsBox.repaint();
//            binsBox.revalidate();
//            binsBox.repaint();
//        } catch (IllegalArgumentException e) {
//            this.participantDumpedData.setBorder(BorderFactory.createLineBorder(Color.RED));
//            this.participantDumpedData.revalidate();
//            this.participantDumpedData.repaint();
//            this.saveButton.setEnabled(false);   
//        }
//    }
//    
//    /**
//     * Reads the entered data and processes it accordingly to participate in a study
//     * 
//     * @param boxBins
//     * @param selectedFile
//     */
//    /**
//     * 
//     */
//    public void digestDataAsStudyParticipation() {
//        BigInteger[] secretValuesofParticipant = new BigInteger[SMPCServices.getServicesSMPC()
//                                                                           .getAppModel().bins.length];
//        Integer i = 0;
//        for(EntryBin currentBinEntry:getBinEntries())
//        {
//            secretValuesofParticipant[i] = new BigInteger( currentBinEntry.getRightValue());
//            i++;
//        }
//
//        SMPCServices.getServicesSMPC().getAppModel().toSendingShares(secretValuesofParticipant);
//    }

//
//    /**
//     * Prepare perspective for study participation
//     */
//    public void setStudyParticipation() {
//        this.setParticipating(true);
//        this.boxParticipantDumpedData.setVisible(true);
//        this.studyTitle.setEnabled(false);
//        this.buttonPlusParticipant.setEnabled(false);
//        this.buttonMinusParticipant.setEnabled(false);
//        this.buttonPlusBin.setEnabled(false);
//        this.buttonMinusBin.setEnabled(false);
//        this.participantsBox.removeAll();
//        this.binsBox.removeAll();
//        this.addParticipanLine(false);
//        this.addBinLine(false);                                         
//        this.boxParticipantDumpedData.revalidate();
//        this.boxParticipantDumpedData.repaint();
//    }
    
//  // -------------------
//  // -------------------
//  // Validate
//  // -------------------
//  // -------------------
//  for (EntryParticipant currentNameEmailParticipantEntry:this.getNameEmailParticipantEntries()) {
//      if(!currentNameEmailParticipantEntry.areValuesValid())
//      {
//          JOptionPane.showMessageDialog(null, Resources.getString("PerspectiveCreate.errorValidateParticipants"));
//          dataValid = false;
//          break;
//      }
//  }

    /**
     * Adds a new line for bin entry
     * @param enabled
     */
    private void addBin(EntryBin previous, boolean enabled) {

        // Find index
        int index = Arrays.asList(this.bins.getComponents()).indexOf(previous);
        index = index == -1 ? 0 : index + 1;
        
        // Create and add entry
        EntryBin entry = new EntryBin(enabled);
        entry.setChangeListener(this);
        entry.setAddListener(new ActionListener() {
           @Override
            public void actionPerformed(ActionEvent e) {
               addBin(entry, true);
            } 
        });
        entry.setRemoveListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                removeBin(entry);
            }
        });
        this.bins.add(entry, index);
        this.bins.revalidate();
        this.bins.repaint();
    }

    /**
     * Adds a new line for participant entry
     * @param previous
     * @param enabled
     */
    private void addParticipant(EntryParticipant previous, boolean enabled) {
        
        // Find index
        int index = Arrays.asList(this.participants.getComponents()).indexOf(previous);
        index = index == -1 ? 0 : index + 1;
        
        // Create and add entry
        EntryParticipant entry = new EntryParticipant("", "", enabled);
        entry.setChangeListener(this);
        entry.setAddListener(new ActionListener() {
           @Override
            public void actionPerformed(ActionEvent e) {
               addParticipant(entry, true);
            } 
        });
        entry.setRemoveListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                removeParticipant(entry);
            }
        });
        this.participants.add(entry, index);
        this.participants.revalidate();
        this.participants.repaint();
    }

    /**
     * Checks all values for validity
     * @return
     */
    private boolean areValuesValid() {
        
        // Check participants
        for (Component c : this.participants.getComponents()) {
            if (!((EntryParticipant) c).areValuesValid()) {
                return false;
            }
        }
        
        // Check bins
        for (Component c : this.bins.getComponents()) {
            if (!((EntryBin) c).areValuesValid()) { 
                return false; 
            }
        }
      
        // Check title
        if (!title.isValueValid()) {
            return false;
        }
        
        // Done
        return true;
    }

    /**
     * Removes a bin
     * @param entry
     */
    private void removeBin(EntryBin entry) {
        
        // Check whether it's the last entry
        if (this.bins.getComponentCount() == 1) {
            JOptionPane.showMessageDialog(null, Resources.getString("PerspectiveCreate.errorTooFewEntries"));
            return;
        }
        
        // Remove and update
        this.bins.remove(entry);
        this.bins.revalidate();
        this.bins.repaint();
    }

    /**
     * Removes a participant
     * @param entry
     */
    private void removeParticipant(EntryParticipant entry) {
        
        // Check whether it's the last entry
        if (this.participants.getComponentCount() == 1) {
            JOptionPane.showMessageDialog(null, Resources.getString("PerspectiveCreate.errorTooFewEntries"));
            return;
        }
        
        // Remove and update
        this.participants.remove(entry);
        this.participants.revalidate();    
        this.participants.repaint();
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
        
        // Collect participants
        List<Participant> participants = new ArrayList<>();
        for (Component entry : this.participants.getComponents()) {
            Participant participant = new Participant(((EntryParticipant)entry).getLeftValue(),
                                                      ((EntryParticipant)entry).getRightValue());
            participants.add(participant);
        }
        
        // Collect bins
        List<Bin> bins = new ArrayList<>();
        for (Component entry : this.bins.getComponents()) {
            Bin bin = new Bin(((EntryBin)entry).getLeftValue());
            bin.initialize(participants.size());
            bin.shareValue(new BigInteger(((EntryBin)entry).getRightValue()));
            bins.add(bin);
        }

        // Initialize 
        SMPCServices.getServicesSMPC().initalizeAsNewStudyCreation();

        // Pass over bins and participants
        SMPCServices.getServicesSMPC().getAppModel().toInitialSending(this.title.getText(),
                                                                      participants.toArray(new Participant[participants.size()]),
                                                                      bins.toArray(new Bin[bins.size()]));
        
        // Try to save file
        try {
            SMPCServices.getServicesSMPC().getAppModel().filename = file;
            SMPCServices.getServicesSMPC().getAppModel().saveProgram();
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, Resources.getString("PerspectiveCreate.saveError") + e.getMessage());
        }
    }

    /**
     *Creates and adds UI elements
     */
    @Override
    protected void createContents(JPanel panel) {

        // Layout
        panel.setLayout(new BorderLayout());
        
//        
//        // -------
//        // Participant data box (only shown when study participation is created
//        // -------
//        this.boxParticipantDumpedData = Box.createHorizontalBox();
//         
//        this.boxParticipantDumpedData.add(new JLabel(Resources.getString("PerspectiveCreate.dataReceived")));
//        this.participantDumpedData = new JTextArea();
//        this.participantDumpedData.setMaximumSize(new Dimension(Resources.SIZE_TEXTAREA_X,Resources.SIZE_TEXTAREA_Y));
//        this.participantDumpedData.setLineWrap(true);
//        // react to to changes in data box
//        this.participantDumpedData.getDocument().addDocumentListener(new DocumentListener() {
//            @Override
//            public void removeUpdate(DocumentEvent e) {
//                if (participantDumpedData.getText().isEmpty()) {
//                    participantsBox.removeAll();
//                    binsBox.removeAll();
//                    saveButton.setEnabled(false);
//                    addParticipanLine(false);
//                    addBinLine(false);
//                } else {
//                    setDataForParticipant();
//                }
//            }
//
//            @Override
//            public void insertUpdate(DocumentEvent e) {
//                setDataForParticipant();
//            }
//
//            @Override
//            public void changedUpdate(DocumentEvent e) {
//                // TODO Implement
//
//            }
//        });
//
//        boxParticipantDumpedData.add(this.participantDumpedData);
//        workingpanel.add(boxParticipantDumpedData);

        // -------
        // Name of study
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
                return !text.trim().isEmpty();
            }
        });
        this.title.setChangeListener(this);
        // TODO: Wie ohne fixed length auskommen? Oder zumindest die Arten Längen festzulegen harmonisieren                                                       
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
        this.addParticipant(null, true);

        // ------
        // Bins
        // ------
        this.bins = new JPanel();
        this.bins.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED),
                                                             Resources.getString("PerspectiveCreate.bins"),
                                                             TitledBorder.LEFT,
                                                             TitledBorder.DEFAULT_POSITION));
        this.bins.setLayout(new BoxLayout(this.bins, BoxLayout.Y_AXIS));
        pane = new JScrollPane(bins);
        pane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        central.add(pane, BorderLayout.SOUTH);
        this.addBin(null, true);
        
        // ------
        // Save button
        // ------
        save = new JButton(Resources.getString("PerspectiveCreate.save"));
        save.setEnabled(this.areValuesValid());
        save.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                save();
            }
        });
        panel.add(save, BorderLayout.SOUTH);
        
    }
}

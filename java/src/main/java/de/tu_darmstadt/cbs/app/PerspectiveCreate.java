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

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import de.tu_darmstadt.cbs.app.templates.BinEntry;
import de.tu_darmstadt.cbs.app.templates.NameEmailParticipantEntry;
import de.tu_darmstadt.cbs.emailsmpc.AppState;
import de.tu_darmstadt.cbs.emailsmpc.Bin;
import de.tu_darmstadt.cbs.emailsmpc.Participant;
import lombok.Getter;
import lombok.Setter;

/**
 * A perspective
 * 
 * @author Fabian Prasser
 */

public class PerspectiveCreate extends Perspective {
    
    /** Swing box containing participantsEntry (Name and E-Mail) */
    /**
     * Returns participantsBox
     * @return
     */
    @Getter
    private Box        participantsBox;
    /** Swing box containing binsEntry (Name and Data/Value for bin) */
    /**
     * Returns binsBox
     * @return
     */
    @Getter
    private Box        binsBox;
    /** Text field containing title of study */
    /**
     * Return studyTitle
     * @return
     */
    @Getter
    private JTextField studyTitle;
    /** Text field containing data entered by a participant (which was received by user) */
    /**
     * Returns participantDumpedData
     * @return
     */
    @Getter
    private JTextArea  participantDumpedData;
    /** Swing box for participantDumpedData */
    /**
     * Return boxParticipantDumpedData
     * @return
     */
    @Getter
    private Box        boxParticipantDumpedData;
    /** Button to add a participant entry line */
    private JButton    buttonPlusParticipant;
    /** Button to remove a participant entry line */
    private JButton    buttonMinusParticipant;
    /** Button to add a bin entry line */
    private JButton    buttonPlusBin;
    /** Button to remove a bin entry line */
    private JButton    buttonMinusBin;
    /** Boolean indicating whether perspective is currently for study creators or study participants */
    /**
     * Gets isParticipating
     * @return
     */
    @Getter
    /**
     * Sets isParticipating
     * @param isParticipating
     */
    @Setter
    boolean            isParticipating;
    /** Button to save study creation/participation */
    private JButton    saveButton;

    /**
     * Creates the perspective
     * @param app
     */
    protected PerspectiveCreate(App app) {
        super(app, Resources.getString("PerspectiveCreate.0")); //$NON-NLS-1$
    }

    /**
     *Creates and adds UI elements
     */
    @Override
    protected void createContents(JPanel panel) {

        // ------
        // Layout TODO: Improve Scroll-Layout
        // ------
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        JPanel workingpanel = new JPanel();
        workingpanel.setLayout(new BoxLayout(workingpanel, BoxLayout.Y_AXIS));
        JScrollPane scrollpane = new JScrollPane(workingpanel,
                                                 ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                                                 ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        panel.add(scrollpane);
        // -------
        // Participant data box (only shown when study participation is created
        // -------
        this.boxParticipantDumpedData = Box.createHorizontalBox();
         
        boxParticipantDumpedData.add(new JLabel(Resources.getString("PerspectiveCreate.dataReceived")));
        this.participantDumpedData = new JTextArea();
        this.participantDumpedData.setMaximumSize(new Dimension(Resources.SIZE_TEXTAREA_X,Resources.SIZE_TEXTAREA_Y));
        this.participantDumpedData.setLineWrap(true);
        // react to to changes in data box
        this.participantDumpedData.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void removeUpdate(DocumentEvent e) {
                if (participantDumpedData.getText().isEmpty()) {
                    participantsBox.removeAll();
                    binsBox.removeAll();
                    saveButton.setEnabled(false);
                    addParticipanLine(false);
                    addBinLine(false);
                } else {
                    setDataForParticipant();
                }
            }

            @Override
            public void insertUpdate(DocumentEvent e) {
                setDataForParticipant();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                // TODO Implement

            }
        });

        boxParticipantDumpedData.add(this.participantDumpedData);
        workingpanel.add(boxParticipantDumpedData);

        // -------
        // Name of study
        // -------
        Box box1 = Box.createHorizontalBox();
        box1.add(new JLabel(Resources.getString("PerspectiveCreate.studyTitle"))); 
        this.studyTitle = new JTextField();
        this.studyTitle.setMaximumSize(new Dimension(Resources.MAX_SIZE_TEXTFIELD_X, Resources.ROW_HEIGHT)); 
        // TODO: Wie ohne fixed length auskommen? Oder zumindest die Arten Längen festzulegen harmonisieren                                                       
        box1.add(studyTitle);
        workingpanel.add(box1);

        // ------
        // Participants
        // ------
        this.participantsBox = Box.createVerticalBox();
        workingpanel.add(participantsBox);

        // Buttons to add and remove lines for participants
        Box boxControlsParticipantLines = Box.createHorizontalBox();
        this.buttonPlusParticipant = new JButton("+");
        buttonPlusParticipant.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ((PerspectiveCreate) getApp().getPerspective(PerspectiveCreate.class)).addParticipanLine(true);
            }
        });
        this.buttonMinusParticipant = new JButton("-");
        buttonMinusParticipant.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ((PerspectiveCreate) getApp().getPerspective(PerspectiveCreate.class)).removeParticipanLine();
            }
        });
        boxControlsParticipantLines.add(buttonPlusParticipant);
        boxControlsParticipantLines.add(buttonMinusParticipant);
        workingpanel.add(boxControlsParticipantLines);

        // ------
        // Bins
        // ------

        this.binsBox = Box.createVerticalBox();
        workingpanel.add(binsBox);

        // Buttons to add and remove lines for bins
        Box boxControlsBinLines = Box.createHorizontalBox();
        this.buttonPlusBin = new JButton("+");
        buttonPlusBin.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ((PerspectiveCreate) getApp().getPerspective(PerspectiveCreate.class)).addBinLine(true);
            }
        });
        this.buttonMinusBin = new JButton("-");
        buttonMinusBin.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ((PerspectiveCreate) getApp().getPerspective(PerspectiveCreate.class)).removeBinLine();
                ;
            }
        });
        boxControlsBinLines.add(buttonPlusBin);
        boxControlsBinLines.add(buttonMinusBin);
        workingpanel.add(boxControlsBinLines);

        // ------
        // Save button
        // ------

        Box saveButtonBox = Box.createHorizontalBox();
        this.saveButton = new JButton(Resources.getString("PerspectiveCreate.save"));
        saveButton.setHorizontalAlignment(SwingConstants.RIGHT);
        saveButtonBox.add(saveButton);
        workingpanel.add(saveButtonBox);
    }

 

    /**
     *  Reads the entered data and processes it accordingly to create a study
     */
    public void digestDataAsNewStudyCreation() {
        ArrayList<Participant> participants = new ArrayList<>();
        ArrayList<Bin> bins = new ArrayList<>();
        SMPCServices.getServicesSMPC().initalizeAsNewStudyCreation();

        // Unmarshall participants
        for (int i = 0; i < participantsBox.getComponentCount(); i++) {
            NameEmailParticipantEntry currentNameEmailParticipant = (NameEmailParticipantEntry) participantsBox.getComponent(i);
            Participant participant = new Participant(currentNameEmailParticipant.getParticipantTextField()
                                                                                 .getText(),
                                                      currentNameEmailParticipant.getEmailTextField()
                                                                                 .getText());
            participants.add(participant);
        }

        // Unmarshall bins
        for (int i = 0; i < binsBox.getComponentCount(); i++) {

            BinEntry currentBinEntry = (BinEntry) binsBox.getComponent(i);

            Bin bin = new Bin(currentBinEntry.getBinNameTextField().getText());
            bin.initialize(participants.size());
            bin.shareValue(new BigInteger(currentBinEntry.getBinValueField().getText()));
            // TODO VORHER
            // bigint
            // Validation & und andere Validierungen (z.B. mindestens 3
            // participants)
            bins.add(bin);
        }

        SMPCServices.getServicesSMPC()
                    .getAppModel()
                    .toInitialSending(this.studyTitle.getText(),
                                      participants.toArray(new Participant[participants.size()]),
                                      bins.toArray(new Bin[bins.size()]));
    }
    
    /**
     * Sets data for a participant derived from the string entered
     */
    protected void setDataForParticipant() {
        boolean firstParticipant = true;
        try {
            SMPCServices.getServicesSMPC()
                        .initalizeAsNewStudyParticipation(participantDumpedData.getText());
            participantsBox.removeAll();
            binsBox.removeAll();
            this.studyTitle.setText(SMPCServices.getServicesSMPC().getAppModel().name);
            this.studyTitle.setEnabled(false);
            for (Participant currentParticipant : SMPCServices.getServicesSMPC()
                                                              .getAppModel().participants) {
                NameEmailParticipantEntry newNameEmailParticipantEntry = new NameEmailParticipantEntry(currentParticipant.name,
                                                                                                       currentParticipant.emailAddress,
                                                                                                       false);
                // First participant(=Initator) can not be selected as a
                // participant
                if (firstParticipant == true) {
                    newNameEmailParticipantEntry.getIsCurrentParticipantRadioButton()
                                                .setSelected(false);
                    newNameEmailParticipantEntry.getIsCurrentParticipantRadioButton()
                                                .setEnabled(false);
                    firstParticipant = false;
                }
                participantsBox.add(newNameEmailParticipantEntry);

            }
            for (Bin currentBin : SMPCServices.getServicesSMPC().getAppModel().bins) {
                binsBox.add(new BinEntry(currentBin.name, false));
            }
            this.saveButton.setEnabled(true);
            participantsBox.revalidate();
            participantsBox.repaint();
            binsBox.revalidate();
            binsBox.repaint();
        } catch (IllegalArgumentException e) {
            System.out.println("message invalid");
            this.saveButton.setEnabled(false);
        }
    }
    
    /**
     * Reads the entered data and processes it accordingly to participate in a study
     * 
     * @param boxBins
     * @param selectedFile
     */
    /**
     * 
     */
    public void digestDataAsStudyParticipation() {
        BigInteger[] secretValuesofParticipant = new BigInteger[SMPCServices.getServicesSMPC()
                                                                            .getAppModel().bins.length];

        for (int i = 0; i < this.binsBox.getComponentCount(); i++) {
            secretValuesofParticipant[i] = new BigInteger(((BinEntry) binsBox.getComponent(i)).getBinValueField()
                                                                                              .getText());
        }
        SMPCServices.getServicesSMPC().getAppModel().toSendingShares(secretValuesofParticipant);
    }
    
    /**
     * Prepare perspective for study creation
     */
    public void setStudyCreation() {
        this.setParticipating(false);
        this.boxParticipantDumpedData.setVisible(false);
        this.studyTitle.setText("");
        this.studyTitle.setEnabled(true);
        this.participantsBox.removeAll();
        this.binsBox.removeAll();
        this.buttonPlusParticipant.setEnabled(true);
        this.buttonMinusParticipant.setEnabled(true);
        this.buttonPlusBin.setEnabled(true);
        this.buttonMinusBin.setEnabled(true);
        this.addParticipanLine(true);
        this.addBinLine(true);
        this.boxParticipantDumpedData.revalidate();
        this.boxParticipantDumpedData.repaint();

    }

    /**
     * Prepare perspective for study participation
     */
    public void setStudyParticipation() {
        this.setParticipating(true);
        this.boxParticipantDumpedData.setVisible(true);
        this.studyTitle.setEnabled(false);
        this.buttonPlusParticipant.setEnabled(false);
        this.buttonMinusParticipant.setEnabled(false);
        this.buttonPlusBin.setEnabled(false);
        this.buttonMinusBin.setEnabled(false);
        this.participantsBox.removeAll();
        this.binsBox.removeAll();
        this.addParticipanLine(false);
        this.addBinLine(false);
        this.saveButton.setEnabled(false);
        this.boxParticipantDumpedData.revalidate();
        this.boxParticipantDumpedData.repaint();
    }

    /**
     * Adds a new line for participant entry
     * @param enabled Indicates whether the new line can be edited or not
     */
    public void addParticipanLine(boolean enabled) {
        this.participantsBox.add(new NameEmailParticipantEntry("", "", enabled));
        this.participantsBox.revalidate();
        this.participantsBox.repaint();
    }

    /**
     * Removes a line for participant entry
     */
    public void removeParticipanLine() {

        int currentComponentCount = this.participantsBox.getComponentCount();
        if (currentComponentCount > 1) this.participantsBox.remove(this.participantsBox.getComponentCount() -
                                                                   1);
        this.participantsBox.revalidate();
        this.participantsBox.repaint();
    }

    /**
     * Adds a new line for bin entry
     * @param enabled Indicates whether the new line can be changed or not
     */
    public void addBinLine(boolean enabled) {
        // TODO: dynamische Texte für Felder (global)
        this.binsBox.add(new BinEntry("", enabled));
        this.participantsBox.revalidate();
        this.participantsBox.repaint();
    }

    /**
     * Removes a line for bin entry
     */
    public void removeBinLine() {

        int currentBinCount = this.binsBox.getComponentCount();
        if (currentBinCount > 1) this.binsBox.remove(this.binsBox.getComponentCount() - 1);
        this.participantsBox.revalidate();
        this.participantsBox.repaint();
    }

    /**
     * Convenience method to change behavior of save button
     * @param newActionListener new behavior
     */
    public void setActionListener(ActionListener newActionListener) {
        if (this.saveButton.getActionListeners().length > 0) this.saveButton.removeActionListener(this.saveButton.getActionListeners()[0]);
        this.saveButton.addActionListener(newActionListener);
    }

    /**
     * Opens a save dialog and sets the status accordingly
     * 
     * @return Saving actually performed?
     */
    /**
     * @return
     */
    public boolean openSaveDialog() {
        boolean saved = false;
        JFileChooser fileChooser = new JFileChooser();
        int returnFileChooser;
        returnFileChooser = fileChooser.showSaveDialog((Component) null);
        if (returnFileChooser == JFileChooser.APPROVE_OPTION) {
            // Saves appStates to reset if saving fails
            AppState tmpAppModelState = SMPCServices.getServicesSMPC().getAppModel().state;
            AppState tmpWorkflowState = SMPCServices.getServicesSMPC().getWorkflowState();
            try {
                SMPCServices.getServicesSMPC()
                            .getAppModel().filename = fileChooser.getSelectedFile();
                SMPCServices.getServicesSMPC().getAppModel().state = AppState.SENDING_SHARE;
                SMPCServices.getServicesSMPC().setWorkflowState(AppState.SENDING_SHARE);
                SMPCServices.getServicesSMPC().getAppModel().saveProgram();
                saved = true;
            } catch (IOException currentException) {
                saved = false;
                SMPCServices.getServicesSMPC().getAppModel().state = tmpAppModelState;
                SMPCServices.getServicesSMPC().setWorkflowState(tmpWorkflowState);
                // TODO Improve error message
                JOptionPane.showMessageDialog(null, Resources.getString("PerspectiveCreate.saveError"));
            }
        }
        return saved;
    }
}

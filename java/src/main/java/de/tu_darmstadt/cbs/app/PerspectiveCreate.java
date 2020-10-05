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
    @Getter
    private Box        boxParticipants;
    @Getter
    private Box        boxBins;
    @Getter
    private JTextField studyTitle;
    @Getter
    private JTextArea  participantDumpedData;
    @Getter
    private Box        boxParticipantDumpedData;
    private JButton    buttonPlusParticipant;
    private JButton    buttonMinusParticipant;
    private JButton    buttonPlusBin;
    private JButton    buttonMinusBin;
    @Getter
    @Setter
    boolean            isParticipating;
    private JButton    saveButton;

    protected PerspectiveCreate(App app) {
        super(app, Messages.getString("PerspectiveCreate.0")); //$NON-NLS-1$
    }

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
        boxParticipantDumpedData.add(new JLabel("Data recieved by study creator:"));
        this.participantDumpedData = new JTextArea();
        this.participantDumpedData.setLineWrap(true);
        // react to to changes in data box
        this.participantDumpedData.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void removeUpdate(DocumentEvent e) {
                if (participantDumpedData.getText().isEmpty()) {
                    boxParticipants.removeAll();
                    boxBins.removeAll();
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
        box1.add(new JLabel("Name of study:")); // TODO: All Strings in message
        this.studyTitle = new JTextField();
        this.studyTitle.setMaximumSize(new Dimension(500, 30)); // TODO: Wie
                                                                // das besser
                                                                // machen?
        box1.add(studyTitle);
        workingpanel.add(box1);

        // ------
        // Participants
        // ------
        this.boxParticipants = Box.createVerticalBox();
        workingpanel.add(boxParticipants);

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

        this.boxBins = Box.createVerticalBox();
        workingpanel.add(boxBins);

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
        this.saveButton = new JButton("Save");
        saveButton.setHorizontalAlignment(SwingConstants.RIGHT);
        saveButtonBox.add(saveButton);
        workingpanel.add(saveButtonBox);
    }

    protected void setDataForParticipant() {
        boolean firstParticipant = true;
        try {
            SMPCServices.getServicesSMPC()
                        .initalizeAsNewStudyParticipation(participantDumpedData.getText());
            boxParticipants.removeAll();
            boxBins.removeAll();
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
                boxParticipants.add(newNameEmailParticipantEntry);

            }
            for (Bin currentBin : SMPCServices.getServicesSMPC().getAppModel().bins) {
                boxBins.add(new BinEntry(currentBin.name, false));
            }
            this.saveButton.setEnabled(true);
            boxParticipants.revalidate();
            boxParticipants.repaint();
            boxBins.revalidate();
            boxBins.repaint();
        } catch (IllegalArgumentException e) {
            System.out.println("message invalid");
            this.saveButton.setEnabled(false);
            // TODO: Validation mit Ampel
        }
    }

    public void digestDataAsNewStudyCreation() {
        ArrayList<Participant> participants = new ArrayList<>();
        ArrayList<Bin> bins = new ArrayList<>();
        SMPCServices.getServicesSMPC().initalizeAsNewStudyCreation();

        // Unmarshall participants
        for (int i = 0; i < boxParticipants.getComponentCount(); i++) {
            NameEmailParticipantEntry currentNameEmailParticipant = (NameEmailParticipantEntry) boxParticipants.getComponent(i);
            Participant participant = new Participant(currentNameEmailParticipant.getParticipantTextField()
                                                                                 .getText(),
                                                      currentNameEmailParticipant.getEmailTextField()
                                                                                 .getText());
            participants.add(participant);
        }

        // Unmarshall bins
        for (int i = 0; i < boxBins.getComponentCount(); i++) {

            BinEntry currentBinEntry = (BinEntry) boxBins.getComponent(i);

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
     * Add bins with values of a participant
     * 
     * @param boxBins
     * @param selectedFile
     */
    public void digestDataAsStudyParticipation() {
        BigInteger[] secretValuesofParticipant = new BigInteger[SMPCServices.getServicesSMPC()
                                                                            .getAppModel().bins.length];

        for (int i = 0; i < this.boxBins.getComponentCount(); i++) {
            secretValuesofParticipant[i] = new BigInteger(((BinEntry) boxBins.getComponent(i)).getBinValueField()
                                                                                              .getText());
        }
        SMPCServices.getServicesSMPC().getAppModel().toSendingShares(secretValuesofParticipant);
    }

    public void setStudyCreation() {
        this.setParticipating(false);
        this.boxParticipantDumpedData.setVisible(false);
        this.studyTitle.setText("");
        this.studyTitle.setEnabled(true);
        this.boxParticipants.removeAll();
        this.boxBins.removeAll();
        this.buttonPlusParticipant.setEnabled(true);
        this.buttonMinusParticipant.setEnabled(true);
        this.buttonPlusBin.setEnabled(true);
        this.buttonMinusBin.setEnabled(true);
        this.addParticipanLine(true);
        this.addBinLine(true);
        this.boxParticipantDumpedData.revalidate();
        this.boxParticipantDumpedData.repaint();

    }

    public void setStudyParticipation() {
        this.setParticipating(true);
        this.boxParticipantDumpedData.setVisible(true);
        this.studyTitle.setEnabled(false);
        this.buttonPlusParticipant.setEnabled(false);
        this.buttonMinusParticipant.setEnabled(false);
        this.buttonPlusBin.setEnabled(false);
        this.buttonMinusBin.setEnabled(false);
        this.boxParticipants.removeAll();
        this.boxBins.removeAll();
        this.addParticipanLine(false);
        this.addBinLine(false);
        this.saveButton.setEnabled(false);
        this.boxParticipantDumpedData.revalidate();
        this.boxParticipantDumpedData.repaint();
    }

    public void addParticipanLine(boolean enabled) {
        this.boxParticipants.add(new NameEmailParticipantEntry("", "", enabled));
        this.boxParticipants.revalidate();
        this.boxParticipants.repaint();
    }

    public void removeParticipanLine() {

        int currentComponentCount = this.boxParticipants.getComponentCount();
        if (currentComponentCount > 1) this.boxParticipants.remove(this.boxParticipants.getComponentCount() -
                                                                   1);
        this.boxParticipants.revalidate();
        this.boxParticipants.repaint();
    }

    public void addBinLine(boolean enabled) {
        // TODO: dynamische Texte für Felder (global)
        this.boxBins.add(new BinEntry("", enabled));
        this.boxParticipants.revalidate();
        this.boxParticipants.repaint();
    }

    public void removeBinLine() {

        int currentBinCount = this.boxBins.getComponentCount();
        if (currentBinCount > 1) this.boxBins.remove(this.boxBins.getComponentCount() - 1);
        this.boxParticipants.revalidate();
        this.boxParticipants.repaint();
    }

    /**
     * @param Convenience
     *            method to change behavior of save button
     */
    public void setActionListener(ActionListener newActionListener) {
        // remove only action listener and add new one
        if (this.saveButton.getActionListeners().length > 0) this.saveButton.removeActionListener(this.saveButton.getActionListeners()[0]);
        this.saveButton.addActionListener(newActionListener);
    }

    /**
     * Opens a save dialog
     * 
     * @return Saving actually performed?
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
                JOptionPane.showMessageDialog(null, "Saving not possible");
            }
        }
        return saved;
    }
}

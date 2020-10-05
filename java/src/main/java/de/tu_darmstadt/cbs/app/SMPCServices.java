/**
 * 
 */
package de.tu_darmstadt.cbs.app;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

import javax.swing.JOptionPane;

import de.tu_darmstadt.cbs.emailsmpc.AppModel;
import de.tu_darmstadt.cbs.emailsmpc.AppState;
import de.tu_darmstadt.cbs.emailsmpc.BinResult;
import lombok.Getter;
import lombok.Setter;

/**
 * Singleton object of this class is the Controller in a MVC pattern
 * 
 * @author Felix Wirth
 *
 */

public class SMPCServices {
    private static SMPCServices singleSMPC;
    @Getter
    private AppModel            appModel;
    /**
     * GUI state deviates sligthly from state in the API, thus a second state
     * variable
     */
    @Getter
    @Setter
    private AppState            workflowState;
    @Getter
    @Setter
    private App                 app;

    public static SMPCServices getServicesSMPC() {
        if (singleSMPC == null) {
            singleSMPC = new SMPCServices();
        }
        return singleSMPC;
    }

    /**
     * Method which controls the program flow/the construction of perspective
     * 
     */
    public void commandAndControl() {
        switch (this.workflowState) {

        case INITIAL_SENDING:
            this.app.showPerspective(PerspectiveCreate.class);
            ((PerspectiveCreate) this.app.getPerspective(PerspectiveCreate.class)).setStudyCreation();
            ((PerspectiveCreate) this.app.getPerspective(PerspectiveCreate.class)).setActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    ((PerspectiveCreate) app.getPerspective(PerspectiveCreate.class)).digestDataAsNewStudyCreation();
                    if (((PerspectiveCreate) app.getPerspective(PerspectiveCreate.class)).openSaveDialog()) {
                        SMPCServices.getServicesSMPC().commandAndControl();
                        // TODO: Aufruf in neuem Thread/als Callback?
                    }
                }
            });
            break;
        case PARTICIPATING:
            this.app.showPerspective(PerspectiveCreate.class);
            ((PerspectiveCreate) getApp().getPerspective(PerspectiveCreate.class)).setStudyParticipation();
            ((PerspectiveCreate) this.app.getPerspective(PerspectiveCreate.class)).setActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    ((PerspectiveCreate) app.getPerspective(PerspectiveCreate.class)).digestDataAsStudyParticipation();
                    if (((PerspectiveCreate) app.getPerspective(PerspectiveCreate.class)).openSaveDialog()) {
                        SMPCServices.getServicesSMPC().getAppModel().state = AppState.SENDING_SHARE;
                        SMPCServices.getServicesSMPC().setWorkflowState(AppState.SENDING_SHARE);
                        SMPCServices.getServicesSMPC().commandAndControl();
                        // TODO: Aufruf in neuem Thread/als Callback?
                    }
                }
            });
            break;
        case SENDING_SHARE:
            this.app.showPerspective(PerspectiveContinue.class);
            ((PerspectiveContinue) this.app.getPerspective(PerspectiveContinue.class)).setDataDisplay();
            ((PerspectiveContinue) this.app.getPerspective(PerspectiveContinue.class)).setActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    SMPCServices.getServicesSMPC().setWorkflowState(AppState.RECIEVING_SHARE);
                    SMPCServices.getServicesSMPC().commandAndControl();
                }
            });
            break;
        case RECIEVING_SHARE:
            if (this.appModel.state != AppState.RECIEVING_SHARE) this.appModel.state = AppState.RECIEVING_SHARE;
            this.app.showPerspective(PerspectiveContinue.class);
            ((PerspectiveContinue) this.app.getPerspective(PerspectiveContinue.class)).setDataEntry();
            ((PerspectiveContinue) this.app.getPerspective(PerspectiveContinue.class)).setActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    int resultConfirmDialog = JOptionPane.showConfirmDialog(null,
                                                                            "Please confirm, that you want to proceed. No changes to the data can be done afterwards",
                                                                            "",
                                                                            JOptionPane.OK_CANCEL_OPTION);
                    if (resultConfirmDialog == 0) {
                        SMPCServices.getServicesSMPC().markMessagesSent();
                        ((PerspectiveContinue) app.getPerspective(PerspectiveContinue.class)).digestDataEntry();
                        SMPCServices.getServicesSMPC().setWorkflowState(AppState.SENDING_RESULT);
                        SMPCServices.getServicesSMPC().getAppModel().toSendingResult();
                        try {
                            // TODO: Possibility of "save as"
                            SMPCServices.getServicesSMPC().getAppModel().saveProgram();
                        } catch (IOException e1) {
                            // TODO Auto-generated catch block
                            e1.printStackTrace();
                        }
                        SMPCServices.getServicesSMPC().commandAndControl();
                    }
                }
            });
            break;
        case SENDING_RESULT:
            this.app.showPerspective(PerspectiveContinue.class);
            ((PerspectiveContinue) this.app.getPerspective(PerspectiveContinue.class)).setDataDisplay();
            ((PerspectiveContinue) this.app.getPerspective(PerspectiveContinue.class)).setActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    SMPCServices.getServicesSMPC().setWorkflowState(AppState.RECIEVING_RESULT);
                    SMPCServices.getServicesSMPC().commandAndControl();
                }
            });
            break;
        case RECIEVING_RESULT:
            this.app.showPerspective(PerspectiveContinue.class);
            ((PerspectiveContinue) this.app.getPerspective(PerspectiveContinue.class)).setDataEntry();
            ((PerspectiveContinue) this.app.getPerspective(PerspectiveContinue.class)).setActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    int resultConfirmDialog = JOptionPane.showConfirmDialog(null,
                                                                            "Please confirm, that you want to proceed. No changes to the data can be done afterwards",
                                                                            "",
                                                                            JOptionPane.OK_CANCEL_OPTION);
                    if (resultConfirmDialog == 0) {
                        SMPCServices.getServicesSMPC().markMessagesSent();
                        SMPCServices.getServicesSMPC().getAppModel().toRecievingResult();
                        ((PerspectiveContinue) app.getPerspective(PerspectiveContinue.class)).digestDataEntry();
                        SMPCServices.getServicesSMPC().setWorkflowState(AppState.FINISHED);
                        SMPCServices.getServicesSMPC().getAppModel().toFinished();
                        SMPCServices.getServicesSMPC().commandAndControl();
                    }
                }
            });
            break;
        case FINISHED:
            BinResult[] binResult = SMPCServices.getServicesSMPC().getAppModel().getAllResults();
            System.out.println(binResult[0]);
            this.app.showPerspective(PerspectiveFinalize.class);
            ((PerspectiveFinalize) this.app.getPerspective(PerspectiveFinalize.class)).getMyResult()
                                                                                      .setText(binResult[0].value.toString());
            ;
            break;
        }
    }

    /**
     * Convenience method to mark messages as already sent
     */
    protected void markMessagesSent() {

        for (int i = 0; i < SMPCServices.getServicesSMPC().getAppModel().numParticipants; i++) {
            if (i != SMPCServices.getServicesSMPC().getAppModel().numberOwnPartcipation) {
                SMPCServices.getServicesSMPC().getAppModel().markMessageSent(i);
            }
        }
    }

    public void initalizeAsNewStudyCreation() {
        this.appModel = new AppModel();
        this.appModel.toStarting();
    }

    /**
     * Initializes APIs state as new study participation
     * 
     * @param Data
     *            copied by user manually in program
     */
    public void initalizeAsNewStudyParticipation(String dumpedData) {
        this.appModel = new AppModel();
        this.appModel.toParticipating();
        this.appModel.toEnteringValues(dumpedData);
    }

    /**
     * Load a file to reinstate API state
     * 
     * @param selectedFile
     * @throws IOException
     * @throws IllegalArgumentException
     * @throws ClassNotFoundException
     */
    public void loadFile(File selectedFile) throws ClassNotFoundException,
                                            IllegalArgumentException,
                                            IOException {
        appModel = AppModel.loadModel(selectedFile);
        appModel.filename = selectedFile;
        this.workflowState = this.appModel.state;
        // TODO Password protect read and write
    }
}

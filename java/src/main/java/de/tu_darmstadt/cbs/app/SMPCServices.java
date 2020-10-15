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
 * Singleton object of this class is the controller in a MVC pattern
 * 
 * @author Felix Wirth
 *
 */

public class SMPCServices {
    /** Singleton of this class */
    private static SMPCServices singleSMPC;
    /** The object containing the secure multi-party computing API */
    /**
     * Returns appModel
     * @return
     */
    @Getter
    private AppModel            appModel;
    /** GUI state deviates slightly from state in the API, thus a second state variable */
    /**
     * Returns workflowState
     * @return
     */
    @Getter
    /**
     * Gets workflowState
     * @param workflowState
     */
    @Setter
    private AppState            workflowState;
    /** The app constructing the GUI */
    /**
     * Gets the app
     * @return
     */
    @Getter
    /**
     * Sets the app
     * @param app
     */
    @Setter
    private App                 app;

    /**
     * Get the object of this class as a singleton
     * @return
     */
    public static SMPCServices getServicesSMPC() {
        if (singleSMPC == null) {
            singleSMPC = new SMPCServices();
        }
        return singleSMPC;
    }

    /**
     * Method which controls the program flow/the construction of perspective
     */
    public void commandAndControl() {
        switch (this.workflowState) {
        case INITIAL_SENDING:
            this.app.showPerspective(PerspectiveCreate.class);
            break;
        case PARTICIPATING:
            this.app.showPerspective(PerspectiveParticipate.class);
//            ((PerspectiveCreate) this.app.getPerspective(PerspectiveParticipate.class)).setActionListener(new ActionListener() {
//                @Override
//                public void actionPerformed(ActionEvent e) {
//                    if( ((PerspectiveCreate) app.getPerspective(PerspectiveParticipate.class)).validateAllData() ) {
//                    ((PerspectiveCreate) app.getPerspective(PerspectiveParticipate.class)).digestDataAsStudyParticipation();
//                    SMPCServices.getServicesSMPC().getAppModel().state = AppState.SENDING_SHARE;
//                    SMPCServices.getServicesSMPC().setWorkflowState(AppState.SENDING_SHARE);
//                    if (((PerspectiveCreate) app.getPerspective(PerspectiveParticipate.class)).openSaveDialog()) {
//                        SMPCServices.getServicesSMPC().commandAndControl();
//                    }
//                    //TODO How to deal with a problem while saving
//                }
//              }
//            });
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
                                                                            Resources.getString("SMPCServices.confirmProceed"),
                                                                            "",
                                                                            JOptionPane.OK_CANCEL_OPTION);
                    if (resultConfirmDialog == 0) {
                        SMPCServices.getServicesSMPC().markMessagesSent();
                        ((PerspectiveContinue) app.getPerspective(PerspectiveContinue.class)).digestDataEntry();
                        SMPCServices.getServicesSMPC().setWorkflowState(AppState.SENDING_RESULT);
                        SMPCServices.getServicesSMPC().getAppModel().toSendingResult();
                        try {
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
        default:
            JOptionPane.showMessageDialog(null, Resources.getString("SMPCServices.wrongWorkflowState"));
        }
        
    }

    /**
     * Convenience method to mark messages as already sent
     */
    protected void markMessagesSent() {

        for (int i = 0; i < SMPCServices.getServicesSMPC().getAppModel().numParticipants; i++) {
            if (i != SMPCServices.getServicesSMPC().getAppModel().ownId) {
                SMPCServices.getServicesSMPC().getAppModel().markMessageSent(i);
            }
        }
    }

    /**
     * Initializes APIs state as new study creation
     */
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
     * Loads a file to reinstate API state
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
    }
}

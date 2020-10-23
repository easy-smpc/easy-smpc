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

import java.io.File;
import java.io.IOException;

import de.tu_darmstadt.cbs.emailsmpc.AppModel;
import de.tu_darmstadt.cbs.emailsmpc.AppState;
import de.tu_darmstadt.cbs.emailsmpc.Message;
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
    public void initalizeAsNewStudyParticipation() {
        this.appModel = new AppModel();
        this.appModel.toParticipating();
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

    /**
     * Check whether message is valid
     * @param text
     * @return
     */
    public boolean isInitialParticipationMessageValid(String text) {
        try {
            String data =  Message.deserializeMessage(text).data;
            return appModel.isInitialParticipationMessageValid(data);
        } catch (Exception e) {
           return false;
        }
    }
    
    /**
     * Check whether message is valid
     * @param text
     * @return
     */
    public boolean isMessageShareResultValid(String text, int participantId) {
        try {
            return appModel.isMessageShareResultValid(Message.deserializeMessage(text),
                                                appModel.getParticipantFromId(participantId));
        } catch (Exception e) {
            return false;
        }
    }
}

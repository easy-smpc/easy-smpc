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

import javax.swing.event.ChangeEvent;

import de.tu_darmstadt.cbs.emailsmpc.AppState;

/**
 * A class to poll the clipboard periodically for messages
 * @author Felix Wirth
 * 
 */
public class TaskPollClipboard implements Runnable {
    /** App */
    private App app;

    /**
     * @return the app
     */
    public App getApp() {
        return app;
    }
    
    /**
     * Creates a new instance
     */
    TaskPollClipboard(App app) {
        this.app = app;
    }
    
    /**
     * Executed periodically when registered in an executor
     */
    @Override
    public void run() {
        //If app is in state to receive a message
        if (getApp().getModel() != null &&
            (getApp().getModel().state == AppState.RECIEVING_SHARE ||
             getApp().getModel().state == AppState.RECIEVING_RESULT)) {
            //if successfully set new message update perspective
            if (getApp().actionReceiveMessage(false))
            {   
                getApp().stateChangedCurrentDisplayedPerspective(new ChangeEvent(this));
            }
        }        
    }
}

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

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.swing.event.ChangeEvent;

import de.tu_darmstadt.cbs.app.resources.Resources;
import de.tu_darmstadt.cbs.emailsmpc.AppState;

/**
 * A class to poll the clipboard periodically for messages
 * 
 * @author Felix Wirth
 */
public class TaskPollClipboardReceive implements Runnable {
    /** PerspectiveReceive */
    private Perspective3Receive perspectiveReceive;

    /**
     * @return the perspectiveReceive
     */
    public Perspective3Receive getPerspectiveReceive() {
        return perspectiveReceive;
    }
    
    /**
     * Creates a new instance
     */
    public TaskPollClipboardReceive(Perspective3Receive perspectiveReceive) {
        this.perspectiveReceive = perspectiveReceive;
        Executors.newScheduledThreadPool(1)
                 .scheduleAtFixedRate(this,
                                      0,
                                      Resources.INTERVAL_SCHEDULER_MILLISECONDS,
                                      TimeUnit.MILLISECONDS);
    }
    
    /**
     * Executed periodically when registered in an executor
     */
    @Override
    public void run() {
        // If app is in state to receive a message
        if (perspectiveReceive.getApp().getModel() != null &&
            (perspectiveReceive.getApp().getModel().state == AppState.RECIEVING_SHARE ||
             perspectiveReceive.getApp().getModel().state == AppState.RECIEVING_RESULT)) {
            // If successfully set new message update perspective
            if (perspectiveReceive.getApp().actionReceiveMessage(false)) {
                perspectiveReceive.stateChanged(new ChangeEvent(this));
            }
        }
    }
    
}
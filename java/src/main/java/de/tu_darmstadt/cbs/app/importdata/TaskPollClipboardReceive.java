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
package de.tu_darmstadt.cbs.app.importdata;

import java.awt.HeadlessException;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.swing.event.ChangeEvent;

import de.tu_darmstadt.cbs.app.Perspective3Receive;
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
            Executors.newScheduledThreadPool(2)
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
        if (perspectiveReceive.getApp().getModelState() != null &&
            (perspectiveReceive.getApp().getModelState() == AppState.RECIEVING_SHARE ||
             perspectiveReceive.getApp().getModelState() == AppState.RECIEVING_RESULT)) {            
            // Set message if valid
            String message = getStrippedExchangeMessage(getTextFromClipBoard());         
            if (perspectiveReceive.getApp().isMessageShareResultValid(message)) {
                perspectiveReceive.getApp().setMessageShare(message);
                perspectiveReceive.stateChanged(new ChangeEvent(this));
            }
        }
    }
    
    /**
     * Convenience method to remove exchange message tags
     * @param text
     * @return
     */
    public static String getStrippedExchangeMessage(String text) {
        text = text.replaceAll("\n", "").trim();
        if (text.contains(Resources.MESSAGE_START_TAG)) {
            text = text.substring(text.indexOf(Resources.MESSAGE_START_TAG) + Resources.MESSAGE_START_TAG.length(), text.length());
        }
        if (text.contains(Resources.MESSAGE_END_TAG)) {
            text = text.substring(0, text.indexOf(Resources.MESSAGE_END_TAG));
        }
        return text;
    }

    /**
     * Returns text from clip board if valid
     * @return clip board text
     */
    public static String getTextFromClipBoard() {
        String text = "";
        if (Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null).isDataFlavorSupported(DataFlavor.stringFlavor)) {
            try {
                text = (String) Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null).getTransferData(DataFlavor.stringFlavor);              
            } catch (HeadlessException | UnsupportedFlavorException | IOException e) {
                // No error message to user necessary
            }
        };
        return text;
    }
}
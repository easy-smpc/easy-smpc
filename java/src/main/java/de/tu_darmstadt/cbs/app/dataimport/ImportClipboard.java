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
package de.tu_darmstadt.cbs.app.dataimport;

import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.swing.event.ChangeEvent;

import de.tu_darmstadt.cbs.app.Perspective3Receive;
import de.tu_darmstadt.cbs.app.resources.Resources;

/**
 * A class to poll the clipboard periodically for messages
 * 
 * @author Felix Wirth
 */

public class ImportClipboard implements Runnable {
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
        if (Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null).isDataFlavorSupported(DataFlavor.stringFlavor)) {
            try {
                return (String) Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null).getTransferData(DataFlavor.stringFlavor);              
            } catch (Exception e) {
                // No error message to user necessary
            }
        }
        return null;
    }
    
    /** PerspectiveReceive */
    private Perspective3Receive parent;
    
    /**
     * Creates a new instance
     */
    public ImportClipboard(Perspective3Receive perspectiveReceive) {
            this.parent = perspectiveReceive;
            Executors.newScheduledThreadPool(1).scheduleAtFixedRate(this, 0, Resources.INTERVAL_SCHEDULER_MILLISECONDS, TimeUnit.MILLISECONDS);        
    }

    /**
     * Executed periodically when registered in an executor
     */
    @Override
    public void run() {
        // If app is in state to receive a message
        if (this.parent.isVisible()) {            
            // Set message if valid
            String message = getStrippedExchangeMessage(getTextFromClipBoard());         
            if (parent.getApp().isMessageShareResultValid(message)) {
                parent.getApp().setMessageShare(message);
                // TODO: Delegate to perspective or app 
                parent.stateChanged(new ChangeEvent(this));
            }
        }
    }
}
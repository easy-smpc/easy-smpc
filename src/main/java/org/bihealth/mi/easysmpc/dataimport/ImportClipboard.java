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
package org.bihealth.mi.easysmpc.dataimport;

import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.swing.event.ChangeEvent;

import org.bihealth.mi.easysmpc.Perspective3Receive;
import org.bihealth.mi.easysmpc.resources.Resources;

/**
 * A class to poll the clip board periodically for messages
 * 
 * @author Felix Wirth
 * @author Fabian Prasser
 */
public class ImportClipboard implements Runnable {
    
    /** Last message which was saved */
    String lastSaveMessage = null;
    
    /**
     * Convenience method to remove exchange message tags
     * @param text
     * @return
     */
    public static String getStrippedExchangeMessage(String text) {
        if (text != null) {
            text = text.replaceAll("\n", "").trim();
            if (text.contains(Resources.MESSAGE_START_TAG)) {
                text = text.substring(text.indexOf(Resources.MESSAGE_START_TAG) + Resources.MESSAGE_START_TAG.length(), text.length());
            }
            if (text.contains(Resources.MESSAGE_END_TAG)) {
                text = text.substring(0, text.indexOf(Resources.MESSAGE_END_TAG));
            }
            return text;
        }
        return null;
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
                // Ignore
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
        if (this.parent.isVisible()) {
            String message = getStrippedExchangeMessage(getTextFromClipBoard());
            if (message != lastSaveMessage && parent.getApp().isMessageShareResultValid(message)) {
                parent.getApp().setMessageShare(message);
                parent.getApp().actionSave();
                lastSaveMessage = message;
                parent.stateChanged(new ChangeEvent(this));
            }
        }
    }
}
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
package org.bihealth.mi.easysmpc.components;

import javax.swing.JPanel;

import org.bihealth.mi.easysmpc.resources.Resources;

/**
 * Entry server ports
 * 
 * @author Felix Wirth
 */
public class EntryServerPorts extends ComponentEntry {

    /** SVUID */
    private static final long serialVersionUID = 1074880888679710657L;
    /** Standard port for IMAP */
    public static final String   DEFAULT_PORT_IMAP = "993";
    /** Standard port for SMTP */
    public static final String   DEFAULT_PORT_SMTP = "465";
    
    /**
     * Creates a new instance
     */
    public EntryServerPorts() {
        super(Resources.getString("EmailConfig.15"),
              DEFAULT_PORT_IMAP,
              true,
              new ComponentTextFieldValidator() {
                    @Override
                    public boolean validate(String text) {
                        return checkPortEntryValid(text);
                    }
                }, 
              Resources.getString("EmailConfig.16"), 
              DEFAULT_PORT_SMTP,
              true,
              new ComponentTextFieldValidator() {
                @Override
                public boolean validate(String text) {
                    return checkPortEntryValid(text);
                }
              },
              false);
    }

    @Override
    protected JPanel createAdditionalControls() {
        return null;
    }

    /**
     * Check if a string is a valid port number
     * 
     * @param text
     * @return
     */
    public static boolean checkPortEntryValid(String text) {
        try {
            int portNumber = Integer.parseInt(text);
            if (portNumber > 1 || portNumber < 65535) {
                return true;
            }
            else {
                return false;
            }
        }
        catch (NumberFormatException e) {
            return false;
        }
    }
}
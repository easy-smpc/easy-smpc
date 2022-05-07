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
 * Entry servers
 * 
 * @author Felix Wirth
 */
public class EntryServers extends ComponentEntry {
    
    /** SVID */
    private static final long serialVersionUID = -1491960197508114210L;

    /**
     * Creates a new instance
     */
    public EntryServers() {
        super(Resources.getString("EmailConfig.3"),
              "",
              true,
              new ComponentTextFieldValidator() {
                    @Override
                    public boolean validate(String text) {
                        return text != null && !text.isBlank();
                    }
                }, 
              Resources.getString("EmailConfig.4"), 
              "",
              true,
              new ComponentTextFieldValidator() {
                @Override
                public boolean validate(String text) {
                    return text != null && !text.isBlank();
                }
              },
              false,
              false);
    }

    @Override
    protected JPanel createAdditionalControls() {
        return null;
    }
}
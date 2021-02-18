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

import java.util.regex.Pattern;

import javax.swing.JPanel;

import org.bihealth.mi.easysmpc.resources.Resources;

/**
 * Entry of E-Mail address and password
 * 
 * @author Felix Wirth
 *
 */
public class EntryEMailPassword extends ComponentEntry {
    
    /** SVUID*/
    private static final long serialVersionUID = -7858252891350611757L;
    /** Regex to check e-mail validity */
    private static final Pattern regex = Pattern.compile("^[\\w!#$%&'*+/=?`{|}~^-]+(?:\\.[\\w!#$%&'*+/=?`{|}~^-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,6}$");

    /**
     * @param leftString
     * @param leftValue
     * @param leftEnabled
     * @param leftValidator
     * @param rightString
     * @param rightValue
     * @param rightEnabled
     * @param rightValidator
     * @param additionalControlsEnabled
     */
    public EntryEMailPassword() {
        super(Resources.getString("EmailConfig.1"),
              "",
              true,
              new ComponentTextFieldValidator() {
                    @Override
                    public boolean validate(String text) {
                        return regex.matcher(text).matches();
                    }
                }, 
              Resources.getString("EmailConfig.2"), 
              "",
              true,
              new ComponentTextFieldValidator() {
                @Override
                public boolean validate(String text) {
                    return !text.isBlank();
                }
              },
              false);
    }

    @Override
    protected JPanel createAdditionalControls() {
        return null;
    }
}

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

/**
 * Entry texts with same validation for both fields
 * 
 * @author Felix Wirth
 */
public class EntryTextMonoValidation extends ComponentEntry {
    
    /** SVID */
    private static final long serialVersionUID = 466453564994174241L;
    
    /**
     * Creates a new instance
     * 
     * @param textLeft
     * @param textRight
     * @param validator - can be null
     */
    public EntryTextMonoValidation(String textLeft, String textRight, ComponentTextFieldValidator validator) {
        super(textLeft,
              "",
              true,
              validator != null ? validator : new ComponentTextFieldValidator() {
                    @Override
                    public boolean validate(String text) {
                        return true;
                    }
                }, 
              textRight, 
              "",
              true,
              validator != null ? validator : new ComponentTextFieldValidator() {
                @Override
                public boolean validate(String text) {
                    return true;
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
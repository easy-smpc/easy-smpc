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

import java.util.prefs.BackingStoreException;

import javax.swing.JPanel;

import org.bihealth.mi.easybus.Participant;
import org.bihealth.mi.easybus.implementations.email.ConnectionIMAPSettings;
import org.bihealth.mi.easysmpc.resources.Connections;
import org.bihealth.mi.easysmpc.resources.Resources;

/**
 * Entry of E-Mail address and password
 * 
 * @author Felix Wirth
 */
public class EntryEMailPassword extends ComponentEntry {
    
    /** SVUID*/
    private static final long serialVersionUID = -7858252891350611757L;
    
    /**
     * Creates a new instance
     * @param checkEmailExisting
     */
    public EntryEMailPassword(boolean checkEmailExisting) {
        super(Resources.getString("EmailConfig.1"),
              "",
              true,
              new ComponentTextFieldValidator() {
                    @Override
                    public boolean validate(String text) {
                        
                      // If mail address is invalid return false
                      if (!Participant.isEmailValid(text)) { return false; }

                      // If mail address exists already or existing can not be read return false
                      if (checkEmailExisting) {
                          try {
                              for (ConnectionIMAPSettings settings : Connections.list()) {
                                  if (settings.getEmailAddress().equals(text)) { return false; }
                              }
                          } catch (BackingStoreException e) {
                              return false;
                          }
                      }

                      // Email address is valid
                      return true;
                    }
                }, 
              false,
              Resources.getString("EmailConfig.2"), 
              "",
              true,
              new ComponentTextFieldValidator() {
                @Override
                public boolean validate(String text) {
                    return !text.isBlank();
                }
              },
              true,
              false,
              false);
    }

    @Override
    protected JPanel createAdditionalControls() {
        return null;
    }
}
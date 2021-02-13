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

import org.bihealth.mi.easysmpc.resources.Resources;

import de.tu_darmstadt.cbs.emailsmpc.Participant;

/**
 * Entry for participants
 * 
 * @author Felix Wirth
 * @author Fabian Prasser
 */
public class EntryParticipant extends ComponentEntryAddRemove {

    /** SVUID */
    private static final long serialVersionUID = -6907197312982454382L;

    /**
     * Creates a new instance
     * @param name
     * @param email
     * @param enabled
     * @param additionalControlsEnabled
     */
    public EntryParticipant(String name, String email, boolean enabled, boolean additionalControlsEnabled) {
        super(Resources.getString("Participant.0"), //$NON-NLS-1$
              name,
              enabled,
              new ComponentTextFieldValidator() {
                @Override
                public boolean validate(String text) {
                    return !text.trim().isEmpty();
                }
              },
              Resources.getString("Participant.1"), //$NON-NLS-1$
              email,
              enabled,
              new ComponentTextFieldValidator() {
                  @Override
                  public boolean validate(String text) {
                      return Participant.validEmail(text);
                  }
                },
              additionalControlsEnabled);
    }
    
    /**
     * Returns true if both values are empty
     */
    @Override
    public boolean isEmpty() {
        return (this.getLeftValue().trim().isEmpty() && this.getRightValue().trim().isEmpty());     
    }
}
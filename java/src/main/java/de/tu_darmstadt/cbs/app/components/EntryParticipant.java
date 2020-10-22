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
package de.tu_darmstadt.cbs.app.components;

import de.tu_darmstadt.cbs.app.Resources;
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
     */
    public EntryParticipant(String name, String email, boolean enabled) {
        super(Resources.getString("Participant.0"), //$NON-NLS-1$
              name,
              enabled,
              new ComponentTextFieldValidator() {
                @Override
                public boolean validate(String text) {
                    // TODO: Must ensure that no two bins have the same name
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
              enabled);
    }
}
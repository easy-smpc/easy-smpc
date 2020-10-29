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

import de.tu_darmstadt.cbs.app.resources.Resources;

/**
 * Display participants for sending mail only
 * 
 * @author Felix Wirth
 * @author Fabian Prasser
 */
public class EntryParticipantEnterExchangeString extends ComponentEntryOneButton {

    /** SVID */
    private static final long serialVersionUID = 1321493331077271865L;

    /**
     * Creates a new instance
     * @param name
     * @param email
     */
    public EntryParticipantEnterExchangeString(String name, String email) {
        this(name, email, false);
    }
    
    /**
     * Creates a new instance
     * @param name
     * @param email
     * @param buttonEnabled
     */
    public EntryParticipantEnterExchangeString(String name, String email, boolean buttonEnabled) {
      super(Resources.getString("Participant.0"), name, Resources.getString("Participant.1"), email, buttonEnabled);
    }

    /**
     * Returns text for button (label)
     * @return button text
     */
    @Override
    protected String getText() {
        return Resources.getString("PerspectiveReceive.receive");
    }
}

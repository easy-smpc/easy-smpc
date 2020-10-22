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

/**
 * Display participants for sending mail only
 * 
 * @author Felix Wirth
 *
 */
public class EntryParticipantSendMail extends ComponentEntryOneButton {


    /**
     * Creates a new instance
     * @param name
     * @param email
     */
    public EntryParticipantSendMail(String name, String email) {
        this(name, email, false);
    }
    
    /**
     * Creates a new instance
     * @param name
     * @param email
     * @param buttonEnabled
     */
    public EntryParticipantSendMail(String name, String email, boolean buttonEnabled) {
        super(name, email, buttonEnabled);
    }

    
    /**
     * Returns text for button (label)
     * @return button text
     */
    @Override
    protected String getText() {
        return Resources.getString("PerspectiveSend.sendEmailButton");
    }  
}

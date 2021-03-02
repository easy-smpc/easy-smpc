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

/**
 * Display participants for sending mail only
 * 
 * @author Felix Wirth
 * @author Armin Müller
 *
 */
public class EntryParticipantSendMail extends ComponentEntryTwoButtons {

    /** SVID */
    private static final long serialVersionUID = 3947342543992289608L;

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
        super(Resources.getString("Participant.0"),
              name,
              Resources.getString("Participant.1"),
              email,
              buttonEnabled);
    }

    
    /**
     * Disables button 1
     */
    public void setButton1Enabled(boolean enabled) {
        this.button1.setEnabled(enabled);
    }
    
    /**
     * Disables button 2
     */
    public void setButton2Enabled(boolean enabled) {
        this.button2.setEnabled(enabled);
    }
    
    /**
     * Returns text for button (label)
     * @return button text
     */
    @Override
    protected String getButton1Text() {
        return Resources.getString("PerspectiveSend.sendEmailAutomaticButton");
    }
    
    /**
     * Returns text for button (label)
     * @return button text
     */
    @Override
    protected String getButton2Text() {
        return Resources.getString("PerspectiveSend.sendEmailManualButton");
    }
}

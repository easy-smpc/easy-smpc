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

import javax.swing.JLabel;

import org.bihealth.mi.easybus.implementations.http.easybackend.ConnectionSettingsEasybackend;

/**
 *  Entry of advanced details of an easybackend connection
 * 
 * @author Felix Wirth
 *
 */
 
public class EntryEasybackendAdvanced extends EntryEasybackendBasic {


    /** SVUID */
    private static final long serialVersionUID = 8266372600895412625L;
    
    /**
     * Create new instance from settings object
     * 
     * @param settings
     * @param createMode 
     */
    public EntryEasybackendAdvanced(ConnectionSettingsEasybackend settings, boolean createMode) {
        super(settings, createMode);
        
        // TODO Add actual advanced options
        this.add(new JLabel("This is very advanced"));
    }
}
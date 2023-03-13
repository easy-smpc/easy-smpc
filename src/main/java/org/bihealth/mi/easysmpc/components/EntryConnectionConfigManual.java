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
import javax.swing.event.ChangeListener;

import org.bihealth.mi.easybus.ConnectionSettings;
import org.bihealth.mi.easybus.implementations.local.ConnectionSettingsManual;
import org.bihealth.mi.easysmpc.resources.Resources;

/**
 * Entry for manual exchange
 * 
 * @author Felix Wirth
 *
 */
public class EntryConnectionConfigManual extends ComponentConnectionConfig {

    /** SVUID */
    private static final long serialVersionUID = -7205182503261046971L;
    
    /**
     * Creates a new instance
     */
    public EntryConnectionConfigManual(){
        this.add(new JLabel(Resources.getString("ManualConfig.0")));
    }

    @Override
    public ConnectionSettings getConnectionSettings() {
        return new ConnectionSettingsManual();
    }

    @Override
    public boolean isProceedPossible() {
        return true;
    }

    @Override
    public boolean isAddPossible() {
        return false;
    }

    @Override
    public boolean isRemovePossible() {
        return false;
    }

    @Override
    public void actionAdd() {
        // Empty
    }

    @Override
    public void actionRemove() {
        // Empty
    }

    @Override
    public void setChangeListener(ChangeListener changeListener) {
        // Empty
    }

}

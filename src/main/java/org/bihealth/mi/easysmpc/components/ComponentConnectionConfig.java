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
import javax.swing.event.ChangeListener;

import org.bihealth.mi.easybus.ConnectionSettings;

/**
 * A component for the connection settings config
 * 
 * @author Felix Wirth
 *
 */
public abstract class ComponentConnectionConfig extends JPanel {
    
    /** SVUID */
    private static final long serialVersionUID = 1455265049057046097L;

    /**
     * Creates a new instance
     */
    public ComponentConnectionConfig() {
       super(); 
    }
    
    /**
     * Is proceeding possible?
     * @return
     */
    public abstract boolean isProceedPossible();
    
    /**
     * Is delete possible?
     * @return
     */
    public abstract boolean isAddPossible();
    
    /**
     * Is remove possible?
     * @return
     */
    public abstract boolean isRemovePossible();
    
    /**
     * Add
     */
    public abstract void actionAdd();
    
    /**
     * Remove
     */
    public abstract void actionRemove();
    
    /**
     * Returns the configured connection
     * @return
     */
    public abstract ConnectionSettings getConnectionSettings();
    
    /**
     * Set change listener
     * @param changeListener
     */
    public abstract void setChangeListener(ChangeListener changeListener);
}

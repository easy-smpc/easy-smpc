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
     * Are the entered values valid?
     * @return
     */
    public abstract boolean areValuesValid();
    
    /**
     * Returns the configured connection
     * @return
     */
    public abstract ConnectionSettings getConnectionSettings();

}

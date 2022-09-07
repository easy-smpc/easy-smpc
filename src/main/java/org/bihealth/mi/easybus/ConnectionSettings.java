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
package org.bihealth.mi.easybus;

import java.io.Serializable;

import org.bihealth.mi.easysmpc.resources.Resources;

/** Generic stettings
 * 
 * @author Felix Wirth
 *
 */
public abstract class ConnectionSettings implements Serializable {
    
    /** SVUID */
    private static final long serialVersionUID = -3887172032343688839L;
    
    /**
     * Returns the identifier
     * @return
     */
    public abstract String getIdentifier();
    
    /** Connection types */
    public enum ConnectionTypes { 
        /** Enum values */
        MANUAL, EMAIL, EASYBACKEND;
        
        public String toString() {
            return Resources.getString(String.format("ConnectionTypes.%s", this.name()));
        }
    }; 
}

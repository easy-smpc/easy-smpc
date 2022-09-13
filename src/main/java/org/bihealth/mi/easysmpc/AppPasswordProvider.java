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
package org.bihealth.mi.easysmpc;

import java.awt.Window;
import java.io.Serializable;

import javax.swing.JFrame;

import org.bihealth.mi.easybus.PasswordStore;
import org.bihealth.mi.easybus.implementations.email.PasswordProvider;
import org.bihealth.mi.easysmpc.components.DialogPassword;

/**
 * A password provider for the app
 * @author Fabian Prasser
 */
public class AppPasswordProvider implements PasswordProvider, Serializable {

    /** SVUID*/
    private static final long serialVersionUID = -7455626179379349238L;
    /** First password descriptor */
    private String firstPasswordDescriptor;
    /** Second password descriptor */
    private String secondPasswordDescriptor = null;
    
    
    /**
     * Creates a new instance to obtain one password
     * 
     * @param singlePasswordDescriptor
     */
    public AppPasswordProvider(String singlePasswordDescriptor) {
        // Store
        this.firstPasswordDescriptor = singlePasswordDescriptor;
    }
    
    /**
     * Creates a new instance to obtain two passwords
     * 
     * @param singlePasswordDescriptor
     */
    public AppPasswordProvider(String firstPasswordDescriptor, String secondPasswordDescriptor) {
        // Store
        this.firstPasswordDescriptor = firstPasswordDescriptor;
        this.secondPasswordDescriptor = secondPasswordDescriptor;
    }
    
    /**
     * Default constructor
     */
    @SuppressWarnings("unused")
    private AppPasswordProvider() {
        // Empty by design
    }
    
    @Override
    public PasswordStore getPassword() {
        return new DialogPassword(firstPasswordDescriptor, secondPasswordDescriptor, getParent()).showDialog();
    }

    /**
     * Returns the parent assuming that the application consists of only one window
     * @return
     */
    private JFrame getParent() {
        for (Window window : Window.getWindows()) {
            if (window instanceof JFrame && window.isVisible()) {
                return (JFrame)window;
            }
        }
        throw new IllegalStateException("Could not determine parent window");
    }
}

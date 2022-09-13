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

/**
 * A class storing up to two passwords
 * 
 * @author Felix Wirth
 *
 */
public class PasswordStore {
    /** First password */
    private final String firstPassword;
    
    /** Second password */
    private final String secondPassword; 
    
    /**
     * Creates a new instance. If second password is null, firstPassword will be assumed as secondPassword
     * 
     * @param firstPassword
     * @param secondPassword
     */
    public PasswordStore(String firstPassword, String secondPassword) {
        
        // Check
        if(firstPassword == null || firstPassword.isBlank()) {
            throw new IllegalArgumentException("First password must not be null");
        }
        
        // Store
        this.firstPassword = firstPassword;            
        this.secondPassword = secondPassword != null && !secondPassword.isBlank() ? secondPassword : firstPassword;
    }

    /**
     * @return the first password
     */
    public String getFirstPassword() {
        return firstPassword;
    }

    /**
     * @return the secondPassword
     */
    public String getSecondPassword() {
        return secondPassword;
    }
}
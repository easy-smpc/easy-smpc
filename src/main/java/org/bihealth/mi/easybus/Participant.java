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
import java.util.regex.Pattern;

/**
 * A participant (either sender or receiver)
 * 
 * @author Felix Wirth
 * 
 */
public class Participant implements Serializable {
    
    /** SVUID */
    private static final long serialVersionUID = 4218866719460664961L;

    /** Regex to check for a correct mail address */
    private static final Pattern CHECK_EMAIL_REGEX = Pattern.compile("^[\\w!#$%&'*+/=?`{|}~^-]+(?:\\.[\\w!#$%&'*+/=?`{|}~^-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,6}$");
   
    /**
     * Check if an e-mail address is valid
     * 
     * @return e-mail is valid
     */
    public static boolean isEmailValid(String emailAddress) {
        return CHECK_EMAIL_REGEX.matcher(emailAddress).matches();
    }
    
    /** Name */
    private String name;

    /** E-mail address */
    private String emailAddress;
     
    /**
    * Creates a new instance
     * @throws BusException 
    */ 
    public Participant(String name, String emailAddress) throws BusException {
        if (!isEmailValid(emailAddress)) {
            throw new BusException("User name is not a valid e-mail address");
        }
        this.name = name;
        this.emailAddress = emailAddress;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        Participant other = (Participant) obj;
        if (emailAddress == null) {
            if (other.emailAddress != null) return false;
        } else if (!emailAddress.equals(other.emailAddress)) return false;
        if (name == null) {
            if (other.name != null) return false;
        } else if (!name.equals(other.name)) return false;
        return true;
    }
    
    /**
     * Returns the e-mail address
     * 
     * @return the emailAddress
     */
    public String getEmailAddress() {
        return emailAddress;
    }

    /**
     * Returns the name
     * 
     * @return the name
     */
    public String getName() {
        return name;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((emailAddress == null) ? 0 : emailAddress.hashCode());
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        return result;
    }
}
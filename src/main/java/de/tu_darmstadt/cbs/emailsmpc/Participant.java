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
package de.tu_darmstadt.cbs.emailsmpc;

import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.io.Serializable;

/**
 * Represents a participant
 * @author Tobias Kussel
 */
public class Participant implements Serializable, Cloneable {
    
    /** SVUID */
    private static final long serialVersionUID = 5370286651195899392L;
    
    /** The regex. */
    private static final Pattern regex = Pattern.compile("^[\\w!#$%&'*+/=?`{|}~^-]+(?:\\.[\\w!#$%&'*+/=?`{|}~^-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,6}$");
    
    /**
     * Valid email.
     *
     * @param email the email
     * @return true, if successful
     */
    public static boolean validEmail(String email) {
        Matcher m = regex.matcher(email);
        return m.matches();
    }
    
    /** The name. */
    public final String name;

    /** The email address. */
    public final String emailAddress;

    /**
     * Instantiates a new participant.
     *
     * @param name the name
     * @param emailAddress the email address
     * @throws IllegalArgumentException the illegal argument exception
     */
    public Participant(String name, String emailAddress) throws IllegalArgumentException {
        if (!Participant.validEmail(emailAddress)) {
            throw new IllegalArgumentException("Invalid Email Address: " + emailAddress);
        }
        this.name = name;
        this.emailAddress = emailAddress;
    }

    /**
     * Clone.
     *
     * @return the object
     */
    @Override
    public Object clone() {
      try {
        return (Participant) super.clone();
      } catch (CloneNotSupportedException e) {
        return new Participant(this.name, this.emailAddress);
      }
    }

    /**
     * Equals.
     *
     * @param o the o
     * @return true, if successful
     */
    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (!(o instanceof Participant))
            return false;
        Participant p = (Participant) o;
        return p.name.equals(name) && p.emailAddress.equals(emailAddress);
    }

    /**
     * Hash code.
     *
     * @return the int
     */
    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + emailAddress.hashCode();
        return result;
    }

    /**
     * To string.
     *
     * @return the string
     */
    @Override
    public String toString() {
        return name + ": " + emailAddress;
    }
}

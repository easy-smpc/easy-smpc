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
import java.util.function.Predicate;
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
    
    /** Regex to check for correct matrix MXID */
    private static final Pattern CHECK_MXID_REGEX  = Pattern.compile("^@{1}[a-z0-9._=-\\\\/]+:{1}(((?!\\-))(xn\\-\\-)?[a-z0-9\\-_]{0,61}[a-z0-9]{1,1}\\.)*(xn\\-\\-)?([a-z0-9\\-]{1,61}|[a-z0-9\\-]{1,30})\\.[a-z]{2,}$");
    
    /** Type of participant's identifier */
    public static enum IDENTIFIER_TYPE {EMAIL, MATRIX};
    
    /**
     * Check if an e-mail address is valid
     * 
     * @return e-mail is valid
     */
    public static boolean isEmailValid(String emailAddress) {
        return CHECK_EMAIL_REGEX.matcher(emailAddress).matches();
    }
    
    /**
     * Check if a matrix mxid is valid
     * 
     * @param mxid
     * @return
     */
    public static boolean isMXIDValid(String mxid) {
        return CHECK_MXID_REGEX.matcher(mxid).matches();
    }
    
    /**
     * Creates a new participant with an e-mail identifier
     * 
     * @param name
     * @param identifier
     * @return
     * @throws BusException 
     */
    public static Participant createEMailParticipant(String name, String identifier) throws BusException {
        return new Participant(name, identifier, EMAIL_VALIDATOR, IDENTIFIER_TYPE.EMAIL);
    }
    
    /**
     * Creates a new participant with a matrix mxid identifier
     * 
     * @param name
     * @param identifier
     * @return
     * @throws BusException 
     */
    public static Participant createMXIDParticipant(String name, String identifier) throws BusException {
        return new Participant(name, identifier, MXID_VALIDATOR, IDENTIFIER_TYPE.MATRIX);
    }
    
    /** Name */
    private final String          name;

    /** E-mail address */
    private final String          identifier;

    /** Type of identifier */
    private final IDENTIFIER_TYPE identifierType;
   
    /**
    * Creates a new instance with an e-mail identifier
    * 
    * @throws BusException 
    */ 
    public Participant(String name, String identifier) throws BusException {
        this(name, identifier, EMAIL_VALIDATOR, IDENTIFIER_TYPE.EMAIL);
    }
    
    /**
    * Creates a new instance
    * 
    * @throws BusException 
    */ 
    public Participant(String name,
                       String identifier,
                       Predicate<String> identifierValidator,
                       IDENTIFIER_TYPE identifier_type) throws BusException {
        // Check
        if (!identifierValidator.test(identifier)) {
            throw new BusException("Identifier is not valid");
        }
        
        // Store
        this.name = name;
        this.identifier = identifier;
        this.identifierType = identifier_type;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        Participant other = (Participant) obj;
        if (identifier == null) {
            if (other.identifier != null) return false;
        } else if (!identifier.equals(other.identifier)) return false;
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
    public String getIdentifier() {
        return identifier;
    }

    /**
     * Returns the name
     * 
     * @return the name
     */
    public String getName() {
        return name;
    }
    
    /**
     * Returns the identifier type
     * 
     * @return the identifier type
     */
    public IDENTIFIER_TYPE getIdentifierType() {
        return identifierType;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((identifier == null) ? 0 : identifier.hashCode());
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        return result;
    }
    
    /**
     * Validates e-mail addresses
     */
    public static final Predicate<String> EMAIL_VALIDATOR = new Predicate<>() {
        @Override
        public boolean test(String t) {
            return isEmailValid(t);
        }
    };
    
    /**
     * Validates a matrix mxid
     */
    public static final Predicate<String> MXID_VALIDATOR = new Predicate<>() {
        @Override
        public boolean test(String t) {
            return isMXIDValid(t);
        }
    };
}
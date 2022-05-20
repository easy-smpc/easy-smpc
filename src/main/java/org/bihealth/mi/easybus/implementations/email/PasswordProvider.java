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
package org.bihealth.mi.easybus.implementations.email;

/**
 * An interface for providing passwords on-demand
 * @author Felix Wirth
 * @author Fabian Prasser
 */
public interface PasswordProvider {

    /**
     * Returns the password
     * @param settings 
     * @return
     */
    public PasswordsStore getPassword();
    
    /**
     * Stores e-mail passwords
     * 
     * @author Felix Wirth
     *
     */
    public class PasswordsStore {
        /** IMAP password */
        private final String imapPassword;
        
        /** SMTP password */
        private final String smtpPassword; 
        
        /**
         * Creates a new instance. If smtpPassword is null, imapPassword will be assumed as smtpPassword
         * 
         * @param imapPassword
         * @param smtpPassword
         */
        public PasswordsStore(String imapPassword, String smtpPassword) {
            
            // Check
            if(imapPassword == null || imapPassword.isBlank()) {
                throw new IllegalArgumentException("IMAP password must not be null");
            }
            
            // Store
            this.imapPassword = imapPassword;            
            this.smtpPassword = smtpPassword != null && !smtpPassword.isBlank() ? smtpPassword : imapPassword;            
        }

        /**
         * @return the imapPassword
         */
        protected String getIMAPPassword() {
            return imapPassword;
        }

        /**
         * @return the smtpPassword
         */
        protected String getSMTPPassword() {
            return smtpPassword;
        }
    }
}

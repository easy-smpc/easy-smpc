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
package org.bihealth.mi.easysmpc.dataexport;

//TODO is this class correct here or rather an inner class?
/**
 * Containing connections details for a mail box
 * 
 * @author Felix Wirth
 *
 */
public class EmailConnection {
    
    /** Imap server path */
    private String imap;
    /** smtp server path */
    private String smtp;
    /** User name */
    private String user;
    /** User password */
    private String passwd;

    /**
     * Creates a new instance
     * 
     * @param imap
     * @param smtp
     * @param user
     * @param passwd
     */
    public EmailConnection(String imap, String smtp, String user, String passwd) {
        this.imap = imap;
        this.smtp = smtp;
        this.user = user;
        this.passwd = passwd;
    }
    
    /**
     * @return the imap
     */
    public String getImap() {
        return imap;
    }

    /**
     * @return the smtp
     */
    public String getSmtp() {
        return smtp;
    }

    /**
     * @return the user
     */
    public String getUser() {
        return user;
    }

    /**
     * @return the passwd
     */
    public String getPasswd() {
        return passwd;
    }
}

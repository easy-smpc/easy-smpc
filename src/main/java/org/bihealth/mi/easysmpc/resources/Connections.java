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
package org.bihealth.mi.easysmpc.resources;

import java.util.ArrayList;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import org.bihealth.mi.easybus.implementations.email.ConnectionIMAPSettings;
import org.bihealth.mi.easysmpc.AppPasswordProvider;

/**
 * Store and retrieve connections
 * 
 * @author Felix Wirth
 */
public class Connections {

    /** Key */
    private static final String IMAP_SERVER_KEY      = "imap_server";
    /** Key */
    private static final String IMAP_PORT_KEY        = "imap_port";
    /** Key */
    private static final String IMAP_ENCRYPTION_TYPE = "imap_encryption";
    /** Key */
    private static final String SMTP_SERVER_KEY      = "smtp_server";
    /** Key */
    private static final String SMTP_PORT_KEY        = "smtp_port";
    /** Key */
    private static final String SMTP_ENCRYPTION_TYPE = "smtp_encryption";
    /** Key */
    private static final String SMTP_EMAIL_KEY       = "smtp_email_address";
    /** Key */
    private static final String IMAP_USER_NAME_KEY   = "imap_user_name";
    /** Key */
    private static final String SMTP_USER_NAME_KEY   = "smtp_user_name";
    /** Key */
    private static final String IMAP_AUTH_MECH_KEY   = "imap_auth_mech";
    /** Key */
    private static final String SMTP_AUTH_MECH_KEY   = "smtp_auth_mech_key";


    /**
     * Adds or updates a certain setting
     * @param settings
     */
    public static void addOrUpdate(ConnectionIMAPSettings settings) {
        
        // Get node
        Preferences node = Preferences.userRoot()
                                      .node(Connections.class.getPackage().getName())
                                      .node(settings.getIMAPEmailAddress());
        
        // Add details
        node.put(SMTP_EMAIL_KEY, settings.getSMTPEmailAddress());
        node.put(IMAP_SERVER_KEY, settings.getIMAPServer());
        node.putInt(IMAP_PORT_KEY, settings.getIMAPPort());
        node.put(SMTP_SERVER_KEY, settings.getSMTPServer());
        node.putInt(SMTP_PORT_KEY, settings.getSMTPPort());
        node.putBoolean(IMAP_ENCRYPTION_TYPE, settings.isSSLTLSIMAP());
        node.putBoolean(SMTP_ENCRYPTION_TYPE, settings.isSSLTLSSMTP());
        if(settings.getIMAPUserName() != null) node.put(IMAP_USER_NAME_KEY, settings.getIMAPUserName());
        if(settings.getSMTPUserName() != null) node.put(SMTP_USER_NAME_KEY, settings.getSMTPUserName());
        if(settings.getIMAPAuthMechanisms() != null) node.put(IMAP_AUTH_MECH_KEY, settings.getIMAPAuthMechanisms());
        if(settings.getSMTPAuthMechanisms() != null) node.put(SMTP_AUTH_MECH_KEY, settings.getSMTPAuthMechanisms());
    }
    
    /**
     * Lists all available settings
     * @return
     * @throws BackingStoreException
     */
    public static ArrayList<ConnectionIMAPSettings> list() throws BackingStoreException{
        
        // Prepare
        ArrayList<ConnectionIMAPSettings> result = new ArrayList<>();
        Preferences rootPreferences = Preferences.userRoot().node(Connections.class.getPackage().getName());
        
        // Loop each sub node
        for(String children : rootPreferences.childrenNames()) {
            Preferences child = rootPreferences.node(children);
            result.add(new ConnectionIMAPSettings(children, new AppPasswordProvider(), child.get(SMTP_EMAIL_KEY, children), new AppPasswordProvider())
                       .setIMAPServer(child.get(IMAP_SERVER_KEY, null))
                                                           .setIMAPPort(child.getInt(IMAP_PORT_KEY, 0))
                                                           .setSSLTLSIMAP(child.getBoolean(IMAP_ENCRYPTION_TYPE, true))
                                                           .setSMTPServer(child.get(SMTP_SERVER_KEY, null))
                                                           .setSMTPPort(child.getInt(SMTP_PORT_KEY, 0))
                                                           .setSSLTLSSMTP(child.getBoolean(SMTP_ENCRYPTION_TYPE, true))
                                                           .setIMAPUserName(child.get(IMAP_USER_NAME_KEY, null))
                                                           .setSMTPUserName(child.get(SMTP_USER_NAME_KEY, null))
                                                           .setIMAPAuthMechanisms(child.get(IMAP_AUTH_MECH_KEY, null))
                                                           .setSMTPAuthMechanisms(child.get(SMTP_AUTH_MECH_KEY, null)));
        }
        
        // Return
        return result;
    }
    
    /**
     * Removes a certain setting
     * @param settings
     * @throws BackingStoreException
     */
    public static void remove(ConnectionIMAPSettings settings) throws BackingStoreException {
        Preferences.userRoot()
                   .node(Connections.class.getPackage().getName())
                   .node(settings.getIMAPEmailAddress())
                   .removeNode();
    }    
}
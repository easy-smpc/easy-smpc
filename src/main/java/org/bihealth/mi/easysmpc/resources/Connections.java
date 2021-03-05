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

/**
 * Store and retrieve connections
 * 
 * @author Felix Wirth
 */
public class Connections {

    /** Key */
    private static final String PASSWORD_KEY    = "password";
    /** Key */
    private static final String IMAP_SERVER_KEY = "imap_server";
    /** Key */
    private static final String IMAP_PORT_KEY   = "imap_port";
    /** Key */
    private static final String SMTP_SERVER_KEY = "smtp_server";
    /** Key */
    private static final String SMTP_PORT_KEY   = "smtp_port";

    /**
     * Adds a certain setting
     * @param settings
     */
    public static void add(ConnectionIMAPSettings settings) {
        
        // Get node
        Preferences node = Preferences.userRoot()
                                      .node(Connections.class.getPackage().getName())
                                      .node(settings.getEmailAddress());
        
        // Add details
        node.put(PASSWORD_KEY, settings.getPassword());
        node.put(IMAP_SERVER_KEY, settings.getIMAPServer());
        node.putInt(IMAP_PORT_KEY, settings.getIMAPPort());
        node.put(SMTP_SERVER_KEY, settings.getSMTPServer());
        node.putInt(SMTP_PORT_KEY, settings.getSMTPPort());
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
            result.add(new ConnectionIMAPSettings(children).setPassword(child.get(PASSWORD_KEY, null))
                                                           .setIMAPServer(child.get(IMAP_SERVER_KEY, null))
                                                           .setIMAPPort(child.getInt(IMAP_PORT_KEY, 0))
                                                           .setSMTPServer(child.get(SMTP_SERVER_KEY, null))
                                                           .setSMTPPort(child.getInt(SMTP_PORT_KEY, 0)));
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
                   .node(settings.getEmailAddress())
                   .removeNode();
    }    
}
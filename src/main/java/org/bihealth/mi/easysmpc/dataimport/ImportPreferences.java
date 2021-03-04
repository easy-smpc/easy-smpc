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
package org.bihealth.mi.easysmpc.dataimport;

import java.util.ArrayList;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import org.bihealth.mi.easybus.implementations.email.ConnectionIMAPSettings;

/**
 * Allows the importing of E-Mail connection details from Java Preferences
 * 
 * @author Felix Wirth
 *
 */
public class ImportPreferences {
    
    private static final String PASSWORD_KEY = "password";
    private static final String IMAP_SERVER_KEY = "imap_server";
    private static final String IMAP_PORT_KEY = "imap_port";
    private static final String SMTP_SERVER_KEY = "stmp_server";
    private static final String SMTP_PORT_KEY = "stmp_port";

    public static ArrayList<ConnectionIMAPSettings> getConnectionIMAPSettings() throws BackingStoreException{
        // Prepare
        ArrayList<ConnectionIMAPSettings> result = new ArrayList<>();
        Preferences rootPreferences = Preferences.userRoot().node(ImportPreferences.class.getPackage().getName());
        
        // Loop each sub node
        for(String childrenName : rootPreferences.childrenNames()) {
            Preferences children = rootPreferences.node(childrenName);
            result.add(new ConnectionIMAPSettings(childrenName).setPassword(children.get(PASSWORD_KEY, null))
                                                               .setIMAPServer(children.get(IMAP_SERVER_KEY, null))
                                                               .setIMAPPort(children.getInt(IMAP_PORT_KEY, 0))
                                                               .setSMTPServer(children.get(SMTP_SERVER_KEY, null))
                                                               .setSMTPPort(children.getInt(SMTP_PORT_KEY, 0)));
        }
        
        // Return
        return result;
    }
    
    public static void removeConnectionIMAPSetting(ConnectionIMAPSettings settings) throws BackingStoreException {
        Preferences.userRoot().node(ImportPreferences.class.getPackage().getName())
                                       .node(settings.getEmailAddress()).removeNode();
    }
    
    public static void setConnectionIMAPSetting(ConnectionIMAPSettings settings) {
        // Get child node
        Preferences childNode = Preferences.userRoot()
                                       .node(ImportPreferences.class.getPackage().getName())
                                       .node(settings.getEmailAddress());
        
        // Add details
        childNode.put(PASSWORD_KEY, settings.getPassword());
        childNode.put(IMAP_SERVER_KEY, settings.getIMAPServer());
        childNode.putInt(IMAP_PORT_KEY, settings.getIMAPPort());
        childNode.put(SMTP_SERVER_KEY, settings.getSMTPServer());
        childNode.putInt(SMTP_PORT_KEY, settings.getSMTPPort());
    }    
}
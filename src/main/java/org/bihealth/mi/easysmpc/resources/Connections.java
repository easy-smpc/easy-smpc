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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Base64;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.bihealth.mi.easybus.ConnectionSettings;

/**
 * Store and retrieve connections
 * 
 * @author Felix Wirth
 */
public class Connections {

    /**
     * Adds or updates a specific setting
     * @param settings
     * @throws IOException 
     */
    public static void addOrUpdate(ConnectionSettings settings) throws IOException {
        // Get node
        Preferences node = Preferences.userRoot()
                                      .node(settings.getClass().getPackage().getName());

        // Add details
        node.put(settings.getIdentifier(), serializeConnectionSettings(settings));
    }

    /**
     * Lists all available settings
     * @param class1 
     * @return
     * @throws BackingStoreException
     * @throws IOException 
     * @throws ClassNotFoundException 
     */
    public static ArrayList<ConnectionSettings> list(Class<?> settingsClass) throws BackingStoreException, ClassNotFoundException, IOException{
        
        // Prepare
        ArrayList<ConnectionSettings> result = new ArrayList<>();
        Preferences rootPreferences = Preferences.userRoot().node(settingsClass.getPackage().getName());
        
        // Loop each sub node
        for(String children : rootPreferences.childrenNames()) {
            result.add(deserializeConnectionSettings(rootPreferences.get(children, null)));
        }
        
        // Return
        return result;
    }
    
    /**
     * Removes a certain setting
     * @param settings
     * @throws BackingStoreException
     */
    public static void remove(ConnectionSettings settings) throws BackingStoreException {
        Preferences.userRoot()
                   .node(settings.getClass().getPackage().getName())
                   .node(settings.getIdentifier())
                   .removeNode();
    }
    
    /**
     * Serializes a ConnectionSettings object
     *
     * @param the settings
     * @return the string
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public static String serializeConnectionSettings(ConnectionSettings settings) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(new GZIPOutputStream(bos));
        oos.writeObject(settings);
        oos.close();
        return Base64.getEncoder().encodeToString(bos.toByteArray());
    }

    /**
     * Deserialize a ConnectionSettings object
     *
     * @param the string
     * @return the settings
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws ClassNotFoundException the class not found exception
     */
    public static ConnectionSettings deserializeConnectionSettings(String serialized) throws IOException, ClassNotFoundException {
        byte[] data = Base64.getDecoder().decode(serialized);
        ObjectInputStream ois = new ObjectInputStream(new GZIPInputStream(new ByteArrayInputStream(data)));
        ConnectionSettings settings = (ConnectionSettings) ois.readObject();
        ois.close();
        return settings;
    }
}
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
package org.bihealth.mi.easysmpc.nogui;


import org.bihealth.mi.easybus.implementations.email.ConnectionIMAPSettings;
/**
 * Starts a performance test without GUI
 * 
 * @author Felix Wirth
 *
 */
public class Start {    

    /**
     * 
     * Starts the performance test
     *
     * @param args
     */
    public static void main(String[] args)  {
        // Set logging properties from file      
        System.setProperty("log4j2.configurationFile", "src/main/resources/org/bihealth/mi/easysmpc/nogui/log4j2.xml");
        
        // Create connection settings
        ConnectionIMAPSettings connectionIMAPSettings = new ConnectionIMAPSettings("easysmpc.dev@insutec.de").setPassword("3a$ySMPC!")
                .setSMTPServer("smtp.ionos.de")
                .setIMAPServer("imap.ionos.de");
        
        // Start a EasySMPC process
        //new CreatingUser(3, 1, connectionIMAPSettings, 1000);
    }
}
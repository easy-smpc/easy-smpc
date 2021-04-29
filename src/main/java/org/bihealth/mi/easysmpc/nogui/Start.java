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
    
    /** The string part of the logging when starting */
    public static final String LOGGING_START_MESSAGE = "%s; started; %s; participants; %s; bins";
    /** The string part of the logging when finishing */
    public static final String LOGGING_FINISH_MESSAGE = "%s; finished; %s ;%d; duration ; %f; mean";

    /**
     * 
     * Starts the performance test
     *
     * @param args
     */
    public static void main(String[] args)  {
        // Set logging properties from file
        System.setProperty( "java.util.logging.config.file", "logging.properties" );
                System.setProperty( "java.util.logging.config.file", "logging.properties" );

        // Create connection settings
        ConnectionIMAPSettings connectionIMAPSettings = new ConnectionIMAPSettings("easysmpc.dev@gmail.com").setPassword("3a$ySMPC!")
                .setSMTPServer("smtp.gmail.com")
                .setIMAPServer("imap.gmail.com");
        
        // Start a EasySMPC process
        new CreatingUser(3, 1, connectionIMAPSettings, 1000);
    }
}
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


import java.io.IOException;
import java.util.logging.Logger;

import org.bihealth.mi.easybus.implementations.email.ConnectionIMAPSettings;
/**
 * Starts a no gui performance test
 * 
 * @author Felix Wirth
 *
 */
public class Start {    
    
    public static final String LOGGING_START_MESSAGE = "%s; started; %s; participants; %s; bins";
    public static final String LOGGING_FINISH_MESSAGE = "%s; finished; %s; id; %s; result of first bin";

    /**
     * 
     * Starts the performance test
     *
     * @param args
     */
    public static void main(String[] args)  {
        System.setProperty( "java.util.logging.config.file", "logging.properties" );
                System.setProperty( "java.util.logging.config.file", "logging.properties" );
        final Logger log = Logger.getLogger( Start.class.getName() );
        
        log.info( "Start process" );
        ConnectionIMAPSettings connectionIMAPSettings = new ConnectionIMAPSettings("easysmpc.dev@gmail.com").setPassword("3a$ySMPC!")
                .setSMTPServer("smtp.gmail.com")
                .setIMAPServer("imap.gmail.com");   
        new CreatingUser(3, 1, connectionIMAPSettings, 1000);
    }
}

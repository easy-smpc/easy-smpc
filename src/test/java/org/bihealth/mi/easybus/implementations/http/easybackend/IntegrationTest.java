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
package org.bihealth.mi.easybus.implementations.http.easybackend;

import java.net.URI;
import java.util.concurrent.TimeUnit;

import org.bihealth.mi.easybus.Bus;
import org.bihealth.mi.easybus.MessageListener;
import org.bihealth.mi.easybus.Participant;
import org.bihealth.mi.easybus.PasswordStore;
import org.bihealth.mi.easybus.Scope;

/**
 * Basic integration test
 * 
 * @author Felix Wirth
 *
 */
public class IntegrationTest {

    /** Name of file to send */
    public static final String FILENAME = "sampleBig.txt";
    /** Received */
    public static boolean      RECEIVED = false;

    /**
     * Starts the test
     * 
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        
        // Prepare
        Participant sender = new Participant("test", "test@easysmpc.org");
        Participant receiver = new Participant("easy", "easy@easy.de");
        Scope scope = new Scope("MyFancyScope");
        
        // Add to bus creation if needed 
        //ConnectionHTTPProxy.getProxy(new URI("https://matrix-client.matrix.org"));
        
        ConnectionSettingsEasybackend settingsReceiver = new ConnectionSettingsEasybackend(receiver, null).setAuthServer(new URI("http://localhost:9090"))
                                                                                                   .setAPIServer(new URI("http://localhost:8080"))
                                                                                                   .setRealm("master")
                                                                                                   .setClientId("easy-client")
                                                                                                   .setClientSecret("Sg9rJVSaBGjj7nX92lUsqsV4NL3uSLXO");
        settingsReceiver.setPasswordStore(new PasswordStore("easy"));
        
        
        ConnectionSettingsEasybackend settingsSender = new ConnectionSettingsEasybackend(sender,
                                                                                         null).setAuthServer(new URI("http://localhost:9090"))
                                                                                              .setAPIServer(new URI("http://localhost:8080"))
                                                                                              .setRealm("master")
                                                                                              .setClientId("easy-client")
                                                                                              .setClientSecret("Sg9rJVSaBGjj7nX92lUsqsV4NL3uSLXO");
        settingsReceiver.setPasswordStore(new PasswordStore("test"));

        
        // Create receiving bus
        Bus busReceiving = new BusEasybackend(5,
                                         10,
                                         new ConnectionEasybackend(settingsReceiver),
                                         1024);
        busReceiving.receive(scope, receiver, new MessageListener() {
            
            @Override
            public void receiveError(Exception exception) {
                exception.printStackTrace();                
            }
            
            @Override
            public void receive(String message) {
                System.out.println("Received: " + message);
                RECEIVED = true;
            }
        });
        
        
        
        // Create sending bus
        Bus busSend = new BusEasybackend(5,
                                         10,
                                         new ConnectionEasybackend(settingsSender),
                                         1024);
        //busSend.purge();
        busSend.send("My fancy message!",
                     scope,
                     receiver)
               .get(1500000, TimeUnit.MILLISECONDS);

        System.out.println("sent");

       // Wait
      while (!RECEIVED) {
          Thread.sleep(10000);
      }      
    }
}

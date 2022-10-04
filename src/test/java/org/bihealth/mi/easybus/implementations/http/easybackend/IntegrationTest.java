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

import java.net.URL;
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
        System.getProperties().setProperty("java.net.preferIPv6Addresses", "true");

        // Prepare
        Participant sender = new Participant("easysmpc.dev0", "easysmpc.dev0@insutec.de");
        Participant receiver = new Participant("easysmpc.dev1", "easysmpc.dev1@insutec.de");
        Scope scope = new Scope("MyFancyScope");

        // Add to bus creation if needed 
        // ConnectionHTTPProxy.getProxy(new URI("https://matrix-client.matrix.org"));
        
        // Create connections details receiver
        ConnectionSettingsEasyBackend settingsReceiver = new ConnectionSettingsEasyBackend(receiver, null)
                .setAPIServer(new URL("http://127.0.0.1:8080"))
                .setRealm("easybackend")
                .setClientId("easy-client");
        settingsReceiver.setPasswordStore(new PasswordStore("test"));
        
        // Create connections details sender
        ConnectionSettingsEasyBackend settingsSender = new ConnectionSettingsEasyBackend(sender,
                                                                                         null)
                .setAPIServer(new URL("http://127.0.0.1:8080"))
                .setRealm("easybackend")
                .setClientId("easy-client");
        settingsSender.setPasswordStore(new PasswordStore("test"));

        
        // Create receiving bus
        Bus busReceiving = new BusEasyBackend(5,
                                         10,
                                         new ConnectionEasyBackend(settingsReceiver),
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
        Bus busSend = new BusEasyBackend(5,
                                         10,
                                         new ConnectionEasyBackend(settingsSender),
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

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
package org.bihealth.mi.easybus.implementations.http.matrix;

import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.util.Properties;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

import org.bihealth.mi.easybus.Bus;
import org.bihealth.mi.easybus.Message;
import org.bihealth.mi.easybus.MessageListener;
import org.bihealth.mi.easybus.Participant;
import org.bihealth.mi.easybus.Scope;

/**
 * Basic test for bus using matrix
 * 
 * @author Felix Wirth
 *
 */
public class IntegrationTest {

    /** Name of file to send */
    public static final String FILENAME = "sampleSmall.txt";
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
        Properties prop = new Properties();
        prop.load(IntegrationTest.class.getResourceAsStream("Settings.properties"));
        
        // Add to bus creation if needed 
        //ConnectionHTTPProxy.getProxy(new URI("https://matrix-client.matrix.org"));
        
        // Create receiving bus
        Bus busReceive = new BusMatrix(5, 1000, new ConnectionMatrix(new URI(prop.getProperty("matrix.url")),
                                                     Participant.createMXIDParticipant("first",
                                                                                       prop.getProperty("first.user")),
                                                     prop.getProperty("first.password"),
                                                     null));
        //busReceive.purge();
        busReceive.receive(new Scope("MyFancyScope"),
                           Participant.createMXIDParticipant("first",
                                                             prop.getProperty("first.user")),
                           new MessageListener() {
                               @Override
                               public void receiveError(Exception exception) {
                                   throw new IllegalStateException(exception);
                               }

                               @Override
                               public void receive(Message message) {
                                   System.out.println("Received!");

                                   try (FileWriter fw = new FileWriter("out.txt")) {
                                       fw.write((String) message.getMessage());
                                       fw.close();
                                   } catch (IOException e) {
                                       e.printStackTrace();
                                   }
                                   
                                   RECEIVED = true;
                               }
                           });
        
        // Read input file
        StringBuilder builder = new StringBuilder();
        Scanner scanner = new Scanner(IntegrationTest.class.getResourceAsStream(FILENAME));
        while(scanner.hasNext()) {
            builder.append(scanner.nextLine());
        }
        scanner.close();
        String message = builder.toString(); 
        
        // Create sending bus
        Bus busSend = new BusMatrix(5, 5, new ConnectionMatrix(new URI(prop.getProperty("matrix.url")),
                                                         Participant.createMXIDParticipant("second",
                                                                                           prop.getProperty("second.user")),
                                                         prop.getProperty("second.password"),
                                                         null));
        busSend.send(new Message(message),
                     new Scope("MyFancyScope"),
                     Participant.createMXIDParticipant("first", prop.getProperty("first.user")))
               .get(1500000, TimeUnit.MILLISECONDS);

        System.out.println("sent");

       // Wait
      while (!RECEIVED) {
          Thread.sleep(10000);
      }
      
      busReceive.stop();
      busSend.stop();
      System.out.println("Finished");
    }

}

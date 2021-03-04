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
package org.bihealth.mi.easybus.tests;

import org.bihealth.mi.easybus.Message;
import org.bihealth.mi.easybus.MessageListener;
import org.bihealth.mi.easybus.Participant;
import org.bihealth.mi.easybus.Scope;
import org.bihealth.mi.easybus.implementations.email.BusEmail;
import org.bihealth.mi.easybus.implementations.email.ConnectionIMAP;
import org.bihealth.mi.easybus.implementations.email.ConnectionIMAPSettings;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * Simple test class for IMAP
 * 
 * @author Felix Wirth
 */
public class IMAPTest {
    
    /** Login*/
    private static ConnectionIMAPSettings gmailLogin;
    /** Login*/
    private static ConnectionIMAPSettings yandexLogin;
    /** Login*/
    private static ConnectionIMAPSettings insutecLogin;
    /** Login*/
    private static ConnectionIMAPSettings login1;
    /** Login*/
    private static ConnectionIMAPSettings login2;
   
    /** Listener*/
    MessageListener ListenerImplPositive = new MessageListener() {                
        @Override
        public void receive(Message message) {
            System.out.println("Message received: " + message.getMessage());
        }
    };

    /** Listener*/
    MessageListener ListenerImplNegative = new MessageListener() {                
        @Override
        public void receive(Message message) {
            System.out.println("This message should not appear: " + message.getMessage());
        }
    };
    
    @BeforeAll
    public static void setup() throws Exception {
        gmailLogin = new ConnectionIMAPSettings("easysmpc.dev@gmail.com").setPassword("3a$ySMPC!");
        if (!gmailLogin.guess()) {
            throw new IllegalStateException("Could not guess connection settings");
        }
        yandexLogin = new ConnectionIMAPSettings("easysmpc.dev@yandex.com").setPassword("3a$ySMPC!")
                                                                          .setIMAPServer("imap.yandex.com")
                                                                          .setSMTPServer("smtp.yandex.com");
        insutecLogin = new ConnectionIMAPSettings("easysmpc.dev@insutec.de").setPassword("3a$ySMPC!")
                                                                            .setIMAPServer("imap.ionos.de")
                                                                            .setSMTPServer("smtp.ionos.de");
        // Which login to use
        login1 = gmailLogin;
        login2 = insutecLogin;
    }
    
    @Test
    public void testSharedMailbox() throws Exception {
        
        // Create bus
        BusEmail bus = new BusEmail(new ConnectionIMAP(login2,
                                                       true), 1000);

        // Positive test receiver
        bus.receive(new Scope("scope1"),
                    new Participant("part1", "hello@number1.de"),
                    ListenerImplPositive);

        // Negative test receiver
        bus.receive(new Scope("scope2"),
                    new Participant("part1", "hello@number1.de"),
                    ListenerImplNegative);

        // Send message
        bus.send(new Message("My shared mailbox message"),
                 new Scope("scope1"),
                 new Participant("part1", "hello@number1.de"));

        // Wait for test to pass
        Thread.sleep(5000);
    }
    
    @Test
    public void testDistributedMailbox() throws Exception {   

        // Create bus
        BusEmail busSender = new BusEmail(new ConnectionIMAP(login1,
                                                             false), 1000);
        BusEmail busReceiver = new BusEmail(new ConnectionIMAP(login2,
                                                               false), 1000);

        // Positive test receiver
        busReceiver.receive(new Scope("scope1"),
                            new Participant("part1", login2.getEmailAddress()),
                            ListenerImplPositive);

        // Negative test receiver
        busReceiver.receive(new Scope("noMessagesToScope"),
                            new Participant("part1", login2.getEmailAddress()),
                            ListenerImplNegative);
        // Positive test
        busSender.send(new Message("My distributed mailbox message"),
                       new Scope("scope1"),
                       new Participant("part1", login2.getEmailAddress()));
        // Negative test
        busSender.send(new Message("This message should not appear"),
                       new Scope("no subscribed scope"),
                       new Participant("part1", login2.getEmailAddress()));

        // Wait for test to pass
        Thread.sleep(15000);

    }
}

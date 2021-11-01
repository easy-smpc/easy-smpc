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
import org.junit.jupiter.api.Test;

/**
 * Simple test class for IMAP
 * 
 * @author Felix Wirth
 */
public class IMAPTest {
   
    /** Listener*/
    MessageListener ListenerImplPositive = new MessageListener() {                
        @Override
        public void receive(Message message) {
            System.out.println("Message received: " + message.getMessage());
        }

        @Override
        public void receiveError(Exception exception) {
            System.out.println("Error receiving message");
        }
    };

    /** Listener*/
    MessageListener ListenerImplNegative = new MessageListener() {                
        @Override
        public void receive(Message message) {
            System.out.println("This message should not appear: " + message.getMessage());
        }
        
        @Override
        public void receiveError(Exception exception) {
            System.out.println("Error receiving message");
        }
    };
    
    @Test
    public void testSharedMailbox() throws Exception {
        
        ConnectionIMAPSettings login = new ConnectionIMAPSettings("easysmpc.dev@gmail.com").setPassword("3a$ySMPC!");
        if (!login.guess()) {
            throw new IllegalStateException("Could not guess connection settings");
        }
        
        // Create bus
        BusEmail bus = new BusEmail(new ConnectionIMAP(login,
                                                       false), 1000);

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

}

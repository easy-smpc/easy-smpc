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

import java.util.concurrent.ExecutionException;

import org.bihealth.mi.easybus.Bus;
import org.bihealth.mi.easybus.BusException;
import org.bihealth.mi.easybus.Message;
import org.bihealth.mi.easybus.MessageListener;
import org.bihealth.mi.easybus.Participant;
import org.bihealth.mi.easybus.Scope;
import org.bihealth.mi.easybus.implementations.local.BusLocal;
import org.junit.jupiter.api.Test;

/**
 * Test local bus
 * 
 * @author Felix Wirth
 */
public class LocalTest {
    @Test
    public void test() throws BusException {
        
        // Bus
        Bus bus = new BusLocal();
        
        // Participant
        Participant part1 = new Participant("number 1", "hello@number1.de");
        
        // Scopes
        Scope scope1ForPart1 = new Scope("scope1");
        // Scope scope2ForPart1 = new Scope("scope2");
        // Scope scope1ForPart2 = new Scope("scope1");
        Scope scope1ForPart3 = new Scope("scope1");
        
        // Subscriptions 
        bus.receive(scope1ForPart1, part1, new ReceiverImplPositive());
        // Positive tests
        try {
            bus.send(new Message("message specific to part1"),
                     scope1ForPart3,
                     new Participant("number 1", "hello@number1.de")).get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    
        // Negative tests
        try {
            bus.send(new Message("message for non existing scope"),
                     new Scope("9999"),
                     new Participant("number 1", "hello@number1.de")).get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }
    
    // This would be done by a user of the API
    class ReceiverImplPositive implements MessageListener {
        @Override
        public void receive(Message message) {
            System.out.println("Message is: " + (String) message.getMessage());
        }
    };
    
    class ReceiverImplPositiveBroadcast implements MessageListener {
        @Override
        public void receive(Message message) {
            System.out.println("This message should only be a brodcast message: " + (String) message.getMessage());
        }
    };
    
    class ReceiverImplNegative implements MessageListener {
        @Override
        public void receive(Message message) {
            System.out.println("This message should not appear: " + (String) message.getMessage());
        }
    };
}

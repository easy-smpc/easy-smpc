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
import java.math.BigInteger;

import org.bihealth.mi.easybus.BusException;
import org.bihealth.mi.easybus.MessageListener;
import org.bihealth.mi.easybus.Scope;
import org.bihealth.mi.easybus.implementations.email.BusEmail;
import org.bihealth.mi.easybus.implementations.email.ConnectionIMAP;
import org.bihealth.mi.easybus.implementations.email.ConnectionIMAPSettings;

import de.tu_darmstadt.cbs.emailsmpc.Message;
import de.tu_darmstadt.cbs.emailsmpc.MessageInitial;
import de.tu_darmstadt.cbs.emailsmpc.Participant;

/**
 * A participant in an EasySMPC process
 * 
 * @author Felix Wirth
 *
 */
public class ParticipatingUser extends User {
    /** Stores the bit length of the big integer */
    private int lengthBitBigInteger;
    /** Interim bus for initial e-mail receiving  */
    private BusEmail interimBus;
    

    /**
     * Creates a new instance
     * 
     * @param studyUID
     * @param ownParticipant
     * @param connectionIMAPSettings
     * @param lengthBitBigInteger
     */
    public ParticipatingUser(String studyUID,
                             Participant ownParticipant,
                             ConnectionIMAPSettings connectionIMAPSettings,
                             int lengthBitBigInteger,
                             int mailBoxCheckInterval) {
        super(mailBoxCheckInterval);
        
        this.lengthBitBigInteger = lengthBitBigInteger;
        
        try {
            // Register for initial e-mail
            interimBus = new BusEmail(new ConnectionIMAP(connectionIMAPSettings, true),
                                      mailBoxCheckInterval);
            interimBus.receive(new Scope(studyUID + ROUND_0),
                                        new org.bihealth.mi.easybus.Participant(ownParticipant.name,
                                                                                ownParticipant.emailAddress),
                                        new MessageListener() {
                                            @Override
                                            public void receive(org.bihealth.mi.easybus.Message message) {
                                                // Spawns the following steps in an own thread
                                                Thread thread = new Thread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        receiveInitialEMail(message);
                                                    }
                                                });
                                                thread.setDaemon(false);
                                                thread.start();
                                                
                                                // Stop interim bus
                                                interimBus.stop();
                                            }
                                        });
        } catch (BusException e) {
            throw new IllegalStateException("Unable to register to receive initial e-mails", e);
        }
    }

    private void receiveInitialEMail(org.bihealth.mi.easybus.Message message) {
        
        try {
            // Get data
            String data = Message.deserializeMessage((String) message.getMessage()).data;

            // Init model
            setModel(MessageInitial.getAppModel(MessageInitial.decodeMessage(Message.getMessageData(data))));

            // Proceed to entering value
            getModel().toEnteringValues(data);

            // Set own values and proceed
            getModel().toSendingShares(fillBins(lengthBitBigInteger));
            
            // Starts the common steps 
            proceedCommonProcessSteps();
            
        } catch (ClassNotFoundException | IllegalArgumentException | IllegalStateException | IOException e) {
            throw new IllegalStateException("Unable to execute particpating users steps" , e);
        }
        
    }
    /**
     * Fills the bins with random numbers
     * 
     * @return bins
     */
    private BigInteger[] fillBins(int lengthBitBigInteger) {
        // Init
        BigInteger[] result = new BigInteger[getModel().bins.length];
        
        // Set a random big integer for each
        for (int index = 0; index < result.length; index++) {
            result[index] = generateRandomBigInteger(lengthBitBigInteger);
        }
        
        // Return
        return result;
    }
}
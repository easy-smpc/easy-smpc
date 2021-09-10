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
import java.io.Serializable;
import java.math.BigInteger;
import java.util.Date;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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
    /** ConnectionIMAPSettings */
    private ConnectionIMAPSettings connectionIMAPSettings;
    /** Logger */
    private static Logger logger = LogManager.getLogger(ParticipatingUser.class); 

    /**
     * Creates a new instance
     * 
     * @param studyUID
     * @param ownParticipant
     * @param connectionIMAPSettings
     * @param lengthBitBigInteger
     */
    public ParticipatingUser(ParticipatingUserData participatingUserData) {                
   
        // Store
        super(participatingUserData.mailBoxCheckInterval, participatingUserData.isSharedMailbox);
        this.lengthBitBigInteger = participatingUserData.lengthBitBigInteger;
        this.connectionIMAPSettings = participatingUserData.connectionIMAPSettings;
        
        // Init time recording
        RecordTimeDifferences.addStartValue(participatingUserData.studyUID, participatingUserData.participantId, System.nanoTime());
        
        try {
            // Register for initial e-mail
            final BusEmail interimBus = new BusEmail(new ConnectionIMAP(connectionIMAPSettings, false, false, false),
                                      participatingUserData.mailBoxCheckInterval);
            interimBus.receive(new Scope(participatingUserData.studyUID + ROUND_0),
                                        new org.bihealth.mi.easybus.Participant(participatingUserData.ownParticipant.name,
                                                                                participatingUserData.ownParticipant.emailAddress),
                                        new MessageListener() {
                                            @Override
                                            public void receive(org.bihealth.mi.easybus.Message message) {
                                                // Stop interim bus
                                                interimBus.stop();
                                                
                                                // Spawns the following steps in an own thread
                                                Thread thread = new Thread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        receiveInitialEMail(message);
                                                    }
                                                });
                                                thread.setDaemon(false);
                                                thread.start();
                                            }
                                        });
        } catch (BusException e) {
            logger.error("Unable to register to receive initial e-mails logged", new Date(), "Unable to register to receive initial e-mails", ExceptionUtils.getStackTrace(e));
            throw new IllegalStateException("Unable to register to receive initial e-mails", e);
        }
    }

    private void receiveInitialEMail(org.bihealth.mi.easybus.Message message) {
        
        try {
            // Get data
            String data = Message.deserializeMessage((String) message.getMessage()).data;

            // Init model
            setModel(MessageInitial.getAppModel(MessageInitial.decodeMessage(Message.getMessageData(data))));
            getModel().setConnectionIMAPSettings(this.connectionIMAPSettings);
            
            // Proceed to entering value
            getModel().toEnteringValues(data);

            // Set own values and proceed
            getModel().toSendingShares(fillBins(lengthBitBigInteger));
            
            // Starts the common steps 
            proceedCommonProcessSteps();
            
        } catch (ClassNotFoundException | IllegalArgumentException | IllegalStateException | IOException e) {
            logger.error("Unable to execute particpating users steps logged", new Date(), "Unable to execute particpating users steps", ExceptionUtils.getStackTrace(e));
            throw new IllegalStateException("Unable to execute particpating users steps" , e);
        }
        
    }
    /**
     * Fills the bins with random numbers
     * 
     * @param lengthBitBigInteger
     * @return
     */
    private BigInteger[] fillBins(int lengthBitBigInteger) {
        // Init
        BigInteger[] result = new BigInteger[getModel().getBins().length];
        
        // Set a random big integer for each
        for (int index = 0; index < result.length; index++) {
            result[index] = generateRandomBigInteger(lengthBitBigInteger);
        }
        
        // Return
        return result;
    }
    
    /**
     * Class containing data to create a participant
     * 
     * @author Felix Wirth
     *
     */
    protected static class ParticipatingUserData implements Serializable {

        /** SVUID */
        private static final long serialVersionUID = -573296130735855246L;
        /** studyUID */
        private final String                 studyUID;
        /** ownParticipant */
        private final Participant            ownParticipant;
        /** participantId */
        private final int                    participantId;
        /** connectionIMAPSettings */
        private final ConnectionIMAPSettings connectionIMAPSettings;
        /** connectionIMAPSettings */
        private final int                    lengthBitBigInteger;
        /** connectionIMAPSettings */
        private final int                    mailBoxCheckInterval;
        /** connectionIMAPSettings */
        private final boolean                isSharedMailbox;

        /**
         * Create a new instance
         * 
         * @param studyUID
         * @param ownParticipant
         * @param participantId
         * @param connectionIMAPSettings
         * @param lengthBitBigInteger
         * @param mailBoxCheckInterval
         * @param isSharedMailbox
         */
        public ParticipatingUserData(String studyUID,
                                     Participant ownParticipant,
                                     int participantId,
                                     ConnectionIMAPSettings connectionIMAPSettings,
                                     int lengthBitBigInteger,
                                     int mailBoxCheckInterval,
                                     boolean isSharedMailbox) {
            // Store
            this.studyUID = studyUID;
            this.ownParticipant = ownParticipant;
            this.participantId = participantId;
            this.connectionIMAPSettings = connectionIMAPSettings;
            this.lengthBitBigInteger = lengthBitBigInteger;
            this.mailBoxCheckInterval = mailBoxCheckInterval;
            this.isSharedMailbox = isSharedMailbox;
        }
    }
}
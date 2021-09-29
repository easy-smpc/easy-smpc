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
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.Random;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bihealth.mi.easybus.BusException;
import org.bihealth.mi.easybus.MessageListener;
import org.bihealth.mi.easybus.Scope;
import org.bihealth.mi.easysmpc.dataimport.ImportClipboard;
import org.bihealth.mi.easysmpc.resources.Resources;

import de.tu_darmstadt.cbs.emailsmpc.Bin;
import de.tu_darmstadt.cbs.emailsmpc.Message;
import de.tu_darmstadt.cbs.emailsmpc.Study;
import de.tu_darmstadt.cbs.emailsmpc.Study.StudyState;

/**
 * A user in an EasySMPC process
 * 
 * @author Felix Wirth
 *
 */
public abstract class User implements MessageListener {
    /** The length of a generated string */
    public final int FIXED_LENGTH_STRING = 10;
    /** The length of a generated big integer */
    public final int FIXED_LENGTH_BIT_BIGINTEGER = 31;
    /** Round for initial e-mails */
    public final String ROUND_0 = "_round0";
    /** Logger */
    private static Logger logger;       
    /** The study model */
    private Study model = new Study();
    /** The random object */
    private final Random random = new Random();
    /** The mailbox check interval in  milliseconds */
    private final int mailBoxCheckInterval;
    /** Is shared mailbox used? */
    private boolean isSharedMailbox;
    
    
    /**
     * Creates a new instance
     * 
     * @param mailBoxCheckInterval
     */
    User(int mailBoxCheckInterval, boolean isSharedMailbox) {
        this.mailBoxCheckInterval = mailBoxCheckInterval;
        this.isSharedMailbox = isSharedMailbox;
        logger = LogManager.getLogger(User.class);        
     }
    
    /**
     * Proceeds the SMPC steps which are the same for participating and creating user
     */
    protected void proceedCommonProcessSteps() {
        
        try {
            // Sends the messages for the first round and proceeds the model
            sendMessages(Resources.ROUND_1);
            this.model.toRecievingShares();
            logger.debug("1. round sending finished logged", new Date(), getModel().getStudyUID(), "1. round sending finished for", getModel().getOwnId());
            
            // Receives the messages for the first round and proceeds the model
            receiveMessages(Resources.ROUND_1);
            this.model.toSendingResult();
            logger.debug("1. round receiving finished logged", new Date(), getModel().getStudyUID(), "1. round receiving finished for", getModel().getOwnId());
            
            // Sends the messages for the second round and proceeds the model
            sendMessages(Resources.ROUND_2);
            this.model.toRecievingResult();
            logger.debug("2. round sending finished logged", new Date(), getModel().getStudyUID(), "2. round sending finished for", getModel().getOwnId());
            
            // Receives the messages for the second round and finalizes the model
            receiveMessages(Resources.ROUND_2);            
            RecordTimeDifferences.finished(getModel().getStudyUID(), this.model.getOwnId(), System.nanoTime());
            this.model.toFinished();
            logger.debug("Result logged", new Date(), getModel().getStudyUID(), "result", getModel().getOwnId(), "participantid", getModel().getAllResults()[0].name, "result name", getModel().getAllResults()[0].value, "result");
            
        } catch (IllegalStateException | IllegalArgumentException | IOException | BusException e) {
            logger.error("Unable to process common process steps logged", new Date(), "Unable to process common process steps", ExceptionUtils.getStackTrace(e));
            throw new IllegalStateException("Unable to process common process steps", e);
        }
    }
    
    /**
     * Receives a message by means of e-mail bus
     * 
     * @param roundIdentifier
     * @throws IllegalArgumentException
     * @throws BusException
     */
    private void receiveMessages(String roundIdentifier) throws IllegalArgumentException,
                                                         BusException {
        getModel().getBusTestMode(this.mailBoxCheckInterval, this.isSharedMailbox).receive(new Scope(getModel().getStudyUID() + roundIdentifier),
                           new org.bihealth.mi.easybus.Participant(getModel().getParticipantFromId(getModel().getOwnId()).name,
                                                                   getModel().getParticipantFromId(getModel().getOwnId()).emailAddress),
                           this);
        
        // Wait for all shares
        while (!areSharesComplete()) {

            // Proceed if shares complete
            if (!getModel().isBusAlive()) {
                logger.error("Bus is not alive anymore!", new Date(), "Bus is not alive anymore!");
            }
            
            // Wait
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                logger.error("Interrupted exception logged", new Date(), "Interrupted exception logged", ExceptionUtils.getStackTrace(e));
            }             
        }
        
        // Stop bus
        getModel().stopBus();        
    }


    /** 
     * Sends a message by means of e-mail bus
     * 
     * @param roundIdentifier
     */
    private void sendMessages(String roundIdentifier) {
        
        // Loop over participants
        for (int index = 0; index < getModel().getNumParticipants(); index++) {
            
            // Only proceed if not own user
            if (index != getModel().getOwnId()) {

                try {
                    // Retrieve bus and send message
                    getModel().getBusTestMode(0, this.isSharedMailbox).send(new org.bihealth.mi.easybus.Message(Message.serializeMessage(getModel().getUnsentMessageFor(index))),
                                    new Scope(getModel().getStudyUID() + (getModel().getState() == StudyState.INITIAL_SENDING ? ROUND_0 : roundIdentifier)),
                                    new org.bihealth.mi.easybus.Participant(getModel().getParticipants()[index].name,
                                                                            getModel().getParticipants()[index].emailAddress),
                                    new org.bihealth.mi.easybus.Participant(getModel().getParticipants()[getModel().getOwnId()].name,
                                                                            getModel().getParticipants()[getModel().getOwnId()].emailAddress));
                    // Mark message as sent
                    model.markMessageSent(index);
                } catch (BusException | IOException e) {
                    logger.error("Unable to send e-mail logged", new Date(), "Unable to send e-mail", ExceptionUtils.getStackTrace(e));
                    throw new IllegalStateException("Unable to send e-mail!", e);
                }
            }
        }
    }

    /**
     * Gets the model
     * 
     * @return the model
     */
    public Study getModel() {
        return model;
    }
    
    /**
     * Sets the model
     * 
     * @param the model
     */
    public void setModel(Study model) {
        this.model = model;
    }
    
    /**
     * Generates a string with random letters
     * 
     * @param string length
     * @return generated string
     */
    protected String generateRandomString(int stringLength) {
        
        // Generates and returns a letter  between "a" (97) and "z" (122)        
        return  new Random().ints(97, 122 + 1)
                .limit(stringLength)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();
    }
    
    /**
     * Generates a random big integer
     * 
     * @param bit length of big integer
     * @return
     * @throws IllegalArgumentException
     */
    protected BigInteger generateRandomBigInteger(int bitLength) throws IllegalArgumentException {
        // Check
        if (bitLength < 2) throw new IllegalArgumentException("Bitlength must be larger than 2");
        
        // Random integer
        BigInteger value = new BigInteger(bitLength - 1, random);
        
        // Swap sign? 
        byte[] randomByte = new byte[1];
        random.nextBytes(randomByte);
        int signum = Byte.valueOf(randomByte[0]).intValue() & 0x01;
        if (signum == 1) value = value.negate();
        
        // Return
        return value;
      }
    
    @Override
    public void receive(org.bihealth.mi.easybus.Message message) {
        String messageStripped = ImportClipboard.getStrippedExchangeMessage((String) message.getMessage());
        
        // Check if valid
        if (isMessageShareResultValid(messageStripped)) {
            try {
                // Set message
                model.setShareFromMessage(Message.deserializeMessage(messageStripped));
            } catch (IllegalStateException | IllegalArgumentException | NoSuchAlgorithmException | ClassNotFoundException | IOException e) {
                logger.error("Unable to digest message logged", new Date(), "Unable to digest message", ExceptionUtils.getStackTrace(e));
            }
        }
    }
  
  /**
   * Check whether message is valid
   * 
   * @param text
   * @return
   */
    public boolean isMessageShareResultValid(String text) {
        // Check not null or empty
        if (model == null || text == null || text.trim().isEmpty()) { return false; }
        
        // Check message
        try {
            return model.isMessageShareResultValid(Message.deserializeMessage(text));
        } catch (Exception e) {
            return false;
        }
    }
  
    /**
     * Are shares complete to proceed?
     * 
     * @return
     */
    private boolean areSharesComplete() {
        for (Bin b : this.model.getBins()) {
            if (!b.isComplete()) return false;
        }
        return true;
    }

    /**
     * @return the mailBoxCheckInterval
     */
    public int getMailboxCheckInterval() {
        return mailBoxCheckInterval;
    }
    
    /**
     * @return is a shared mailbox used?
     */
    public boolean isSharedMailbox() {
        return isSharedMailbox;
    }
    
    /**
     * Is the process finished?
     * 
     * @return
     */
    public boolean isProcessFinished() {
        return getModel().getState() == StudyState.FINISHED;
    }
    
    /**
     * Is study state none?
     * 
     * @return
     */
    public boolean isStudyStateNone() {
        return (getModel() == null || getModel().getState() == null || getModel().getState() == StudyState.NONE);
    }    
}
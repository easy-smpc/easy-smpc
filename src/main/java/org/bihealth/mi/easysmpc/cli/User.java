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
package org.bihealth.mi.easysmpc.cli;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bihealth.mi.easybus.BusException;
import org.bihealth.mi.easybus.MessageListener;
import org.bihealth.mi.easybus.Scope;
import org.bihealth.mi.easybus.implementations.email.ConnectionIMAPSettings;
import org.bihealth.mi.easysmpc.dataexport.ExportFile;
import org.bihealth.mi.easysmpc.dataimport.ImportClipboard;
import org.bihealth.mi.easysmpc.resources.Resources;

import de.tu_darmstadt.cbs.emailsmpc.Bin;
import de.tu_darmstadt.cbs.emailsmpc.BinResult;
import de.tu_darmstadt.cbs.emailsmpc.Message;
import de.tu_darmstadt.cbs.emailsmpc.Study;
import de.tu_darmstadt.cbs.emailsmpc.Study.StudyState;

/**
 * A user in an EasySMPC process
 * 
 * @author Felix Wirth
 * @author Fabian Prasser
 */
public class User implements MessageListener {

    /** Logger */
    private static final Logger    LOGGER  = LogManager.getLogger(User.class);
    /** Round for initial e-mails */
    public static final String     ROUND_0 = "_round0";
    /** The study model */
    private Study                  model   = new Study();
    /** The mailbox check interval in milliseconds */
    private final int              mailBoxCheckInterval;
    /** connectionIMAPSettings */
    private ConnectionIMAPSettings connectionIMAPSettings;
    /** Error flag */
    private boolean                error   = false;

    /**
     * Creates a new instance for creating users
     * 
     * @param mailboxCheckInterval
     * @param connectionIMAPSettings
     */
    public User(int mailboxCheckInterval, ConnectionIMAPSettings connectionIMAPSettings) {

        this.mailBoxCheckInterval = mailboxCheckInterval;
        this.connectionIMAPSettings = connectionIMAPSettings;
    }   
    
    /**
     * @return the mailBoxCheckInterval
     */
    public int getMailboxCheckInterval() {
        return mailBoxCheckInterval;
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
     * Is the process finished?
     * 
     * @return
     */
    public boolean isProcessFinished() {
        return getModel().getState() == StudyState.FINISHED;
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
                LOGGER.error("Unable to digest message", e);
            }
        }
    }
    
    @Override
    public void receiveError(Exception e) {
        LOGGER.error("Error receiveing e-mails",e);
        this.error = true;
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
     * Check whether message is valid
     * 
     * @param text
     * @return
     */
    private boolean isMessageShareResultValid(String text) {
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
     * Receives a message by means of e-mail bus
     * 
     * @param roundIdentifier
     * @throws IllegalArgumentException
     * @throws BusException
     */
    private void receiveMessages(String roundIdentifier) throws IllegalArgumentException, BusException {
        getModel().getBus(this.mailBoxCheckInterval, false).receive(new Scope(getModel().getName() + roundIdentifier),
                           new org.bihealth.mi.easybus.Participant(getModel().getParticipantFromId(getModel().getOwnId()).name,
                                                                   getModel().getParticipantFromId(getModel().getOwnId()).emailAddress),
                           this);
        
        // Wait for all shares
        while (!areSharesComplete()) {
            
            // Check for error while receiving and throw exeception
            if(this.error) {
                throw new IllegalArgumentException("Error receiving e-mails!");
            }
            
            // Proceed if shares complete
            if (!getModel().isBusAlive()) {
                LOGGER.error("Bus is not alive anymore!");
            }
            
            // Wait
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                LOGGER.error("Sleep of bus interrupted", e);
            }             
        }     
    }
    
    /** 
     * Sends a message by means of e-mail bus
     * 
     * @param roundIdentifier
     */
    private void sendMessages(String roundIdentifier) {
        
        // Prepare
        FutureTask<Void> future = null;
        
        // Loop over participants
        for (int index = 0; index < getModel().getNumParticipants(); index++) {
            
            
            // Only proceed if not own user
            if (index != getModel().getOwnId()) {

                try {
                    // Retrieve bus and send message
                    future = getModel().getBus(this.mailBoxCheckInterval, false).send(new org.bihealth.mi.easybus.Message(Message.serializeMessage(getModel().getUnsentMessageFor(index))),
                                    new Scope(getModel().getName() + (getModel().getState() == StudyState.INITIAL_SENDING ? ROUND_0 : roundIdentifier)),
                                    new org.bihealth.mi.easybus.Participant(getModel().getParticipants()[index].name,
                                                                            getModel().getParticipants()[index].emailAddress));
                    
                    // Wait for result with a timeout time
                    future.get(Resources.TIMEOUT_SEND_EMAILS, TimeUnit.MILLISECONDS);
                    
                    // Mark message as sent
                    model.markMessageSent(index);
                } catch (Exception e) {
                     future.cancel(true);
                    LOGGER.error("Unable to send e-mail" ,e);
                    throw new IllegalStateException("Unable to send e-mail!", e);
                }
            }
        }
    }

    /**
     * Proceeds the SMPC steps which are the same for participating and creating user
     */
    protected void performCommonSteps() {
        
        try {
            // Sends the messages for the first round and proceeds the model
            sendMessages(Resources.ROUND_1);
            this.model.toRecievingShares();
            LOGGER.info(String.format("1. round sending finished for study %s", getModel().getName()));
            
            // Receives the messages for the first round and proceeds the model
            receiveMessages(Resources.ROUND_1);
            this.model.toSendingResult();
            LOGGER.info(String.format("1. round receiving finished for study %s", getModel().getName()));
            
            // Sends the messages for the second round and proceeds the model
            sendMessages(Resources.ROUND_2);
            this.model.toRecievingResult();
            LOGGER.info(String.format("2. round sending finished for study %s", getModel().getName()));
            
            // Receives the messages for the second round, stops the bus and finalizes the model
            receiveMessages(Resources.ROUND_2);
            getModel().stopBus();
            this.model.toFinished();
            LOGGER.info(String.format("2. round receiving finished for study %s", getModel().getName()));
            
            // Write result
            LOGGER.info("Start calculating and writing result");
            exportResult();
            
            // Log finished
            LOGGER.info(String.format("Process completed sucessfully. Please see result file %s", createResultFileName()));
            
        } catch (IllegalStateException | IllegalArgumentException | IOException | BusException e) {
            LOGGER.error("Unable to process common process steps", e);
            try {
                this.model.getBus().stop();
            } catch (BusException e1) {
                // Ignore
            }
        }
    }

    /**
     * Export result to file
     */
    private void exportResult() {
        // Load data into list
        List<List<String>> list = new ArrayList<>();
        for (BinResult result : getModel().getAllResults()) {
            list.add(new ArrayList<String>(Arrays.asList(result.name,
                                                         String.valueOf(result.value))));
        }
        
        // Export
        try {
            ExportFile.toFile(new File(createResultFileName()))
                      .exportData(list);
        } catch (IOException e) {
            LOGGER.error("Unable to write result file", e);
        }
    }

    /**
     * Create the file name for the result file
     * 
     * @return
     */
    private String createResultFileName() {
        return "result_" + getModel().getName() + "_" +
               DateTimeFormatter.ofPattern("yyyy.MM.dd HH.mm.ss").format(LocalDateTime.now()) +
               ".xlsx";
    }

    /**
     * Sets the model
     * 
     * @param the model
     */
    protected void setModel(Study model) {
        this.model = model;
    }
    
    /**
     * Get connectionIMAPSettings
     * 
     * @return
     */
    protected ConnectionIMAPSettings getConnectionIMAPSettings() {
        return connectionIMAPSettings;
    }
}
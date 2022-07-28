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
import java.util.Scanner;
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
public class UserProcess implements MessageListener {

    /** Logger */
    private static final Logger    LOGGER  = LogManager.getLogger(UserProcess.class);
    /** Round for initial e-mails */
    public static final String     ROUND_0 = "_round0";
    /** The study model */
    private Study                  model   = new Study();
    /** connectionIMAPSettings */
    private ConnectionIMAPSettings connectionIMAPSettings;
    /** Error flag */
    private boolean                stop    = false;

    /**
     * Creates a new instance
     * 
     * @param mailboxCheckInterval
     * @param connectionIMAPSettings
     */
    protected UserProcess(ConnectionIMAPSettings connectionIMAPSettings) {

        // Store
        this.connectionIMAPSettings = connectionIMAPSettings;               
    }
    
    
    
    /**
     * Create a new instance from an existing model
     * 
     * @param file
     * @throws IOException 
     * @throws IllegalArgumentException 
     * @throws ClassNotFoundException 
     */
    public UserProcess(Study model, int mailboxCheckInterval) throws ClassNotFoundException, IllegalArgumentException, IOException {
        this(model.getConnectionIMAPSettings());
        
        // Store
        this.model = model;
                
        // Spawns the common steps in an own thread
        LOGGER.info("Restart process");
        new Thread(new Runnable() {
            public void run() {
                performCommonSteps();
            }
        }).start();
    }
    
    /**
     * Creates a key board listener thread to allow for stop processing
     * 
     * @param thread
     */
    private void registerKeyboardListenerThread() {
        
        // Create thread
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {

                Scanner scanner = new Scanner(System.in);                
                while (true) {
                    // Check if stop is necessary
                    if (scanner.hasNext() && scanner.next().equals(Resources.STOP_CLI_PROCESS_STRING)) {
                        break;
                    }

                    // Sleep
                    try {
                        Thread.sleep(300);
                    } catch (InterruptedException e) {
                        // Ignore
                    }
                }
                
                // Initiate shutdown
                scanner.close();
                stop = true;
            }
        });
        
        // Start thread and log
        thread.setDaemon(true);
        thread.start();        
        LOGGER.info(String.format("Enter \"%s\" to stop processing at the next possible step", Resources.STOP_CLI_PROCESS_STRING));
    }
    
    /**
     * Stops the process
     */
    protected void shutdown() {
        // Set stop flag
        this.stop = true;
        
        // Stop bus
        try {
            this.model.getBus().stop();
        } catch (BusException e1) {
            // Ignore
        }
        
        // Save latest state
        save();
        
        // Last log entry
        LOGGER.info(String.format("Process stopped. Restart with \"-resume -d %s\"", model.getFilename()));
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
    public void receive(String message) {
        String messageStripped = ImportClipboard.getStrippedExchangeMessage(message);
        
        // Check if valid
        if (isMessageShareResultValid(messageStripped)) {
            try {
                // Set message
                model.setShareFromMessage(Message.deserializeMessage(messageStripped));
                
                // Save
                save();
            } catch (IllegalStateException | IllegalArgumentException | NoSuchAlgorithmException | ClassNotFoundException | IOException e) {
                LOGGER.error("Unable to digest message", e);
            }
        }
    }
    
    @Override
    public void receiveError(Exception e) {
        LOGGER.error("Error receiveing e-mails",e);
        LOGGER.info("Receiveing will be retried",e);
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
     * Starts receiving a message by means of e-mail bus
     * 
     * @param roundIdentifier
     * @throws IllegalArgumentException
     * @throws BusException
     * @throws InterruptedException 
     */
    private void receiveMessages(String roundIdentifier) throws IllegalArgumentException, BusException, InterruptedException {
        getModel().getBus(getModel().getConnectionIMAPSettings().getCheckInterval(), false).receive(new Scope(getModel().getName() + roundIdentifier),
                           new org.bihealth.mi.easybus.Participant(getModel().getParticipantFromId(getModel().getOwnId()).name,
                                                                   getModel().getParticipantFromId(getModel().getOwnId()).emailAddress),
                           this);
        
        // Wait for all shares
        while (!areSharesComplete()) {
            
            // Check for error while receiving and throw exception
            if (this.stop) {
                throw new InterruptedException("Process stopped");
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
     * @throws InterruptedException 
     */
    private void sendMessages(String roundIdentifier) throws InterruptedException {
        
        // Prepare
        FutureTask<Void> future = null;
        
        // Loop over participants
        for (int index = 0; index < getModel().getNumParticipants(); index++) {
            
            
            // Only proceed if not own user
            if (index != getModel().getOwnId()) {

                // Check for error while receiving and throw exception
                if (this.stop) {
                    throw new InterruptedException("Process stopped");
                }
                
                // Check if message has been sent already
                if (getModel().getUnsentMessageFor(index) == null) {
                    continue;
                }                
                
                try {
                    // Retrieve bus and send message

                    future = getModel().getBus(getModel().getConnectionIMAPSettings().getCheckInterval(), false).send(Message.serializeMessage(getModel().getUnsentMessageFor(index)),
                                    new Scope(getModel().getName() + (getModel().getState() == StudyState.INITIAL_SENDING ? ROUND_0 : roundIdentifier)),
                                    new org.bihealth.mi.easybus.Participant(getModel().getParticipants()[index].name,
                                                                            getModel().getParticipants()[index].emailAddress));
                    
                    // Wait for result with a timeout time                    
                    future.get(getModel().getConnectionIMAPSettings().getEmailSendTimeout(), TimeUnit.SECONDS);
                    
                    // Mark message as sent
                    model.markMessageSent(index);

                    // Save
                    save();
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
            
            // Register keyboard listener to stop
            registerKeyboardListenerThread();
            
            // Sends the messages for the first round and proceeds the model
            if ((model.getState() == StudyState.INITIAL_SENDING || model.getState() == StudyState.SENDING_SHARE) && !this.stop) {
                LOGGER.info(String.format("1. round sending started for study %s", getModel().getName()));
                sendMessages(Resources.ROUND_1);
                this.model.toRecievingShares();
                this.save();
                LOGGER.info(String.format("1. round sending finished for study %s", getModel().getName()));
            }
            
            // Receives the messages for the first round and proceeds the model
            if (model.getState() == StudyState.RECIEVING_SHARE && !this.stop) {
                LOGGER.info(String.format("1. round receiving started for study %s", getModel().getName()));
                receiveMessages(Resources.ROUND_1);
                this.model.toSendingResult();
                this.save();
                LOGGER.info(String.format("1. round receiving finished for study %s", getModel().getName()));
            }
            
            // Sends the messages for the second round and proceeds the model
            if (getModel().getState() == StudyState.SENDING_RESULT && !this.stop) {
                LOGGER.info(String.format("2. round sending started for study %s", getModel().getName()));
                sendMessages(Resources.ROUND_2);
                this.model.toRecievingResult();
                this.save();
                LOGGER.info(String.format("2. round sending finished for study %s", getModel().getName()));
            }
            
            // Receives the messages for the second round, stops the bus and finalizes the model
            if (getModel().getState() == StudyState.RECIEVING_RESULT && !this.stop) {
                LOGGER.info(String.format("2. round receiving started for study %s", getModel().getName()));
                receiveMessages(Resources.ROUND_2);
                getModel().stopBus();
                this.model.toFinished();
                this.save();
                LOGGER.info(String.format("2. round receiving finished for study %s", getModel().getName()));
            }
            
            // Calculate & write result, delete file model
            if (getModel().getState() == StudyState.FINISHED) {
                LOGGER.info("Start calculating and writing result");
                exportResult();
                model.getFilename().delete();
            }
            
            // Log finished
            LOGGER.info(String.format("Process completed sucessfully. Please see result file %s", createResultFileName()));
            
        } catch (IllegalStateException | IllegalArgumentException | IOException | BusException e) {
            // Log and shutdown
            LOGGER.error("Unable to process common process steps", e);
            shutdown();
        } catch (InterruptedException e) {
            // Log and shutdown
            LOGGER.info("Execution stopped");
            shutdown();
        }
    }

    /**
     * Export result to file
     */
    private void exportResult() {
        // Load data into list
        List<List<String>> list = new ArrayList<>();
        for (BinResult result : getModel().getAllResults()) {
            list.add(new ArrayList<String>(Arrays.asList(result.name, String.valueOf(result.value))));
        }
        
        // Export
        try {
            ExportFile.toFile(new File(createResultFileName())).exportData(list);
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
        return "result_" + getModel().getName() + "_" + DateTimeFormatter.ofPattern("yyyy.MM.dd HH.mm.ss").format(LocalDateTime.now()) + ".xlsx";
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
    
    /**
     * Tries to save the current state and logs in case of an error
     * 
     * @return
     */
    protected void save() {
        
        // Ensure filename
        if (model.getFilename() == null) {
            model.setFilename(new File(getModel().getName() + "." + Resources.FILE_ENDING));
        }
        
        // Try saving
        try {
            this.model.saveProgram();
        } catch (IllegalStateException | IOException e) {
            LOGGER.error("Unable to save interim state. Program execution is proceeded but state will be lost if the programm stops", e);
        }
    }
}
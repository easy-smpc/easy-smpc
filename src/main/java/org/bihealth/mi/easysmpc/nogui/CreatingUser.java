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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.Scanner;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bihealth.mi.easysmpc.nogui.ParticipatingUser.ParticipatingUserData;

import de.tu_darmstadt.cbs.emailsmpc.Bin;
import de.tu_darmstadt.cbs.emailsmpc.Participant;

/**
 * A creating user in an EasySMPC process
 * 
 * @author Felix Wirth
 *
 */
public class CreatingUser extends User {
    
    /** All participating users */
    private final List<User> participatingUsers = new ArrayList<>();
    /** All participating users processes */
    private final List<Process> participatingUsersProcesses = new ArrayList<>();   
    /** Logger */
    Logger logger = LogManager.getLogger(CreatingUser.class);
    
    /**
     * Create a new instance
     * 
     * @param numberParticipants
     * @param numberBins
     * @param mailBoxDetails
     * @param separatedProcesses 
     * @throws IllegalStateException
     */
    CreatingUser(int numberParticipants,
                 int numberBins,
                 int mailBoxCheckInterval,
                 MailboxDetails mailBoxDetails, boolean separatedProcesses) throws IllegalStateException {
        super(mailBoxCheckInterval, mailBoxDetails.isSharedMailbox());

        try {          
            // Set model to starting
            getModel().toStarting();          
            
            // Init model with generated study name, participants and bins 
            getModel().toInitialSending(generateRandomString(FIXED_LENGTH_STRING),
                                        generateParticpants(numberParticipants, mailBoxDetails, FIXED_LENGTH_STRING),
                                        generateBins(numberBins,numberParticipants, FIXED_LENGTH_STRING, FIXED_LENGTH_BIT_BIGINTEGER), mailBoxDetails.getConnection(0));
            // Init recoding
            RecordTimeDifferences.init(getModel(), mailBoxCheckInterval, System.nanoTime());
        } catch (IOException | IllegalStateException e) {
            logger.error("Unable to init logged", new Date(), "Unable to init", ExceptionUtils.getStackTrace(e));
            throw new IllegalStateException("Unable to init study!", e);
        }
        
        // Spawn participants
        createParticipants(FIXED_LENGTH_BIT_BIGINTEGER, mailBoxDetails, separatedProcesses);
        
        // Spawns the common steps in an own thread
        new Thread(new Runnable() {
            @Override
            public void run() {
                proceedCommonProcessSteps();
            }
        }).start();
        
        new Thread(new Runnable() {
            @Override
            public void run() {
                Scanner scanner = new Scanner(System.in);
                while (true) {
                    
                    if (scanner.nextLine().equals("s")) {
                        scanner.close();
                        stopAllProcesses();
                    }
                    // Wait
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        logger.error("Interrupted exception logged",
                                     new Date(),
                                     "Interrupted exception logged",
                                     ExceptionUtils.getStackTrace(e));
                    }
                }
            }
        }).start();
    }

    /**
     * Stop all processes
     */
    protected void stopAllProcesses() {
        // Init
        boolean allStopped = false;

        // Repeat until all spawned processes are stopped
        while (!allStopped) {
            allStopped = true;
            for (Process process : participatingUsersProcesses) {
                if (process.isAlive()) {
                    allStopped = false;
                    process.destroy();
                }
            }
        }        

        // Log
        logger.debug("Stopped all processes logged", new Date(), "Stopped all processes");

        // Stop own process
        System.exit(0);
    }

    /**
     * Create participants
     * 
     * @param lengthBitBigInteger
     * @param separatedProcesses
     * @param connectionIMAPSettings 
     */
    private void createParticipants(int lengthBitBigInteger,
                                    MailboxDetails mailBoxDetails,
                                    boolean separatedProcesses) {
        // Loop over participants
        for(int index = 1; index < getModel().numParticipants; index++) {
            ParticipatingUserData userData =  new ParticipatingUserData(getModel().studyUID,
                                  getModel().participants[index],
                                  index,
                                  mailBoxDetails.getConnection(index),
                                  lengthBitBigInteger,
                                  getMailboxCheckInterval(),
                                  mailBoxDetails.isSharedMailbox());
            
            if (separatedProcesses) {                
                try {
                    // Serialize data
                    ByteArrayOutputStream bos = new ByteArrayOutputStream();
                    ObjectOutputStream oos = new ObjectOutputStream(bos);
                    oos.writeObject(userData);
                    oos.close();
                    // Start process
                    // TODO fix dependencies
                    ProcessBuilder processBuilder = new ProcessBuilder("java",
//                                                                 "-Xdebug",
//                                                                 "-Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=500" + index,
                                                                "-cp", 
                                                                "./target/classes;./target/dependency/*",
                                                                "org.bihealth.mi.easysmpc.nogui.ParticipatingUser",
                                                                Base64.getEncoder().encodeToString(bos.toByteArray()));
                    participatingUsersProcesses.add(processBuilder.start());
                } catch (IOException e) {
                    throw new IllegalStateException("Unable to serialze data!", e);
                }               
                
            } else {
                // Create user as new thread
                participatingUsers.add(new ParticipatingUser(userData));
            }
        }
    }

    /**
     * Generate the involved participants
     * 
     * @param numberParticipants
     * @param connectionIMAPSettings 
     * @param stringLength length of names and e-mail address parts
     * @return
     */
    private Participant[] generateParticpants(int numberParticipants, MailboxDetails mailBoxDetails, int stringLength) {
        // Init result
        Participant[] result = new Participant[numberParticipants];
        
        // Init each participant with a generated name and generated mail address
        for(int index = 0; index < numberParticipants; index++) {
            
            // Generate either am email address or use an actual address 
            String emailAddress = isSharedMailbox()
                    ? generateRandomString(15) + "@" + generateRandomString(10) + ".org"
                    : mailBoxDetails.getConnection(index).getEmailAddress();
            
            // Create participant   
            result[index] = new Participant(generateRandomString(15), emailAddress);
        }
        
        // Return
        return result;
    }


    /**
     * Generate bins
     * 
     * @param numberBins number of bins
     * @param numberParties number of involved parties/users
     * @param stringLength length of bin name
     * @param bigIntegerBitLength length of generated big integer
     * @return
     */
    protected Bin[] generateBins(int numberBins,
                                 int numberParties,
                                 int stringLength,
                                 int bigIntegerBitLength) {
        // Init result bin array
        Bin[] result = new Bin[numberBins];
        
        // Init each bin and set generated secret value of creating user
        for (int index = 0; index < numberBins; index++) {
            result[index] = new Bin(generateRandomString(stringLength), numberParties);
            result[index].shareValue(generateRandomBigInteger(bigIntegerBitLength));
        }
        
        // Return
        return result;
    }
    
    /**
     * Are all users finished?
     * 
     * @return
     */
    public boolean areAllUsersFinished() {        
        
        // Check for this creating users
        if (!isProcessFinished()) {
            return false;
        }
        
        // Check for all participating users
        for(User user : participatingUsers) {
            if(!user.isProcessFinished()) {
                return false;
            }
        }
        
        // Check for all participating users
        for(Process process : participatingUsersProcesses) {
            if(process.isAlive()) {
                return false;
            }
        }
        
        // Return all
        return true;
    }
}
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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bihealth.mi.easysmpc.nogui.ParticipatingUser.ParticipatingUserData;
import org.bihealth.mi.easysmpc.resources.Resources;

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
                 MailboxDetails mailBoxDetails,
                 ResultPrinter printer) throws IllegalStateException {
        super(mailBoxCheckInterval, mailBoxDetails.isSharedMailbox());

        try {          
            // Set model to starting
            getModel().toStarting();          
            
            // Init model with generated study name, participants and bins 
            getModel().toInitialSending(generateRandomString(FIXED_LENGTH_STRING),
                                        generateParticpants(numberParticipants, mailBoxDetails, FIXED_LENGTH_STRING),
                                        generateBins(numberBins,numberParticipants, FIXED_LENGTH_STRING, FIXED_LENGTH_BIT_NUMBER), mailBoxDetails.getConnection(0));
            // Init recoding
            setRecording(new RecordTimeDifferences(getModel(), mailBoxCheckInterval, System.nanoTime(), mailBoxDetails.getTracker(), printer));
        } catch (IOException | IllegalStateException e) {
            logger.error("Unable to init logged", new Date(), "Unable to init", ExceptionUtils.getStackTrace(e));
            throw new IllegalStateException("Unable to init study!", e);
        }
        
        // Spawn participants
        createParticipants(FIXED_LENGTH_BIT_NUMBER, mailBoxDetails);
        
        // Spawns the common steps in an own thread
        new Thread(new Runnable() {
            @Override
            public void run() {
                proceedCommonProcessSteps();
            }
        }).start();             
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
        
        // Return all
        return true;
    }

    /**
     * Create participants
     * 
     * @param lengthBitBigInteger
     * @param separatedProcesses
     * @param connectionIMAPSettings 
     */
    private void createParticipants(int lengthBitBigInteger,
                                    MailboxDetails mailBoxDetails) {
        // Loop over participants
        for(int index = 1; index < getModel().getNumParticipants(); index++) {
            // Create data
            ParticipatingUserData userData =  new ParticipatingUserData(getModel().getStudyUID(),
                                  getModel().getParticipants()[index],
                                  index,
                                  mailBoxDetails.getConnection(index),
                                  lengthBitBigInteger,
                                  getMailboxCheckInterval(),
                                  mailBoxDetails.isSharedMailbox(),
                                  getRecording());
            
            // Create user as new thread
            participatingUsers.add(new ParticipatingUser(userData));
        }
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
            result[index].shareValue(generateRandomBigDecimal(bigIntegerBitLength), Resources.FRACTIONAL_BITS);
        }
        
        // Return
        return result;
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
    
}
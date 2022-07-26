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

import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bihealth.mi.easybus.BusException;
import org.bihealth.mi.easybus.implementations.email.BusEmail;
import org.bihealth.mi.easybus.implementations.email.ConnectionIMAP;
import org.bihealth.mi.easybus.implementations.email.ConnectionIMAPSettings;
import org.bihealth.mi.easysmpc.resources.Resources;

import de.tu_darmstadt.cbs.emailsmpc.Bin;
import de.tu_darmstadt.cbs.emailsmpc.Participant;

/**
 * A creating user in an EasySMPC process
 * 
 * @author Felix Wirth
 * @author Fabian Prasser
 */
public class UserProcessCreating extends UserProcess {

    /** Logger */
    private static final Logger LOGGER = LogManager.getLogger(UserProcessCreating.class);

    /**
     * Create a new instance
     * 
     * @param studyTitle
     * @param participants
     * @param binsNames
     * @param data
     * @param connectionIMAPSettings
     * @param mailboxCheckInterval
     * @throws IllegalStateException
     */
    public UserProcessCreating(String studyTitle,
                        Participant[] participants,
                        Map<String, String> binsNames,
                        Map<String, String> data,
                        ConnectionIMAPSettings connectionIMAPSettings,
                        int mailboxCheckInterval) throws IllegalStateException {

        super(connectionIMAPSettings);
        
        // Check
        if (participants == null || binsNames == null || participants.length < 3 || binsNames.size() < 1) {
            throw new IllegalArgumentException("Please provide at least three participants and one bin!");
        }
        
        // Delete pre-existing bus emails
        try {
            LOGGER.info("Start deleting pre-existing e-mails");
            purgeEmails();
        } catch (BusException | InterruptedException e) {
            LOGGER.error("Unable to delete pre-existing e-mails", e);
        }  
        
        try {
            // Set model to starting
            getModel().toStarting();
            
            // Init model with generated study name, participants and bins            
            getModel().toInitialSending(studyTitle, participants, createBinsFromMaps(binsNames, participants.length, data), connectionIMAPSettings);
            LOGGER.info(String.format("Started process for project %s with %d participants and %d variables",
                                      getModel().getName(),
                                      getModel().getNumParticipants(),
                                      getModel().getBins().length));
            
            // Save state
            save();

        } catch (IOException | IllegalStateException e) {
            LOGGER.error("Unable to init study", e);
            throw new IllegalStateException("Unable to init study!", e);
        }

        // Spawns the common steps in an own thread
        new Thread(new Runnable() {
            @Override
            public void run() {
                performCommonSteps();
            }
        }).start();
    }

    /**
     * Create bins from a map
     * 
     * @param binsNamesMap
     * @param numberParticipants
     * @return
     */
    public static Bin[] createBinsFromMaps(Map<String, String> binsNamesMap, int numberParticipants, Map<String, String> dataMap) {
        // Init
        Bin[] bins = new Bin[binsNamesMap.size()];
        int i = 0;
        Map<String, String> workingCopyData = new HashMap<>();
        workingCopyData.putAll(dataMap);
        
        // Create bins
        for (Entry<String, String> binsNameEntry : binsNamesMap.entrySet()) {
            // Init new bin
            bins[i] = new Bin(binsNameEntry.getKey());
            bins[i].initialize(numberParticipants);

            // Set either zero or the data found in data map
            if (dataMap.get(binsNameEntry.getKey()) != null) {
                try {
                    bins[i].shareValue(new BigDecimal(dataMap.get(binsNameEntry.getKey()).trim().replace(',', '.')),
                                       Resources.FRACTIONAL_BITS);
                } catch (NumberFormatException e) {
                    LOGGER.error(String.format("Unable to understand value %s for variable %s", dataMap.get(binsNameEntry.getKey()), binsNameEntry.getKey()));
                }
                workingCopyData.remove(binsNameEntry.getKey());
            } else {
                bins[i].shareValue(BigDecimal.ZERO, Resources.FRACTIONAL_BITS);
            }

            i++;
        }
        
        // Warning about unmapped variables
        for (Entry<String, String> entry : workingCopyData.entrySet()) {
            LOGGER.warn(String.format("Data for variable \"%s\" was provided, but variable was not found in variable definition", entry.getKey()));
        }
        
        // Return
        return bins;
    }
    
    /**
     * Deletes all pre-existing email in the mailbox which are related to the bus
     * 
     * @throws BusException 
     * @throws InterruptedException 
     */
    private void purgeEmails() throws BusException, InterruptedException {
        BusEmail bus = new BusEmail(new ConnectionIMAP(getConnectionIMAPSettings(), false), 1000);
        bus.purgeEmails();
        bus.stop();
    }

    /**
     * Creates an array of participants object from a string in the form name1,email1;name2,email1 ...
     * 
     * @param String
     * @return
     */
    public static Participant[] createParticipantsFromCSVString(String participantsCSV) {
        // Init
        String[] participantSplit = participantsCSV.split(";");
        Participant[] result = new Participant[participantSplit.length];

        // Loop over participants
        int index = 0;
        for (String nameEMail : participantSplit) {
            String nameEMailSplit[] = nameEMail.split(",");

            // Check
            if (nameEMailSplit.length != 2) {
                throw new IllegalArgumentException(String.format("Participant/e-mail part s% is incorrectly formated", nameEMail));
            }

            // Add participant an increase index
            result[index] = new Participant(nameEMailSplit[0], nameEMailSplit[1]);
            index++;
        }

        // Return
        return result;
    }
}
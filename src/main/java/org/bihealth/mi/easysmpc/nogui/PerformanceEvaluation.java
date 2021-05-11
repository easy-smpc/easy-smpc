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


import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bihealth.mi.easybus.Bus;
import org.bihealth.mi.easybus.BusException;
import org.bihealth.mi.easybus.implementations.email.BusEmail;
import org.bihealth.mi.easybus.implementations.email.ConnectionIMAP;
import org.bihealth.mi.easybus.implementations.email.ConnectionIMAPSettings;
import org.bihealth.mi.easysmpc.resources.Resources;
/**
 * Starts a performance test without GUI
 * 
 * @author Felix Wirth
 *
 */
public class PerformanceEvaluation {
    /** File path */
    public static final String FILEPATH = "performanceEvaluation";
    /** CSV printer */
    public static CSVPrinter csvPrinter;
    
    /** Logger */
    private static Logger logger;
    
    /**
     * 
     * Starts the performance test
     *
     * @param args
     * @throws IOException 
     */
    public static void main(String[] args) throws IOException  {        
        // Create parameters
        int rounds = 10;
        List<Integer> participants = new ArrayList<>(Arrays.asList(new Integer[] {3, 5, 10, 20}));
        List<Integer> bins = new ArrayList<>(Arrays.asList(new Integer[] {10, 100, 1000, 10000}));
        List<Integer> mailboxCheckInterval = new ArrayList<>(Arrays.asList(new Integer[] {1000, 5000, 10000, 20000}));
        
//          List<Integer> participants = new ArrayList<>(Arrays.asList(new Integer[] {20}));
//          List<Integer> bins = new ArrayList<>(Arrays.asList(new Integer[] {10000}));
//          List<Integer> mailboxCheckInterval = new ArrayList<>(Arrays.asList(new Integer[] {1000, 3000, 5000, 10000}));

        
        // Create connection settings
        ConnectionIMAPSettings connectionIMAPSettings = new ConnectionIMAPSettings("easysmpc.dev@insutec.de").setPassword("3a$ySMPC!")
                .setSMTPServer("smtp.ionos.de")
                .setIMAPServer("imap.ionos.de");
        
        for(int i = 0; i < rounds; i++) {
            new PerformanceEvaluation(participants, bins, mailboxCheckInterval, connectionIMAPSettings);   
        }
    }

    /**
     * Creates a new instance
     * 
     * @param participants
     * @param bins
     * @param mailboxCheckInterval
     * @param connectionIMAPSettings 
     * @throws IOException 
     */
    public PerformanceEvaluation(List<Integer> participants,
                                 List<Integer> bins,
                                 List<Integer> mailboxCheckIntervals, ConnectionIMAPSettings connectionIMAPSettings) throws IOException {
        // Prepare
        try {
            prepare(connectionIMAPSettings);
        } catch (IOException | BusException | InterruptedException e) {
            throw new IllegalStateException("Unable to prepare performance evaluation", e);
        }                
        
        // Permutation of parameters
        for (int participantNumber : participants) {
            for (int binNumber : bins) {
                for (int mailboxCheckInterval : mailboxCheckIntervals) {
                    
                    // Start a EasySMPC process
                    CreatingUser user = new CreatingUser(participantNumber, binNumber, mailboxCheckInterval, connectionIMAPSettings);
                    
                    // Wait to finish
                    while (!user.areAllUsersFinished()) {                        
                        try {
                            Thread.sleep(Resources.INTERVAL_SCHEDULER_MILLISECONDS);
                        } catch (InterruptedException e) {
                            // Ignore
                        }
                    }
                    Bus.resetStatistics();
                    int waitTime = (participantNumber * 1000 * 60) / 4;
                    logger.debug("Wait logged", new Date(), "Started waiting for",  waitTime);
                    try {
                        Thread.sleep(waitTime);
                    } catch (InterruptedException e) {
                        throw new IllegalStateException("Unable to wait between processed");
                    }
                }
            }
        }
    }

    /**
     * Prepare evaluation
     * @param connectionIMAPSettings 
     * 
     * @throws IOException 
     * @throws BusException 
     * @throws InterruptedException 
     */
    private void prepare(ConnectionIMAPSettings connectionIMAPSettings) throws IOException, BusException, InterruptedException {
        // Set logging properties from file      
        System.setProperty("log4j2.configurationFile", "src/main/resources/org/bihealth/mi/easysmpc/nogui/log4j2.xml");
        logger = LogManager.getLogger(PerformanceEvaluation.class);
        
        // Delete existing e-mails      
        BusEmail bus = new BusEmail( new ConnectionIMAP(connectionIMAPSettings, true), 0);
        bus.purgeEmails();
        bus.stop();
        
        // Create csv printer
        boolean skipHeader = new File(FILEPATH).exists();
        csvPrinter = new CSVPrinter(Files.newBufferedWriter(Paths.get(FILEPATH),StandardOpenOption.APPEND, StandardOpenOption.CREATE), CSVFormat.DEFAULT
                                                     .withHeader("Date",
                                                                 "StudyUID",
                                                                 "Number participants",
                                                                 "Number bins",
                                                                 "Mailbox check interval",
                                                                 "Fastest processing time",
                                                                 "Slowest processing time",
                                                                 "Mean processing time",
                                                                 "Number messages received",
                                                                 "Total size messages received",
                                                                 "Number messages sent",
                                                                 "Total size messages sent")
                                                     .withSkipHeaderRecord(skipHeader));
        
        logger.debug("Finished preparation logged", new Date(), "Finished preparation");
    }
}
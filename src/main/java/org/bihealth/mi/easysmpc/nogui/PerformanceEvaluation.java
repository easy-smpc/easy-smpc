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
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bihealth.mi.easybus.Bus;
import org.bihealth.mi.easybus.BusException;
import org.bihealth.mi.easybus.implementations.email.BusEmail;
import org.bihealth.mi.easybus.implementations.email.ConnectionIMAP;
import org.bihealth.mi.easybus.implementations.email.ConnectionIMAPSettings;
import org.bihealth.mi.easysmpc.nogui.Combinator.Combination;
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
    /** Was preparation executed*/
    private static boolean prepared = false;
    
    /**
     * 
     * Starts the performance test
     *
     * @param args
     * @throws IOException 
     */
    public static void main(String[] args) throws IOException  {        
        // Create parameters
        List<Integer> participants = new ArrayList<>(Arrays.asList(new Integer[] {3, 5, 10, 20}));
        List<Integer> bins = new ArrayList<>(Arrays.asList(new Integer[] {100, 1000, 10000}));
        List<Integer> mailboxCheckInterval = new ArrayList<>(Arrays.asList(new Integer[] {1000, 5000, 10000, 20000}));
        boolean isSharedMailbox = false;
        Combinator combinator =
//                new RandomCombinator(participants, bins, mailboxCheckInterval);
                new RepeatPermuteCombinator(participants, bins, mailboxCheckInterval, 2);
        
        // Create connection settings
//      ConnectionIMAPSettings connectionIMAPSettings = new ConnectionIMAPSettings("easysmpc.dev@insutec.de").setPassword("3a$ySMPC!")
//                .setSMTPServer("smtp.ionos.de")
//                .setIMAPServer("imap.ionos.de");
//        ConnectionIMAPSettings connectionIMAPSettings = new ConnectionIMAPSettings("easysmpc.dev@yahoo.de").setPassword("jjyhafmgqazaawge")
//                .setSMTPServer("imap.mail.yahoo.com")
//                .setIMAPServer("smtp.mail.yahoo.com");
        ConnectionIMAPSettings connectionIMAPSettings =
                new ConnectionIMAPSettings("hmail" + MailboxDetails.INDEX_REPLACE + "@easysmpc.org")
//                new ConnectionIMAPSettings("hmail@easysmpc.org")
          .setPassword("12345")
          .setSMTPServer("easysmpc.org")
          .setIMAPServer("easysmpc.org")
          .setIMAPPort(143)
          .setSMTPPort(587);
//      ConnectionIMAPSettings connectionIMAPSettings = new ConnectionIMAPSettings("james" + ConnectionIMAPSettings.INDEX_REPLACE + "@easysmpc.org").setPassword("12345")
//      .setSMTPServer("easysmpc.org")
//      .setIMAPServer("easysmpc.org")
//      .setIMAPPort(143)
//      .setSMTPPort(25);
        
        // Create mailbox details
        MailboxDetails mailBoxDetails = new MailboxDetails(isSharedMailbox, connectionIMAPSettings, participants);
        
        // Repeat endless
        while (true) {
            Combination combination = combinator.getNextCombination();
            
            // Call evaluation
            new PerformanceEvaluation(combination.getParticipants(),
                                      combination.getBins(),
                                      combination.getMailboxCheckInterval(),
                                      mailBoxDetails);
        }
    }

    /**
     * Creates a new instance
     * 
     * @param participants
     * @param bins
     * @param mailboxCheckInterval
     * @param mailBoxDetails 
     * @throws IOException 
     */
    public PerformanceEvaluation(int participants,
                                 int bins,
                                 int mailboxCheckIntervals,
                                 MailboxDetails mailBoxDetails) throws IOException {
        // Prepare if necessary
        if (!prepared) {
            try {
                prepare(mailBoxDetails);
                prepared = true;
            } catch (IOException | BusException | InterruptedException e) {
                logger.error("Preparation failed logged", new Date(), "Preparation failed", ExceptionUtils.getStackTrace(e));
                throw new IllegalStateException("Unable to prepare performance evaluation", e);
            }
        }

        // Start a EasySMPC process
        CreatingUser user = new CreatingUser(participants,
                                             bins,
                                             mailboxCheckIntervals,
                                             mailBoxDetails);

        // Wait to finish
        while (!user.areAllUsersFinished()) {
            try {
                Thread.sleep(Resources.INTERVAL_SCHEDULER_MILLISECONDS);
            } catch (InterruptedException e) {
                logger.error("Interrupted exception logged",
                             new Date(),
                             "Interrupted exception logged",
                             ExceptionUtils.getStackTrace(e));
            }
        }

        // Reset statistics
        Bus.resetStatistics();

        // Wait
        int waitTime = 10000;
        logger.debug("Wait logged", new Date(), "Started waiting for", waitTime);
        try {
            Thread.sleep(waitTime);
        } catch (InterruptedException e) {
            logger.error("Interrupted exception logged",
                         new Date(),
                         "Interrupted exception logged",
                         ExceptionUtils.getStackTrace(e));
        }
    }

    /**
     * Prepare evaluation
     * @param mailBoxDetails 
     * 
     * @throws IOException 
     * @throws BusException 
     * @throws InterruptedException 
     */
    private void prepare(MailboxDetails mailBoxDetails) throws IOException, BusException, InterruptedException {
        // Set logging properties from file      
        System.setProperty("log4j2.configurationFile", "src/main/resources/org/bihealth/mi/easysmpc/nogui/log4j2.xml");
        logger = LogManager.getLogger(PerformanceEvaluation.class);
        
        // Delete existing e-mails
        for (ConnectionIMAPSettings connectionIMAPSettings : mailBoxDetails.getAllConnections()) {
            BusEmail bus = new BusEmail(new ConnectionIMAP(connectionIMAPSettings, true), 0);
            bus.purgeEmails();
            bus.stop();
        }
        
        // Create CSV printer
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

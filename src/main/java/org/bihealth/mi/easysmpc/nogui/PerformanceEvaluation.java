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

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.bihealth.mi.easybus.implementations.email.ConnectionIMAPSettings;
import org.bihealth.mi.easysmpc.resources.Resources;
/**
 * Starts a performance test without GUI
 * 
 * @author Felix Wirth
 *
 */
public class PerformanceEvaluation {
    
    public static final String FILEPATH = "performanceEvaluation";
    
    public static CSVPrinter csvPrinter;

    /**
     * 
     * Starts the performance test
     *
     * @param args
     * @throws IOException 
     */
    public static void main(String[] args) throws IOException  {
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
        
        // Set logging properties from file      
        System.setProperty("log4j2.configurationFile", "src/main/resources/org/bihealth/mi/easysmpc/nogui/log4j2.xml");
        
        // Create connection settings
        ConnectionIMAPSettings connectionIMAPSettings = new ConnectionIMAPSettings("easysmpc.dev@insutec.de").setPassword("3a$ySMPC!")
                .setSMTPServer("smtp.ionos.de")
                .setIMAPServer("imap.ionos.de");
        
        // Start a EasySMPC process
        CreatingUser user = new CreatingUser(3, 10, connectionIMAPSettings, 1000);
        
        while(!user.areAllUsersFinished()) {
            // Wait to finish
            try {
                Thread.sleep(Resources.INTERVAL_SCHEDULER_MILLISECONDS);
            } catch (InterruptedException e) {
                // Ignore
            }
        }
    }
}
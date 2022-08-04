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
import java.util.Properties;

/**
 * Test
 * 
 * @author Felix Wirth
 *
 */
public class IntegrationTest {
    
    /** Properties */
    public static Properties prop = new Properties();

    /**
     * Starts the test
     * 
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        // Init
        prop.load(IntegrationTest.class.getResourceAsStream("Settings.properties"));
        
        // Config
        String[] argumentsCreator = {"-create",
                                    "-b", "variables1.csv,variables2.csv,variables3.csv", 
                                    "-d", "aggregation1.csv,aggregation2.csv,aggregation3.csv",
                                    "-f" , "Creator,easysmpc.dev0@insutec.de;Participant1,easysmpc.dev1@insutec.de;Participant2,easysmpc.dev2@insutec.de",
                                    "-a", "easysmpc.dev0@insutec.de",
                                    "-i", "imap.ionos.de",
                                    "-x", "993",
                                    "-y", "SSLTLS",
                                    "-s", "smtp.ionos.de",
                                    "-z", "465",
                                    "-q", "SSLTLS",
                                    "-l", "cord-aggregation",
                                    "-e",
                                    "-j", "2",
                                    "-mbc", "20",
                                    "-tm", "55",
                                    "-ms", "6",
                                    "-p", prop.getProperty("first.password")};
        
        String[] arugmentParticipant1 = {
                                    "-participate",
                                    "-d", "aggregation1.csv,aggregation2.csv,aggregation3.csv",
                                    "-o", "Participant1", 
                                    "-a", "easysmpc.dev1@insutec.de",
                                    "-i", "imap.ionos.de", 
                                    "-x", "993",
                                    "-y", "SSLTLS",
                                    "-s", "smtp.ionos.de",
                                    "-z", "465",
                                    "-q", "SSLTLS",
                                    "-l", "cord-aggregation",
                                    "-e",
                                    "-j", "2",
                                    "-mbc", "20",
                                    "-tm", "55",
                                    "-ms", "6",
                                    "-p", prop.getProperty("first.password")};
        
        String[] arugmentParticipant2 = {
                                         "-participate",
                                         "-d", "aggregation1.csv,aggregation2.csv,aggregation3.csv",
                                         "-o", "Participant2", 
                                         "-a", "easysmpc.dev2@insutec.de",
                                         "-i", "imap.ionos.de", 
                                         "-x", "993",
                                         "-y", "SSLTLS",
                                         "-s", "smtp.ionos.de",
                                         "-z", "465",
                                         "-q", "SSLTLS",
                                         "-l", "cord-aggregation",
                                         "-e",
                                         "-j", "2",
                                         "-mbc", "20",
                                         "-tm", "55",
                                         "-ms", "6",
                                         "-p", prop.getProperty("third.password")};

        // Start creator
        new Thread(new Runnable() {
            @Override
            public void run() {
                Main.main(argumentsCreator);
            }
        }).start();
        
        // Start participant1
        new Thread(new Runnable() {
            @Override
            public void run() {
                Main.main(arugmentParticipant1);
            }
        }).start();
        
        // Start participant2
        new Thread(new Runnable() {
            @Override
            public void run() {
                Main.main(arugmentParticipant2);
            }
        }).start();
        
    }
}

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
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bihealth.mi.easybus.Bus;

import de.tu_darmstadt.cbs.emailsmpc.Study;

/**
 * Calculates time differences and logs them 
 * 
 * @author Felix Wirth
 *
 */
public class RecordTimeDifferences {
    /** Logger */
    private static final Logger  logger = LogManager.getLogger(RecordTimeDifferences.class);
    /** Stores measurements */
    private static final Map<String, RecordTimeDifferences> timeDifferencesMap = new HashMap<>();


    /** Measurements */
    private final List<Pair<Long, Long>> measurements;
    /** Mailbox check interval */
    private int mailBoxCheckInterval;
    /** Model */
    private Study model;

    
    private RecordTimeDifferences(Study model, int mailBoxCheckInterval, long startTime) {
        this.mailBoxCheckInterval = mailBoxCheckInterval;
        this.model = model;
        this.measurements = createEmptyList(model.numParticipants);
    }
    
    /**
     * Creates a list with entries are all null
     * 
     * @param numberParticipants
     * @return
     */
    private static List<Pair<Long, Long>> createEmptyList(int numberParticipants) {
        // Create list
        List<Pair<Long, Long>>  result = new ArrayList<>();
        for(int i = 0; i < numberParticipants ; i++) {
            result.add(null);
        }
        
        // Return
        return result;
    }
    
    /**
     * Add a new record with a start time
     * 
     * @param studyUID
     * @param numberParticipants
     * @param numberBins
     * @param startTime
     */
    public static void init(Study model, int mailBoxCheckInterval, long startTime) {        
        // Create a new entry in measurements
        timeDifferencesMap.put(model.studyUID, new RecordTimeDifferences(model, mailBoxCheckInterval, startTime));
        
        // Add and log the starting value 
        addStartValue(model.studyUID, model.ownId, startTime);       
        logger.debug("Started", new Date(), model.studyUID, "started", model.numParticipants, "participants", model.bins.length, "bins", mailBoxCheckInterval, "mailbox check interval"); 
    }
     
    /**
     * Records the start of a user's participation
     * 
     * @param studyUID
     * @param participantId
     * @param startTime
     */
    public static void addStartValue(String studyUID, int participantId, long startTime) {
        timeDifferencesMap.get(studyUID).measurements.set(participantId, new Pair<Long, Long>(startTime));
    }

    /**
     * Add finished time
     * If all values are finished calculates final results
     * 
     * @param studyUID
     * @param finishedTime
     */
    public static void finished(String studyUID, int participantId, long finishedTime) {
        // Init
        List<Pair<Long, Long>> measurements = timeDifferencesMap.get(studyUID).measurements;
        
        // Set finished time
        measurements.get(participantId).setSecondValue(finishedTime);
        
        // Proceed only if result can be calculated
        if (!isProcessFinished(measurements)) {
            return;
        }        
        
        // Calculate execution times
        Long[] timeDifferences = new Long[measurements.size()];
        int index = 0;
        for (Pair<Long, Long> duration : measurements) {
            timeDifferences[index] = duration.getSecondValue() - duration.getFirstValue();
            index++;
        }
            
        // Sort execution times
        Arrays.sort(timeDifferences);
        
        // Write performance results
        try {
            PerformanceEvaluation.csvPrinter.printRecord(new Date(),
                                         timeDifferencesMap.get(studyUID).model.studyUID,
                                         timeDifferencesMap.get(studyUID).model.numParticipants,
                                         timeDifferencesMap.get(studyUID).model.bins.length,
                                         timeDifferencesMap.get(studyUID).mailBoxCheckInterval,
                                         timeDifferences[0],
                                         timeDifferences[timeDifferences.length - 1],
                                         calculateMean(timeDifferences),
                                         Bus.numberMessagesReceived,
                                         Bus.totalSizeMessagesReceived,
                                         Bus.numberMessagesSent,
                                         Bus.totalSizeMessagesSent);
            PerformanceEvaluation.csvPrinter.flush();
        } catch (IOException e) {
            throw new IllegalStateException("Unable to write performance results", e);
        }
        
        
        // Fastest finished entry => log            
        logger.debug("Entry logged",
                    new Date(),
                    studyUID,
                    "finished",
                    "first",
                    timeDifferences[0],
                    "duration");
            
        // Slowest finished entry => log
        logger.debug("Slowest entry logged",
                    new Date(),
                    studyUID,
                    "finished",
                    "last",
                    timeDifferences[timeDifferences.length - 1],
                    "duration",
                    calculateMean(timeDifferences)
                    );
        }

    /**
     * Checks if all values for start and finish are set
     * 
     * @param durations
     * @return
     */
    private static boolean isProcessFinished(List<Pair<Long, Long>> durations) {
        for(Pair<Long, Long> duration : durations) {
            if(duration.getFirstValue() == null || duration.getSecondValue() == null) {
                return false;
            }
        }
        
        return true;
    }

    /**
     * Calculates a mean
     * 
     * @param timeDifferences
     * @return
     */
    private static long calculateMean(Long[] timeDifferences) {
        long sum = 0; 
        for(long timeDifference : timeDifferences) {
            sum = sum + timeDifference;
        }
        return sum / timeDifferences.length;
    }
    
    /**
     * Class for pairs
     * 
     * @author Felix Wirth
     *
     */
    private static class Pair<K,V> {
        /**First value */
        private K firstValue;
        /** Second value*/
        private V secondValue;
        
        /**
         * Creates a new instance
         * 
         * @param firstValue
         */
        Pair(K firstValue) {
            this.firstValue = firstValue;
        }

        /**
         * @return the secondValue
         */
        public V getSecondValue() {
            return secondValue;
        }

        /**
         * @param secondValue the secondValue to set
         */
        public void setSecondValue(V secondValue) {
            this.secondValue = secondValue;
        }

        /**
         * @return the firstValue
         */
        public K getFirstValue() {
            return firstValue;
        }     
    }
}

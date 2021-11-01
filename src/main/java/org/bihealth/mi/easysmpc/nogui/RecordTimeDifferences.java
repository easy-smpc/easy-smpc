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
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.tu_darmstadt.cbs.emailsmpc.Study;

/**
 * Calculates time differences and logs them 
 * 
 * @author Felix Wirth
 *
 */
public class RecordTimeDifferences {
    /**
     * Class for pairs
     * 
     * @author Felix Wirth
     *
     */
    private static class Pair<K,V> {
        /**First value */
        private K firstValue;
        /** Second value */
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
         * @return the firstValue
         */
        public K getFirstValue() {
            return firstValue;
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
    }
    
    /** Logger */
    private static final Logger  logger = LogManager.getLogger(RecordTimeDifferences.class);
    
    /** Measurements */
    private final List<Pair<Long, Long>> measurements;

    /** Mailbox check interval */
    private int mailBoxCheckInterval;

    /** Model */
    private Study model;

    /** Tracker*/
    private PerformanceTracker tracker;
    
    /** Printer */
    private ResultPrinter printer;

    /**
     * Records the start of a user's participation
     * 
     * @param studyUID
     * @param participantId
     * @param startTime
     */
    public void addStartValue(int participantId, long startTime) {
        measurements.set(participantId, new Pair<Long, Long>(startTime));
    }
    /**
     * Calculates a mean
     * 
     * @param timeDifferences
     * @return
     */
    private long calculateMean(Long[] timeDifferences) {
        // Init
        long sum = 0;
        
        // Sum
        for(long timeDifference : timeDifferences) {
            sum = sum + timeDifference;
        }
        
        // Divide and return
        return sum / timeDifferences.length;
    }
    /**
     * Creates a list with entries all null
     * 
     * @param numberParticipants
     * @return
     */
    private List<Pair<Long, Long>> initList(int numberParticipants) {
        // Create list
        List<Pair<Long, Long>>  result = new ArrayList<>();
        for(int i = 0; i < numberParticipants ; i++) {
            result.add(null);
        }
        
        // Return
        return result;
    }

    
    /**
     * Add finished time for a participant and calculates final results if all values are finished 
     * 
     * @param studyUID
     * @param finishedTime
     */
    public void finished(int participantId, long finishedTime) {       
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
           printer.print(new Date(), model.getStudyUID(),
                                         model.getNumParticipants(),
                                         model.getBins().length,
                                         mailBoxCheckInterval,
                                         timeDifferences[0],
                                         timeDifferences[timeDifferences.length - 1],
                                         calculateMean(timeDifferences),
                                         tracker.getNumberMessagesReceived(),
                                         tracker.getTotalSizeMessagesReceived(),
                                         tracker.getNumberMessagesSent(),
                                         tracker.getTotalsizeMessagesSent());
           printer.flush();
        } catch (IOException e) {
            throw new IllegalStateException("Unable to write performance results", e);
        }
        
        
        // Fastest finished entry => log            
        logger.debug("Entry logged",
                    new Date(),
                    model.getStudyUID(),
                    "finished",
                    "first",
                    timeDifferences[0],
                    "duration");
            
        // Slowest finished entry => log
        logger.debug("Slowest entry logged",
                    new Date(),
                    model.getStudyUID(),
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
        // Check for null in each entry
        for(Pair<Long, Long> duration : durations) {
            if(duration.getFirstValue() == null || duration.getSecondValue() == null) {
                return false;
            }
        }
        
        // Return
        return true;
    }     
    
    /**
     * Creates a new instance
     * 
     * @param model
     * @param mailBoxCheckInterval
     * @param startTime
     * @param performanceTracker 
     */
    public RecordTimeDifferences(Study model, int mailBoxCheckInterval, long startTime, PerformanceTracker performanceTracker, ResultPrinter printer) {
        // Store
        this.mailBoxCheckInterval = mailBoxCheckInterval;
        this.model = model;
        this.measurements = initList(model.getNumParticipants());
        this.tracker = performanceTracker;
        this.printer = printer;
        
        // Add and log the starting value
        measurements.set(model.getOwnId(), new Pair<Long, Long>(startTime));
        logger.debug("Started", new Date(), model.getStudyUID(), "started", model.getNumParticipants(), "participants", model.getBins().length, "bins", mailBoxCheckInterval, "mailbox check interval");
    }
}

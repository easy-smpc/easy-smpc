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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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
    private static final Map<String, List<Pair<Long, Long>>> measurements = new HashMap<>();
    /** Durations
     * @return */
    
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
    public static void init(String studyUID, int participantId, int numberParticipants, int numberBins, int mailBoxCheckInterval, long startTime) {        
        // Create a new entry in measurements
        measurements.put(studyUID, createEmptyList(numberParticipants));
        
        // Add and log the starting value 
        addStartValue(studyUID, participantId, startTime);       
        logger.info("Started", new Date(), studyUID, "started", numberParticipants, "participants", numberBins, "bins", mailBoxCheckInterval, "mailbox check interval"); 
    }
     
    /**
     * Records the start of a user's participation 
     * 
     * @param studyUID
     * @param participantId
     * @param startTime
     */
    public static void addStartValue(String studyUID,
                                       int participantId,
                                       long startTime) {
        measurements.get(studyUID).set(participantId, new Pair<Long, Long>(startTime));
    }

    /**
     * Add finished time
     * If all values are finished calculates final results
     * 
     * @param studyUID
     * @param finishedTime
     */
    public static void finished(String studyUID, int participantId, long finishedTime) {
        // Set finished time
        measurements.get(studyUID).get(participantId).setSecondValue(finishedTime);
        
        // Proceed only if result can be calculated 
        if(!isProcossFinished(measurements.get(studyUID))) {
            return;
        }
            // Calculate execution times
            Long[] timeDifferences = new Long[measurements.get(studyUID).size()];
            int index = 0;
            for(Pair<Long, Long> duration: measurements.get(studyUID)) {
                timeDifferences[index] = duration.getSecondValue() - duration.getFirstValue();
                index++;
            }
            
            // Sort execution times
            Arrays.sort(timeDifferences);
            
            // Fastest finished entry => log            
            logger.info("Fastest entry logged", new Date(),studyUID, "finished", "first", timeDifferences[0], "duration"); 
            
            // Slowest finished entry => log
            logger.info("Slowest entry logged", new Date(),studyUID, "finished", "last", timeDifferences[timeDifferences.length - 1], "duration", calculateMean(timeDifferences), calculateMean(timeDifferences));
        }


    /**
     * Checks if all values for start and finish are set
     * 
     * @param durations
     * @return
     */
    private static boolean isProcossFinished(List<Pair<Long, Long>> durations) {
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
    private static double calculateMean(Long[] timeDifferences) {
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

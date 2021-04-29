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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Calculates time differences and logs them 
 * 
 * @author Felix Wirth
 *
 */
public class RecordTimeDifferences {
    /** Logger */
    private static final Logger logger = Logger.getLogger(User.class.getName());
    /** Stores measurements */
    private static final Map<String, RecordTimeDifferences> measurements = new HashMap<>();
    /** Number of participants in a measurements*/
    private int numberParticipants;
    /** Start time of measurements*/
    private long startTime;
    /** Durations*/
    private List<Long> durations;
    
    /**
     * Create a new instance
     * 
     * @param numberParticipants
     * @param startTime
     */
    private RecordTimeDifferences(int numberParticipants, long startTime) {
        this.numberParticipants = numberParticipants;
        this.startTime = startTime;
        this.durations = new ArrayList<>();
    }
    
    /**
     * Add a new record with a start time
     * 
     * @param studyUID
     * @param numberParticipants
     * @param numberBins
     * @param startTime
     */
    public static void start(String studyUID, int numberParticipants, int numberBins, long startTime) {        
        measurements.put(studyUID, new RecordTimeDifferences(numberParticipants, startTime));
        logger.info(String.format(Start.LOGGING_START_MESSAGE, studyUID, numberParticipants, numberBins));
    }
     
    /**
     * Add a result time
     * 
     * @param studyUID
     * @param finishedTime
     */
    public static void finished(String studyUID, long finishedTime) {
        // Get the measurement and add the duration
        RecordTimeDifferences measurement = measurements.get(studyUID);
        Long duration = finishedTime - measurement.startTime;
        measurement.durations.add(duration);
        
        // If first finished entry => log
        if (measurement.durations.size() == 1) {
            logger.info(String.format(Start.LOGGING_FINISH_MESSAGE, studyUID, "first", duration, 0.0));
            return;
        }
        
        // If last finished entry => log
        if (measurement.durations.size() == measurement.numberParticipants) {
            logger.info(String.format(Start.LOGGING_FINISH_MESSAGE, studyUID, "last", duration, calculateMean(measurement.durations)));
        }        
    }

    /**
     * Calculates a mean
     * 
     * @param durations
     * @return
     */
    private static double calculateMean(List<Long> durations) {
        long sum = 0; 
        for(long duration : durations) {
            sum = sum + duration;
        }
        return sum / durations.size();
    }    
}

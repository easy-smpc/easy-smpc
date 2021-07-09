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

import java.util.List;

/**
 * A class to combine the different parameters
 * 
 * @author Felix Wirth
 *
 */
public abstract class Combinator {
    
    /** Possible participants */
    private final List<Integer> participants;
    /** Possible bins */
    private final List<Integer> bins;
    /** Possible mailboxCheckInterval */
    private final List<Integer> mailboxCheckInterval;
    
   
    /**
     * @param participants
     * @param bins
     * @param mailboxCheckInterval
     */
    public Combinator(List<Integer> participants,
                            List<Integer> bins,
                            List<Integer> mailboxCheckInterval) {
        // Store
        this.participants = participants;
        this.bins = bins;
        this.mailboxCheckInterval = mailboxCheckInterval;
    }
    
    /**
     * Get a new combination
     * 
     * @return
     */
    public abstract Combination getNextCombination();
    
    /**
     * A combination of possible parameters 
     * @author Felix Wirth
     *
     */
    public class Combination {
        /** Participant */
        private final int participants;
        /** Bins */
        private final int bins;
        /** mailboxCheckInterval */
        private final int mailboxCheckInterval;

        /**
         * @return participants
         */
        protected int getParticipants() {
            return participants;
        }

        /**
         * @return the bins
         */
        protected int getBins() {
            return bins;
        }

        /**
         * @return the mailboxCheckInterval
         */
        protected int getMailboxCheckInterval() {
            return mailboxCheckInterval;
        }

        /**
         * Creates a new instance
         * 
         * @param participants
         * @param bins
         * @param mailboxCheckInterval
         */
        public Combination(int participants,
                           int bins,
                           int mailboxCheckInterval) {
            this.participants = participants;
            this.bins = bins;
            this.mailboxCheckInterval = mailboxCheckInterval;
        }
    }
    
    /**
     * @return the participants
     */
    protected List<Integer> getParticipants() {
        return participants;
    }

    /**
     * @return the bins
     */
    protected List<Integer> getBins() {
        return bins;
    }

    /**
     * @return the mailboxCheckInterval
     */
    protected List<Integer> getMailboxCheckInterval() {
        return mailboxCheckInterval;
    }
}

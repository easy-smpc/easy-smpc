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
import java.util.List;
import java.util.Random;

/**
 * Allows to obtain a random combination of parameters
 * 
 * @author Felix Wirth
 *
 */
public class RandomCombinator {
    
    /** Possible participants */
    private List<Integer> particpants;
    /** Possible bins */
    private List<Integer> bins;
    /** Possible mailboxCheckInterval */
    private List<Integer> mailboxCheckInterval;
    /** Random */
    private Random random;

    /**
     * @param participants
     * @param bins
     * @param mailboxCheckInterval
     */
    public RandomCombinator(List<Integer> participants,
                            List<Integer> bins,
                            List<Integer> mailboxCheckInterval) {
        // Store
        this.particpants = participants;
        this.bins = bins;
        this.mailboxCheckInterval = mailboxCheckInterval;
        this.random = new Random();
    }
    
    /**
     * Creates a new combination
     * 
     * @return
     */
    public Combination getNewCombination() {        
        return new Combination(particpants.get(random.nextInt(particpants.size())),
                               bins.get(random.nextInt(bins.size())),
                               mailboxCheckInterval.get(random.nextInt(mailboxCheckInterval.size())));
    }
    
    /**
     * A combination of possible parameters 
     * @author Felix Wirth
     *
     */
    public class Combination {
        /** Participant */
        private final int particpants;
        /** Bins */
        private final int bins;
        /** mailboxCheckInterval */
        private final int mailboxCheckInterval;

        /**
         * @return the particpants
         */
        protected List<Integer> getParticpants() {
            return new ArrayList<Integer>(Arrays.asList(particpants));
        }

        /**
         * @return the bins
         */
        protected List<Integer> getBins() {
            return new ArrayList<Integer>(Arrays.asList(bins));
        }

        /**
         * @return the mailboxCheckInterval
         */
        protected List<Integer> getMailboxCheckInterval() {
            return new ArrayList<Integer>(Arrays.asList(mailboxCheckInterval));
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
            this.particpants = participants;
            this.bins = bins;
            this.mailboxCheckInterval = mailboxCheckInterval;
        }
    }
}
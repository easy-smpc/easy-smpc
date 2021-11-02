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

import java.util.Iterator;
import java.util.List;
import java.util.Random;

import org.bihealth.mi.easysmpc.nogui.Combinator.Combination;

/**
 * Allows to obtain a random combination of parameters
 * 
 * @author Felix Wirth
 *
 */
public class RandomCombinator extends Combinator implements Iterator<Combination> {
    
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
        // Call super
        super(participants, bins, mailboxCheckInterval);
        
        // Create random
        this.random = new Random();
    }    

    @Override
    public Iterator<Combination> iterator() {
        return this;
    }

    @Override
    public boolean hasNext() {
        // This combinator has always a next element
        return true;
    }

    @Override
    public Combination next() {
        return new Combination(getParticipants().get(random.nextInt(getParticipants().size())),
                               getBins().get(random.nextInt(getBins().size())),
                               getMailboxCheckInterval().get(random.nextInt(getMailboxCheckInterval().size())));
    }
}
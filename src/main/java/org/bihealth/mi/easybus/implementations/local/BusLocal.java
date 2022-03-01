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
package org.bihealth.mi.easybus.implementations.local;

import org.bihealth.mi.easybus.Bus;
import org.bihealth.mi.easybus.BusException;
import org.bihealth.mi.easybus.MessageFragment;
import org.bihealth.mi.easybus.MessageFragmentFinish;
import org.bihealth.mi.easybus.Participant;
import org.bihealth.mi.easybus.Scope;

/**
 * An easy, minimal Bus implementation
 * 
 * @author Felix Wirth
 */
public class BusLocal extends Bus {
    
    /**
     * Creates a new instance
     * 
     * @param sizeThreadpool
     * @param maxMessageSize
     */
    public BusLocal(int sizeThreadpool, int maxMessageSize){
        super(sizeThreadpool, maxMessageSize);
    }

    @Override
    public boolean isAlive() {
        return true;
    }

    @Override
    protected Void sendInternal(MessageFragment message, Scope scope, Participant participant) throws Exception {
        receiveInternal(new MessageFragmentFinish(message) {
            
            /** SVUID */
            private static final long serialVersionUID = -6679946774576062591L;

            @Override
            public void finalize() throws BusException {
                // Empty by design
            }
            
            @Override
            public void delete() throws BusException {
                // Empty by design
            }
        }, scope, participant);
        return null;
    }

    @Override
    public void stop() {
        // Empty by design
    }
}
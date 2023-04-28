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

import java.util.concurrent.FutureTask;

import org.bihealth.mi.easybus.Bus;
import org.bihealth.mi.easybus.BusException;
import org.bihealth.mi.easybus.BusMessage;
import org.bihealth.mi.easybus.MessageFilter;

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
    public BusLocal(int sizeThreadpool){
        super(sizeThreadpool);
    }

    @Override
    public boolean isAlive() {
        return true;
    }

    @Override
    public void stop() {
        // Empty by design
    }
    
    @Override
    protected Void sendInternal(BusMessage message) throws Exception {
        receiveInternal(message);
        return null;
    }

    @Override
    public void purge(MessageFilter filter) throws BusException, InterruptedException {
     // Empty by design
    }

    @Override
    public FutureTask<Void> sendPlain(String recipient, String subject, String body) throws BusException {
        throw new UnsupportedOperationException("Sending plain messages is not supported by this bus");
    }
}
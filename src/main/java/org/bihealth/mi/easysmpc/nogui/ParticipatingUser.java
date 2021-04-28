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
import java.math.BigInteger;

import de.tu_darmstadt.cbs.emailsmpc.Message;
import de.tu_darmstadt.cbs.emailsmpc.MessageInitial;

/**
 * A participant in an EasySMPC process
 * 
 * @author Felix Wirth
 *
 */
public class ParticipatingUser extends User {

    /**
     * Create a new instance
     * 
     * @param serializeMessage initial message
     * @param lengthBitBigInteger size of generated big integers
     */
    public ParticipatingUser(String serializeMessage, int lengthBitBigInteger) {
        try {
            // Get data
            String data = Message.deserializeMessage(serializeMessage).data;

            // Init model
            setModel(MessageInitial.getAppModel(MessageInitial.decodeMessage(Message.getMessageData(data))));

            // Proceed to entering value
            getModel().toEnteringValues(data);

            // Set own values and proceed
            getModel().toSendingShares(fillBins(lengthBitBigInteger));
            
            // Starts the common steps 
            proceedCommonProcessSteps();
            
        } catch (ClassNotFoundException | IllegalArgumentException | IllegalStateException | IOException e) {
            throw new IllegalStateException("Unable to execute particpating users steps" , e);
        }
    }

    /**
     * Fills the bins with random numbers
     * 
     * @return bins
     */
    private BigInteger[] fillBins(int lengthBitBigInteger) {
        // Init
        BigInteger[] result = new BigInteger[getModel().bins.length];
        
        // Set a random big integer for each
        for (int index = 0; index < result.length; index++) {
            result[index] = generateRandomBigInteger(lengthBitBigInteger);
        }
        
        // Return
        return result;
    }
}

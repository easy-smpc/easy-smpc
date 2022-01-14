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
package de.tu_darmstadt.cbs.emailsmpc;

import java.io.IOException;
import java.math.BigDecimal;

/**
 * Small helper to analyze the size of messages (exchange strings)
 * 
 * @author Felix Wirth
 */
public class AnalyzeMessageSize {

    /**
     * @param args
     * @throws IOException 
     * @throws IllegalStateException 
     */
    public static void main(String[] args) throws IllegalStateException, IOException {
        
        Study testmodel = new Study();
        Participant[] part = new Participant[3];
        Bin[] bins = new Bin[1];
        int fractionalBits = 32;
        for (int i = 0; i < part.length; i++) {
            part[i] = new Participant("Participant1 " + i, "part" + i + "@test.com");
        }
        for (int i = 0; i < bins.length; i++) {
            bins[i] = new Bin("Bin " + i);
            bins[i].initialize(part.length);
            bins[i].shareValue(BigDecimal.valueOf(1), fractionalBits);
        }
        testmodel.toStarting();
        testmodel.toInitialSending("A1231231", part, bins, null);
        
        String serializedMessage = Message.serializeMessage(testmodel.getUnsentMessageFor(1));
        System.out.println("chars " + serializedMessage.length());
        System.out.println("bytes " + serializedMessage.getBytes().length);
        System.out.println(serializedMessage);
    }
}

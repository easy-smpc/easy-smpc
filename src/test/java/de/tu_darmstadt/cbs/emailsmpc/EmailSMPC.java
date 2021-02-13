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

import java.math.BigInteger;

import de.tu_darmstadt.cbs.secretshare.ArithmeticShare;
import de.tu_darmstadt.cbs.secretshare.ArithmeticSharing;

/**
 * Not really a test, but a hello world!.
 * @author Tobias Kussel
 */
public class EmailSMPC {
    
    /**
     * The main method.
     *
     * @param args the arguments
     */
    public static void main(String[] args) {
        int num_parties = 7;
        BigInteger secret = BigInteger.valueOf(15);
        if (args.length == 1) {
            secret = new BigInteger(args[0]);
        }
        ArithmeticSharing sharer = new ArithmeticSharing(num_parties);
        ArithmeticShare[] shares = sharer.share(secret);
        System.out.println(num_parties + " Parties:");
        for (int i = 0; i != num_parties; i++) {
            System.out.println("Share " + i + ": " + shares[i]);
        }
        System.out.println("The reconstructed secret is: " + ArithmeticSharing.reconstruct(shares) + " and should be: " + secret);
    }
}

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
package de.tu_darmstadt.cbs.secretshare;

import static org.junit.Assert.*;

import org.junit.Test;
import java.math.BigInteger;
import java.security.SecureRandom;

/**
 * Unit test for secret sharing
 * @author Tobias Kussel
 */
public class ArithmeticSharingTest {
    
    /**
     * Rigorous Test :-).
     */
    @Test
    public void fiveParties() {
        int numParties = 5;
        ArithmeticSharing as = new ArithmeticSharing(numParties);
        SecureRandom randomGenerator = new SecureRandom();
        BigInteger secret1 = BigInteger.valueOf(7);
        BigInteger secret2 = BigInteger.valueOf(764023475);
        BigInteger secret3 = new BigInteger(126, randomGenerator);

        ArithmeticShare[] shares1 = as.share(secret1);
        ArithmeticShare[] shares2 = as.share(secret2);
        ArithmeticShare[] shares3 = as.share(secret3);
        assertEquals(shares1.length, numParties);
        assertEquals(shares2.length, numParties);
        assertEquals(shares3.length, numParties);
        assertEquals(ArithmeticSharing.reconstruct(shares1), secret1);
        assertEquals(ArithmeticSharing.reconstruct(shares2), secret2);
        assertEquals(ArithmeticSharing.reconstruct(shares3), secret3);
    }

    /**
     * Homomorphity.
     */
    @Test
    public void homomorphity() {
        int numParties = 15;
        ArithmeticSharing as = new ArithmeticSharing(numParties);
        SecureRandom randomGenerator = new SecureRandom();
        BigInteger secret1 = new BigInteger(124, randomGenerator);
        BigInteger secret2 = new BigInteger(124, randomGenerator);
        BigInteger secret3 = new BigInteger(124, randomGenerator);

        ArithmeticShare[] shares1 = as.share(secret1);
        ArithmeticShare[] shares2 = as.share(secret2);
        ArithmeticShare[] shares3 = as.share(secret3);
        ArithmeticShare[] sum = new ArithmeticShare[numParties];
        for (int i = 0; i < numParties; i++) {
            sum[i] = shares1[i].add(shares2[i]).add(shares3[i]);
        }
        assertEquals(ArithmeticSharing.reconstruct(sum), secret1.add(secret2).add(secret3));

    }

    /**
     * Lot parties.
     */
    @Test
    public void lotParties() {
        int numParties = 5341;
        ArithmeticSharing as = new ArithmeticSharing(numParties);
        SecureRandom randomGenerator = new SecureRandom();
        BigInteger secret1 = BigInteger.valueOf(7);
        BigInteger secret2 = BigInteger.valueOf(768532475);
        BigInteger secret3 = new BigInteger(126, randomGenerator);

        ArithmeticShare[] shares1 = as.share(secret1);
        ArithmeticShare[] shares2 = as.share(secret2);
        ArithmeticShare[] shares3 = as.share(secret3);
        assertEquals(shares1.length, numParties);
        assertEquals(shares2.length, numParties);
        assertEquals(shares3.length, numParties);
        assertEquals(ArithmeticSharing.reconstruct(shares1), secret1);
        assertEquals(ArithmeticSharing.reconstruct(shares2), secret2);
        assertEquals(ArithmeticSharing.reconstruct(shares3), secret3);
    }

    /**
     * Seven parties.
     */
    @Test
    public void sevenParties() {
        int numParties = 7;
        ArithmeticSharing as = new ArithmeticSharing(numParties);
        SecureRandom randomGenerator = new SecureRandom();
        BigInteger secret1 = BigInteger.valueOf(7);
        BigInteger secret2 = BigInteger.valueOf(76853235);
        BigInteger secret3 = new BigInteger(126, randomGenerator);

        ArithmeticShare[] shares1 = as.share(secret1);
        ArithmeticShare[] shares2 = as.share(secret2);
        ArithmeticShare[] shares3 = as.share(secret3);
        assertEquals(shares1.length, numParties);
        assertEquals(shares2.length, numParties);
        assertEquals(shares3.length, numParties);
        assertEquals(ArithmeticSharing.reconstruct(shares1), secret1);
        assertEquals(ArithmeticSharing.reconstruct(shares2), secret2);
        assertEquals(ArithmeticSharing.reconstruct(shares3), secret3);
    }
}

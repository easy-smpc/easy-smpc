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
import java.math.BigDecimal;
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
    public void fivePartiesInt() {
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

    @Test
    public void fivePartiesDec() {
        int numParties = 5;
        int fractionalBits = 32;
        double delta = 0.001;
        ArithmeticSharing as = new ArithmeticSharing(numParties);
        SecureRandom randomGenerator = new SecureRandom();
        BigDecimal secret1 = BigDecimal.valueOf(7.634);
        BigDecimal secret2 = BigDecimal.valueOf(764023475.927456326789234);
        BigDecimal secret3 = new BigDecimal(new BigInteger(93, randomGenerator));

        ArithmeticShare[] shares1 = as.share(secret1, fractionalBits);
        ArithmeticShare[] shares2 = as.share(secret2, fractionalBits);
        ArithmeticShare[] shares3 = as.share(secret3, fractionalBits);
        assertEquals(shares1.length, numParties);
        assertEquals(shares2.length, numParties);
        assertEquals(shares3.length, numParties);
        assertEquals(ArithmeticSharing.reconstruct(shares1, fractionalBits).doubleValue(), secret1.doubleValue(), delta);
        assertEquals(ArithmeticSharing.reconstruct(shares2, fractionalBits).doubleValue(), secret2.doubleValue(), delta);
        assertEquals(ArithmeticSharing.reconstruct(shares3, fractionalBits).doubleValue(), secret3.doubleValue(), delta);
    }
    /**
     * Homomorphity.
     */
    @Test
    public void homomorphityInt() {
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
    @Test
    public void homomorphityDec() {
        int numParties = 15;
        int fractionalBits = 32;
        double delta = 0.001;
        ArithmeticSharing as = new ArithmeticSharing(numParties);
        SecureRandom randomGenerator = new SecureRandom();
        BigDecimal secret1 = new BigDecimal(new BigInteger(93, randomGenerator)).add(BigDecimal.valueOf(0.4532679821));
        BigDecimal secret2 = new BigDecimal(new BigInteger(93, randomGenerator)).add(BigDecimal.valueOf(0.4532679821));
        BigDecimal secret3 = new BigDecimal(new BigInteger(93, randomGenerator)).add(BigDecimal.valueOf(0.4532679821));

        ArithmeticShare[] shares1 = as.share(secret1, fractionalBits);
        ArithmeticShare[] shares2 = as.share(secret2, fractionalBits);
        ArithmeticShare[] shares3 = as.share(secret3, fractionalBits);
        ArithmeticShare[] sum = new ArithmeticShare[numParties];
        for (int i = 0; i < numParties; i++) {
            sum[i] = shares1[i].add(shares2[i]).add(shares3[i]);
        }
        assertEquals(ArithmeticSharing.reconstruct(sum, fractionalBits).doubleValue(), secret1.add(secret2).add(secret3).doubleValue(), delta);

    }

    /**
     * Lot parties.
     */
    @Test
    public void lotPartiesInt() {
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
    @Test
    public void lotPartiesDec() {
        int numParties = 5341;
        int fractionalBits = 32;
        double delta = 0.001;
        ArithmeticSharing as = new ArithmeticSharing(numParties);
        SecureRandom randomGenerator = new SecureRandom();
        BigDecimal secret1 = BigDecimal.valueOf(7.543323456);
        BigDecimal secret2 = BigDecimal.valueOf(768532475.5437892031);
        BigDecimal secret3 = new BigDecimal(new BigInteger(93, randomGenerator));

        ArithmeticShare[] shares1 = as.share(secret1, fractionalBits);
        ArithmeticShare[] shares2 = as.share(secret2, fractionalBits);
        ArithmeticShare[] shares3 = as.share(secret3, fractionalBits);
        assertEquals(shares1.length, numParties);
        assertEquals(shares2.length, numParties);
        assertEquals(shares3.length, numParties);
        assertEquals(ArithmeticSharing.reconstruct(shares1, fractionalBits).doubleValue(), secret1.doubleValue(), delta);
        assertEquals(ArithmeticSharing.reconstruct(shares2, fractionalBits).doubleValue(), secret2.doubleValue(), delta);
        assertEquals(ArithmeticSharing.reconstruct(shares3, fractionalBits).doubleValue(), secret3.doubleValue(), delta);
    }

    /**
     * Seven parties.
     */
    @Test
    public void sevenPartiesInt() {
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

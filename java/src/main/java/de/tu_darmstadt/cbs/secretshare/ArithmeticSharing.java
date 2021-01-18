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

import java.math.BigInteger;
import java.security.SecureRandom;

/**
 * This class implements arithmetic sharing
 * @author Tobias Kussel
 */
public class ArithmeticSharing {
    
    /**
     * Reconstruct secret from shares
     * @param shares
     * @return
     * @throws IllegalArgumentException
     */
    public static BigInteger reconstruct(ArithmeticShare[] shares) throws IllegalArgumentException {
        BigInteger reconstruction = BigInteger.ZERO;
        BigInteger first_prime = shares[0].prime;
        for (int i = 0; i != shares.length; i++) {
            if (!(shares[i].prime.equals(first_prime))) {
                throw new IllegalArgumentException("Incompatible primes found!");
            }
            reconstruction = reconstruction.add(shares[i].value).remainder(shares[i].prime);
        }
        return reconstruction;
    }
    
    /** Prime*/
    private BigInteger prime = BigInteger.valueOf(2).pow(127).subtract(BigInteger.ONE);
    
    /** Number of parties*/
    private int numParties;

    /** RNG*/
    private SecureRandom randomGenerator = new SecureRandom();

    /**
     * Creates a new instance
     * @param numParties
     */
    public ArithmeticSharing(int numParties) {
        this.numParties = numParties;
    }

    /**
     * Sets the prime
     * @param prime
     */
    public void setPrime(BigInteger prime) {
        this.prime = prime;
    }

    /**
     * Generate a signed blind
     * @param bitlength Bitlength *including* sign bit
     * @throws IllegalArgumentException Too small or negative bitlength
     * @return BigInteger
     */
    private BigInteger getSignedBlind(int bitlength) throws IllegalArgumentException {
      if (bitlength < 2)
        throw new IllegalArgumentException("Bitlength must be larger than 2");
      BigInteger value = new BigInteger(bitlength-1, randomGenerator);
      byte[] randomByte = new byte[1];
      randomGenerator.nextBytes(randomByte);
      int signum = Byte.valueOf(randomByte[0]).intValue() & 0x01;
      if (signum == 1)
        value = value.negate();
      return value;
    }

    /**
     * Share a secret
     * @param secret
     * @return
     */
    public ArithmeticShare[] share(BigInteger secret) {
        BigInteger[] shares = new BigInteger[numParties];
        shares[numParties - 1] = secret;
        for (int i = 0; i != numParties - 1; i++) {
            shares[i] = getSignedBlind(127);
            shares[numParties - 1] = shares[numParties - 1].subtract(shares[i]).remainder(prime);
        }
        ArithmeticShare[] result = new ArithmeticShare[numParties];
        for (int i = 0; i != numParties; i++) {
            result[i] = new ArithmeticShare(shares[i], prime);
        }
        return result;
    }

    /**
     * Share a secret
     * @param secret
     * @return
     */
    public ArithmeticShare[] share(int secret) {
        return share(BigInteger.valueOf(secret));
    }
}

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

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.security.SecureRandom;

/**
 * This class implements arithmetic sharing
 * @author Tobias Kussel
 */
public class ArithmeticSharing {
    
    /**
     * Convert a BigDecimal to a fixed point representation
     * @param value Value as (unscaled) BigDecimal
     * @param fractionalBits number of bits for fixed point scaling. Must be positive
     * @return Fixed point represented value
     * @throws IllegalArgumentException Negative fractionalBits
    */

    public static BigInteger convertToFixedPoint(BigDecimal value, int fractionalBits) throws IllegalArgumentException {
      if (fractionalBits < 0)
        throw new IllegalArgumentException("FractionalBits must be positive");
      final BigDecimal scaleFactor = BigDecimal.valueOf(2).pow(fractionalBits);
      return value.multiply(scaleFactor).toBigInteger();
    }

    /**
     * Reconstruct secret from shares
     * @param shares Array of all arithmetic shares
     * @return Clear text BigInteger
     * @throws IllegalArgumentException Incompatible primes
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

    /**
     * Reconstruct secret from shares
     * @param shares Array of all arithmetic shares (containing decimal values)
     * @param fractionalBits Number of bits for fractional part during construction of shares
     * @return Clear text BigDecimal
     * @throws IllegalArgumentException Incompatible primes
     */
    public static BigDecimal reconstruct(ArithmeticShare[] shares, int fractionalBits) throws IllegalArgumentException {
      final BigDecimal scaleFactor = BigDecimal.valueOf(2).pow(fractionalBits);
      BigDecimal result = new BigDecimal(reconstruct(shares));
      return result.divide(scaleFactor);
    }
    
    /**
     * Reconstruct secret from shares
     * @param shares Array of all arithmetic shares (containing decimal values)
     * @param fractionalBits Number of bits for fractional part during construction of shares
     * @param roundingMode Rounding mode for final rescaling
     * @return Clear text BigDecimal
     * @throws IllegalArgumentException Incompatible primes
     */
    public static BigDecimal reconstruct(ArithmeticShare[] shares, int fractionalBits, RoundingMode roundingMode) throws IllegalArgumentException {
      final BigDecimal scaleFactor = BigDecimal.valueOf(2).pow(fractionalBits);
      BigDecimal result = new BigDecimal(reconstruct(shares));
      return result.divide(scaleFactor, roundingMode);
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
     * Share a secret
     * @param secret Secret BigDecimal value to share
     * @param fractionalBits number of bits for fixed point scaling. Must be positive
     * @return Array of arithmetic shares
     * @throws IllegalArgumentException Negative fractionalBits
    */
    public ArithmeticShare[] share(BigDecimal secret, int fractionalBits) throws IllegalArgumentException {
      return share(ArithmeticSharing.convertToFixedPoint(secret, fractionalBits));
    }

    /**
     * Share a secret
     * @param secret BigInteger secret value to share
     * @return Array of arithmetic shares
     */
    public ArithmeticShare[] share(BigInteger secret) {
        BigInteger[] shares = new BigInteger[numParties];
        shares[numParties - 1] = secret;
        for (int i = 0; i != numParties - 1; i++) {
            shares[i] = getSignedBlind(127);
            shares[numParties - 1] = shares[numParties - 1].add(prime).subtract(shares[i]);
        }
        ArithmeticShare[] result = new ArithmeticShare[numParties];
        for (int i = 0; i != numParties; i++) {
            result[i] = new ArithmeticShare(shares[i], prime);
        }
        return result;
    }

    /**
     * Share a secret
     * @param secret Secret double value to share
     * @param fractionalBits number of bits for fixed point scaling. Must be positive
     * @return Array of arithmetic shares
     * @throws IllegalArgumentException Negative fractionalBits
    */
    public ArithmeticShare[] share(double secret, int fractionalBits) throws IllegalArgumentException {
      return share(BigDecimal.valueOf(secret), fractionalBits);
    }

    /**
     * Share a secret
     * @param secret Secret integral value to share
     * @return Array of arithmetic shares
     */
    public ArithmeticShare[] share(int secret) {
        return share(BigInteger.valueOf(secret));
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
}

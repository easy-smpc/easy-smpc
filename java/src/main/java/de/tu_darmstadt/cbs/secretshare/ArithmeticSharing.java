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
            reconstruction = reconstruction.add(shares[i].value).mod(shares[i].prime);
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
     * Share a secret
     * @param secret
     * @return
     */
    public ArithmeticShare[] share(BigInteger secret) {
        BigInteger[] shares = new BigInteger[numParties];
        shares[numParties - 1] = secret;
        for (int i = 0; i != numParties - 1; i++) {
            shares[i] = new BigInteger(127, randomGenerator);
            shares[numParties - 1] = shares[numParties - 1].subtract(shares[i]).mod(prime);
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

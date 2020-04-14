package de.tu_darmstadt.cbs.secretshare;

import java.math.BigInteger;
import java.security.SecureRandom;

public class ArithmeticSharing {
    private BigInteger prime = BigInteger.valueOf(2).pow(127).subtract(BigInteger.ONE);
    private int num_parties;
    private SecureRandom random_generator = new SecureRandom();

    public ArithmeticSharing(int num_parties) {
        this.num_parties = num_parties;
    }

    public ArithmeticShare[] share(int secret) {
        return share(BigInteger.valueOf(secret));
    }

    public ArithmeticShare[] share(BigInteger secret) {
        BigInteger[] shares = new BigInteger[num_parties];
        shares[num_parties - 1] = secret;
        BigInteger sharingId = BigInteger.ZERO;
        if(System.getProperty("DebugSecretshare") != null && "true".equalsIgnoreCase(System.getProperty("DebugSecretshare"))) {
            sharingId = new BigInteger(127, random_generator);
        }
        for (int i = 0; i != num_parties - 1; i++) {
            shares[i] = new BigInteger(127, random_generator);
            shares[num_parties - 1] = shares[num_parties - 1].subtract(shares[i]).mod(prime);
        }
        ArithmeticShare[] result = new ArithmeticShare[num_parties];
        for (int i = 0; i != num_parties; i++) {
            result[i] = new ArithmeticShare(shares[i], prime, sharingId);
        }
        return result;
    }

    public static BigInteger reconstruct(ArithmeticShare[] shares) throws IllegalArgumentException {
        BigInteger reconstruction = BigInteger.ZERO;
        BigInteger first_prime = shares[0].prime;
        BigInteger first_sharingId = BigInteger.ZERO;
        if(System.getProperty("DebugSecretshare") != null && "true".equalsIgnoreCase(System.getProperty("DebugSecretshare"))) {
          first_sharingId = shares[0].sharingId;
        }
        for (int i = 0; i != shares.length; i++) {
            if(System.getProperty("DebugSecretshare") != null && "true".equalsIgnoreCase(System.getProperty("DebugSecretshare"))) {
              if (shares[i].sharingId != first_sharingId) {
                throw new IllegalArgumentException("Debug: Attempt to recombine unrelated shares!");
              }
            }
            if (shares[i].prime != first_prime) {
                throw new IllegalArgumentException("Incompatible primes found!");
            }
            reconstruction = reconstruction.add(shares[i].value).mod(shares[i].prime);
        }
        return reconstruction;
    }

    public void setPrime(BigInteger prime) {
        this.prime = prime;
    }
}

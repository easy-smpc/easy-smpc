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
        for (int i = 0; i != num_parties - 1; i++) {
            shares[i] = new BigInteger(127, random_generator);
            shares[num_parties - 1] = shares[num_parties - 1].subtract(shares[i]).mod(prime);
        }
        ArithmeticShare[] result = new ArithmeticShare[num_parties];
        for (int i = 0; i != num_parties; i++) {
            result[i] = new ArithmeticShare(shares[i], prime);
        }
        return result;
    }

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

    public void setPrime(BigInteger prime) {
        this.prime = prime;
    }
}

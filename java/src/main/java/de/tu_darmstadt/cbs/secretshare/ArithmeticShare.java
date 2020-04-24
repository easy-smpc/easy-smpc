package de.tu_darmstadt.cbs.secretshare;

import java.math.BigInteger;
import java.io.Serializable;

class DebugSecretsharing {
    public static boolean state = (System.getProperty("DebugSecretshare") != null
            && "true".equalsIgnoreCase(System.getProperty("DebugSecretshare")));
}

public class ArithmeticShare implements Serializable {
    final public BigInteger value;
    final public BigInteger prime;
    final public transient BigInteger sharingId;
    private static final long serialVersionUID = 5017971477461756174L;

    public ArithmeticShare(BigInteger value, BigInteger prime) {
        this.value = value;
        this.prime = prime;
        this.sharingId = BigInteger.ZERO;
    }

    public ArithmeticShare(BigInteger value, BigInteger prime, BigInteger sharingId) {
        this.value = value;
        this.prime = prime;
        if (DebugSecretsharing.state == true) {
            this.sharingId = sharingId;
        } else {
            this.sharingId = BigInteger.ZERO;
        }
    }

    @Override
    public String toString() {
        if (DebugSecretsharing.state == true) {
            return sharingId + ": " + value + " mod " + prime;
        } else {
            return value + " mod " + prime;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (!(o instanceof ArithmeticShare))
            return false;
        ArithmeticShare as = (ArithmeticShare) o;
        if (DebugSecretsharing.state == true) {
            return as.value.equals(value) && as.prime.equals(prime) && as.sharingId.equals(sharingId);
        } else {
            return as.value.equals(value) && as.prime.equals(prime);
        }
    }

    @Override
    public int hashCode() {
        int result = value.hashCode();
        result = 31 * result + prime.hashCode();
        if (DebugSecretsharing.state == true) {
            result = 31 * result + sharingId.hashCode();
        }
        return result;
    }

    public ArithmeticShare(ArithmeticShare other) {
        value = other.value;
        prime = other.prime;
        sharingId = other.sharingId;
    }

    public ArithmeticShare add(ArithmeticShare other) throws IllegalArgumentException {
        if (this.prime != other.prime)
            throw new IllegalArgumentException("Incompatible primes for addition");
        if (other.value == BigInteger.ZERO)
            return this;
        if (this.value == BigInteger.ZERO)
            return other;
        BigInteger sum = this.value.add(other.value).mod(this.prime);
        return new ArithmeticShare(sum, prime, this.sharingId.add(other.sharingId));
    }

    /// Disallow uninitialized or incomplete instance
    private ArithmeticShare() {
        value = BigInteger.ZERO;
        prime = BigInteger.ZERO;
        sharingId = BigInteger.ZERO;
    };
}

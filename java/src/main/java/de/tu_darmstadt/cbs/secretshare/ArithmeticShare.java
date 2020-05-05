package de.tu_darmstadt.cbs.secretshare;

import java.math.BigInteger;
import java.io.Serializable;

public class ArithmeticShare implements Serializable {
    final public BigInteger value;
    final public BigInteger prime;
    private static final long serialVersionUID = 5017971477461756174L;

    public ArithmeticShare(BigInteger value, BigInteger prime) {
        this.value = value;
        this.prime = prime;
    }

    @Override
    public String toString() {
        return value + " mod " + prime;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (!(o instanceof ArithmeticShare))
            return false;
        ArithmeticShare as = (ArithmeticShare) o;
        return as.value.equals(value) && as.prime.equals(prime);
    }

    @Override
    public int hashCode() {
        int result = value.hashCode();
        result = 31 * result + prime.hashCode();
        return result;
    }

    public ArithmeticShare(ArithmeticShare other) {
        value = other.value;
        prime = other.prime;
    }

    public ArithmeticShare add(ArithmeticShare other) throws IllegalArgumentException {
        if (this.prime != other.prime)
            throw new IllegalArgumentException("Incompatible primes for addition");
        if (other.value == BigInteger.ZERO)
            return this;
        if (this.value == BigInteger.ZERO)
            return other;
        BigInteger sum = this.value.add(other.value).mod(this.prime);
        return new ArithmeticShare(sum, prime);
    }

    /// Disallow uninitialized or incomplete instance
    private ArithmeticShare() {
        value = BigInteger.ZERO;
        prime = BigInteger.ZERO;
    };
}

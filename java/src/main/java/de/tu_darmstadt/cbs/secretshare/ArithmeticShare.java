package de.tu_darmstadt.cbs.secretshare;

import java.math.BigInteger;
import java.io.Serializable;

/**
 * This class implements an arithmetic share
 * @author Tobias Kussel
 */
public class ArithmeticShare implements Serializable, Cloneable {

    /** SVUID*/
    private static final long serialVersionUID = 5017971477461756174L;
    
    /** Value*/
    public final BigInteger value;
    /** Prime*/
    public final BigInteger prime;

    /**
     * Creates a copy of another arithmetic share
     * @param other
     */
    public ArithmeticShare(ArithmeticShare other) {
        value = other.value;
        prime = other.prime;
    }

    /**
     * Creates a new instance
     * @param value
     * @param prime
     */
    public ArithmeticShare(BigInteger value, BigInteger prime) {
        this.value = value;
        this.prime = prime;
    }

    /**
     * Adds another arithmatic share
     * @param other
     * @return
     * @throws IllegalArgumentException
     */
    public ArithmeticShare add(ArithmeticShare other) throws IllegalArgumentException {
        if (!this.prime.equals(other.prime))
            throw new IllegalArgumentException("Incompatible primes for addition");
        if (other.value == BigInteger.ZERO)
            return this;
        if (this.value == BigInteger.ZERO)
            return other;
        BigInteger sum = this.value.add(other.value).mod(this.prime);
        return new ArithmeticShare(sum, prime);
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

    @Override
    public String toString() {
        return value + " mod " + prime;
    }
    @Override
    public Object clone() {
      try {
        return (ArithmeticShare) super.clone();
      } catch (CloneNotSupportedException e) {
        return new ArithmeticShare(this.value, this.prime);
      }
    }
}

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

import java.io.Serializable;
import java.math.BigInteger;

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
     * Adds another arithmetic share
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
        BigInteger sum = this.value.add(other.value).remainder(this.prime);
        return new ArithmeticShare(sum, prime);
    }

    @Override
    public Object clone() {
        return new ArithmeticShare(this.value, this.prime);
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
}

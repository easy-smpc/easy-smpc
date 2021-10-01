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
package de.tu_darmstadt.cbs.emailsmpc;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Arrays;

import de.tu_darmstadt.cbs.secretshare.ArithmeticShare;
import de.tu_darmstadt.cbs.secretshare.ArithmeticSharing;

/**
 * Class representing a bin
 * @author Tobias Kussel
 */
public class Bin implements Serializable, Cloneable {
    
    /** SVUID. */
    private static final long serialVersionUID = -8804264711786268229L;
    
    /** The name. */
    public final String name;
    
    /** The input shares. */
    private ArithmeticShare[] inShares;
    
    /** The output shares. */
    private ArithmeticShare[] outShares;

    /**
     * Instantiates a new bin.
     *
     * @param name the name
     */
    public Bin(String name) {
        this.name = name;
        inShares = null;
        outShares = null;
    }

    /**
     * Instantiates a new bin.
     *
     * @param name the name
     * @param numParties the num parties
     */
    public Bin(String name, int numParties) {
        this.name = name;
        inShares = new ArithmeticShare[numParties];
        outShares = new ArithmeticShare[numParties];
    }

    /**
     * Clear in shares except id.
     *
     * @param id the id
     */
    public void clearInSharesExceptId(int id) {
        for (int i = 0; i < inShares.length; i++) {
            if (i != id)
                inShares[i] = null;
        }
    }

    /**
     * Clear out shares except id.
     *
     * @param id the id
     */
    public void clearOutSharesExceptId(int id) {
        for (int i = 0; i < outShares.length; i++) {
            if (i != id)
                outShares[i] = null;
        }
    }

    /**
     * Clear shares.
     */
    public void clearShares() {
        for (int i = 0; i < inShares.length; i++) {
            inShares[i] = null;
            outShares[i] = null;
        }
    }

    /**
     * Clone.
     *
     * @return the object
     */
    @Override
    public Object clone() {
      Bin newBin = new Bin(this.name, this.inShares.length);
      for (int i = 0; i < this.inShares.length; i++) {
        if (this.inShares[i] != null)
          newBin.inShares[i] = (ArithmeticShare) this.inShares[i].clone();
        if (this.outShares[i] != null)
          newBin.outShares[i] =(ArithmeticShare) this.outShares[i].clone();
      }
      return newBin;
    }

    /**
     * Equals.
     *
     * @param o the o
     * @return true, if successful
     */
    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (!(o instanceof Bin))
            return false;
        Bin b = (Bin) o;
        boolean result = b.name.equals(name);
        result = result && (inShares.length == b.inShares.length);
        result = result && (outShares.length == b.outShares.length);
        for (int i = 0; i < inShares.length; i++) {
            if (b.inShares[i] != null)
                result = result && b.inShares[i].equals(inShares[i]);
            else
                result = result && (inShares[i] == null);
        }
        for (int i = 0; i < outShares.length; i++) {
            if (b.outShares[i] != null)
                result = result && b.outShares[i].equals(outShares[i]);
            else
                result = result && (outShares[i] == null);
        }
        return result;
    }

    /**
     * Gets the filled in share indices.
     *
     * @return the filled in share indices
     * @throws IllegalArgumentException the illegal argument exception
     */
    public int[] getFilledInShareIndices() throws IllegalArgumentException {
        return getFilledArrayIndices(inShares);
    }

    /**
     * Gets the filled out share indices.
     *
     * @return the filled out share indices
     * @throws IllegalArgumentException the illegal argument exception
     */
    public int[] getFilledOutShareIndices() throws IllegalArgumentException {
        return getFilledArrayIndices(outShares);
    }

    /**
     * Gets the out share.
     *
     * @param participant the participant
     * @return the out share
     */
    public ArithmeticShare getOutShare(int participant) {
        return outShares[participant];
    }

    /**
     * Gets the sum share.
     *
     * @return the sum share
     * @throws IllegalStateException the illegal state exception
     */
    public ArithmeticShare getSumShare() throws IllegalStateException {
        if (!isComplete())
            throw new IllegalStateException("Can not reconstruct incomplete shares");
        ArithmeticShare sum = inShares[0];
        for (int i = 0; i < inShares.length; i++) {
            if (i != 0) { // sum is already initialized as share 0
                sum = sum.add(inShares[i]);
            }
        }
        return sum;
    }

    /**
     * Hash code.
     *
     * @return the int
     */
    @Override
    public int hashCode() {
        int result = name.hashCode();
        for (ArithmeticShare as : inShares) {
            if (as != null)
                result = 31 * result + as.hashCode();
            else
                result = 31 * result;
        }
        for (ArithmeticShare as : outShares) {
            if (as != null)
                result = 31 * result + as.hashCode();
            else
                result = 31 * result;
        }
        return result;
    }

    /**
     * Initialize.
     *
     * @param numParties the num parties
     * @throws IllegalStateException the illegal state exception
     */
    public void initialize(int numParties) throws IllegalStateException {
        if (inShares != null)
            throw new IllegalStateException("Unable to initialize already initialized bin");
        inShares = new ArithmeticShare[numParties];
        outShares = new ArithmeticShare[numParties];
    }

    /**
     * Checks if is complete.
     *
     * @return true, if is complete
     */
    public boolean isComplete() {
        for (ArithmeticShare b : inShares) {
            if (b == null)
                return false;
        }
        return true;
    }

    /**
     * Checks if is complete for participant id.
     *
     * @param participantId the participant id
     * @return true, if is complete for participant id
     */
    public boolean isCompleteForParticipantId(int participantId) {
        return inShares[participantId] != null ? true : false;
    }

    /**
     * Checks if is initialized.
     *
     * @return true, if is initialized
     */
    public boolean isInitialized() {
        return inShares != null;
    }

    /**
     * Reconstruct bin.
     *
     * @return the big integer
     * @throws IllegalStateException the illegal state exception
     */
    public BigDecimal reconstructBin(int fractionalBits) throws IllegalStateException, IllegalArgumentException {
        if (fractionalBits < 0)
          throw new IllegalArgumentException("fractionalBits must be positive");
        if (!isComplete())
            throw new IllegalStateException("Can not reconstruct incomplete shares");
        return ArithmeticSharing.reconstruct(inShares, fractionalBits);
    }
    
    /**
     * Sets the in share.
     *
     * @param share the share
     * @param participant the participant
     */
    public void setInShare(ArithmeticShare share, int participant) {
        inShares[participant] = share;
    }

    /**
     * Sets the in shares.
     *
     * @param shares the new in shares
     * @throws IllegalArgumentException the illegal argument exception
     */
    public void setInShares(ArithmeticShare[] shares) throws IllegalArgumentException {
        if (shares.length != inShares.length) {
            throw new IllegalArgumentException("Number of shares not compatible with number of parties");
        }
        inShares = shares;
    }

    /**
     * Share value.
     *
     * @param value the value
     * @throws IllegalStateException the illegal state exception
     * @throw IllegalArgumentException fractionalBits must be positive
     */
    public void shareValue(BigDecimal value, int fractionalBits) throws IllegalStateException, IllegalArgumentException {
        if (fractionalBits < 0)
          throw new IllegalArgumentException("fractionalBits must be positive");
        if (!isInitialized())
            throw new IllegalStateException("Unable to share value in unititialized bin");
        ArithmeticSharing as = new ArithmeticSharing(outShares.length);
        outShares = as.share(value, fractionalBits);
    }
    /**
     * To string.
     *
     * @return the string
     */
    @Override
    public String toString() {
        String result = name + "\nInShares:\n";
        for (ArithmeticShare as : inShares) {
            result = result + as + "\n";
        }
        result = result + "\nOutShares:\n";
        for (ArithmeticShare as : outShares) {
            result = result + as + "\n";
        }
        return result;

    }

    /**
     * Transfer shares out in.
     *
     * @param ownId the own id
     */
    public void transferSharesOutIn(int ownId) {
        inShares[ownId] = outShares[ownId];
        outShares[ownId] = null;
    }
    
    /**
     * Gets the filled array indices.
     *
     * @param array the array
     * @return the filled array indices
     * @throws IllegalArgumentException the illegal argument exception
     */
    private int[] getFilledArrayIndices(ArithmeticShare[] array) throws IllegalArgumentException {
        if (array == null)
            throw new IllegalArgumentException("Not a valid array");
        int[] result = new int[0]; // How is Data locality of lists in Java?
        for (int i = 0; i < array.length; i++) {
            if (array[i] != null) {
                result = Arrays.copyOf(result, result.length + 1);
                result[result.length - 1] = i;
            }
        }
        return result;
    }
}

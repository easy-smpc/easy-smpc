package de.tu_darmstadt.cbs.emailsmpc;

import de.tu_darmstadt.cbs.secretshare.*;
import java.math.BigInteger;
import java.io.Serializable;

public class Bin implements Serializable {
    public final String name;
    private ArithmeticShare[] inShares;
    private ArithmeticShare[] outShares;
    private static final long serialVersionUID = -8804264711786268229L;

    public Bin(String name, int numParties) {
        this.name = name;
        inShares = new ArithmeticShare[numParties];
        outShares = new ArithmeticShare[numParties];
    }

    public Bin(String name) {
        this.name = name;
        inShares = null;
        outShares = null;
    }

    public void initialize(int numParties) throws IllegalStateException {
        if (inShares != null)
            throw new IllegalStateException("Unable to initialize already initialized bin");
        inShares = new ArithmeticShare[numParties];
        outShares = new ArithmeticShare[numParties];
    }

    public boolean isInitialized() {
        return inShares != null;
    }

    public void shareValue(BigInteger value) throws IllegalStateException {
        if (!isInitialized())
            throw new IllegalStateException("Unable to share value in unititialized bin");
        ArithmeticSharing as = new ArithmeticSharing(outShares.length);
        outShares = as.share(value);
    }

    public ArithmeticShare getOutShare(int participant) {
        return outShares[participant];
    }

    public void setInShares(ArithmeticShare[] shares) throws IllegalArgumentException {
        if (shares.length != inShares.length) {
            throw new IllegalArgumentException("Number of shares not compatible with number of parties");
        }
        inShares = shares;
    }

    public void setInShare(ArithmeticShare share, int participant) {
        inShares[participant] = share;
    }

    public void transferSharesOutIn(int ownId) {
        inShares[ownId] = outShares[ownId];
        outShares[ownId] = null;
    }

    public ArithmeticShare getSumShare() throws IllegalStateException {
        ArithmeticShare sum = inShares[0];
        if (!isComplete())
                throw new IllegalStateException("Can not reconstruct incomplete shares");
        for (int i = 0; i < inShares.length; i++) {
            if (i != 0) { // sum is already initialized as share 0
                sum = sum.add(inShares[i]);
            }
        }
        return sum;
    }

    public boolean isComplete() {
        for (ArithmeticShare b : inShares) {
            if (b == null)
                return false;
        }
        return true;
    }

    public BigInteger reconstructBin() throws IllegalStateException {
        if (!isComplete())
                throw new IllegalStateException("Can not reconstruct incomplete shares");
        return ArithmeticSharing.reconstruct(inShares);
    }
}

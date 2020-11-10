package de.tu_darmstadt.cbs.emailsmpc;

import java.math.BigInteger;

public class BinResult{
    public String name;
    public BigInteger value;

    BinResult(String name, BigInteger value) {
        this.name = name;
        this.value = value;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (!(o instanceof BinResult))
            return false;
        BinResult br = (BinResult) o;
        return br.value.equals(value) && br.name.equals(name);
    }

    @Override
    public int hashCode() {
        int result = value.hashCode();
        return 31 * result + name.hashCode();
    }

    @Override
    public String toString() {
        return name + ": " + value.toString();
    }
}

package de.tu_darmstadt.cbs.emailsmpc;

import static org.junit.Assert.assertTrue;
import java.math.BigInteger;
import java.io.File;
import java.io.IOException;
import java.util.Set;
import java.util.HashSet;

import org.junit.Test;

/**
 * Unit test for simple App.
 */
public class BinTest {
  private static Bin getInitializedBin() {
    Bin bin = new Bin("Testbin", 4);
    bin.shareValue(BigInteger.valueOf(5));
    return bin;
  }

  @Test
  public void testClone() {
    Bin original = BinTest.getInitializedBin();
    Bin copy = (Bin) original.clone();
    assertTrue(original != copy);
    assertTrue(original.equals(copy));
  }

}


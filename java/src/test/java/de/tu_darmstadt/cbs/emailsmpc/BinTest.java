package de.tu_darmstadt.cbs.emailsmpc;

import static org.junit.Assert.assertTrue;

import java.math.BigInteger;

import org.junit.Test;

/**
 * Test basic classes and clone
 * @author Tobias Kussel
 */
public class BinTest {
  
  /**
   * Gets the initialized bin.
   *
   * @return the initialized bin
   */
  private static Bin getInitializedBin() {
    Bin bin = new Bin("Testbin", 4);
    bin.shareValue(BigInteger.valueOf(5));
    return bin;
  }

  /**
   * Test clone.
   */
  @Test
  public void testClone() {
    Bin original = BinTest.getInitializedBin();
    Bin copy = (Bin) original.clone();
    assertTrue(original != copy);
    assertTrue(original.equals(copy));
  }

}


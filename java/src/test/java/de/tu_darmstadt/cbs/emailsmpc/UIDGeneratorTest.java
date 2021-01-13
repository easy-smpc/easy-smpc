package de.tu_darmstadt.cbs.emailsmpc;

import static org.junit.Assert.assertTrue;
import org.junit.Test;

/**
 * Unit Test for UID Generator.
 * @author Tobias Kussel
 */
public class UIDGeneratorTest {
  
  /**
   * Test length.
   */
  @Test
  public void testLength() {
    for (int i = 2; i < 64+1; i *= 2 ){
      String uid = UIDGenerator.generateShortUID(i);
      assertTrue(uid.length() == i);
    }
  }
}


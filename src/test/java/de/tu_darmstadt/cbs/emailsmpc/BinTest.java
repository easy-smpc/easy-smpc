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

import static org.junit.Assert.assertTrue;
import java.math.BigDecimal;
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
    bin.shareValue(BigDecimal.valueOf(5), 32);
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


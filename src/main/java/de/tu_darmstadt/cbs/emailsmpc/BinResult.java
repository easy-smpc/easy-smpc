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

import java.math.BigInteger;
import java.math.BigDecimal;

/**
 * Result for a bin
 * @author Tobias Kussel
 */
public class BinResult{
    
    /** The name. */
    public String name;
    
    /** The value. */
    @Deprecated
    public BigDecimal value;

    /**
     * Instantiates a new bin result.
     *
     * @param name the name
     * @param value the value
     */
    BinResult(String name, BigDecimal value) {
        this.name = name;
        this.value = value;
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
        if (!(o instanceof BinResult))
            return false;
        BinResult br = (BinResult) o;
        boolean result = br.name.equals(name);
        if (value != null && br.value != null)
          result &= br.value.equals(value);
        if (value != null && br.value != null)
          result &= br.value.equals(value);
        return result;
    }

    /**
     * Hash code.
     *
     * @return the int
     */
    @Override
    public int hashCode() {
        int result = 0;
        if (value != null)
          result += value.hashCode();
        if (value != null)
          result += value.hashCode();
        return 31 * result + name.hashCode();
    }

    /**
     * To string.
     *
     * @return the string
     */
    @Override
    public String toString() {
      String result = new String(name);
      if (value != null)
        result += ": " + value.toString();
      if (value != null)
        result += ": " + value.toString();
      return result;
    }
}

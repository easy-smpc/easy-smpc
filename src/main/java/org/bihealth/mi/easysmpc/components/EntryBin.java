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
package org.bihealth.mi.easysmpc.components;

import java.math.BigDecimal;
import java.math.BigInteger;

import org.bihealth.mi.easysmpc.resources.Resources;

/**
 * Entry for bins
 * 
 * @author Fabian Prasser
 * @author Felix Wirth
 */
public class EntryBin extends ComponentEntryAddRemove {
    
    /** SVUID*/
    private static final long serialVersionUID = 950691229934119178L;
    /** LowerInt*/
    private static final BigInteger LOWERINT = ((BigInteger.valueOf(2).pow(126)).negate()).add(BigInteger.ONE); // -2^126+1
    /** UpperInt*/
    private static final BigInteger UPPERINT = ((BigInteger.valueOf(2).pow(126)).subtract(BigInteger.ONE)); // 2^126-1
    /** LowerDec*/
    private static final BigDecimal LOWERDEC = ((BigDecimal.valueOf(2).pow(93)).negate()).add(BigDecimal.ONE); // -2^93+1
    /** UpperDec*/
    private static final BigDecimal UPPERDEC = ((BigDecimal.valueOf(2).pow(93)).subtract(BigDecimal.ONE)); // 2^93-1
    
    /**
     * Checks whether the integral value is within range [-2^126+1, 2^126-1]
     * @param value
     * @return
     */
    private static final boolean isInRange(BigInteger value) {
        return (value.compareTo(LOWERINT) >= 0) && (value.compareTo(UPPERINT) <= 0);
    }

    /**
     * Checks whether the decimal value is within range [-2^93+1, 2^93-1]
     * @param value
     * @return
     */
    private static final boolean isInRange(BigDecimal value) {
        return (value.compareTo(LOWERDEC) >= 0) && (value.compareTo(UPPERDEC) <= 0);
    }

    /**
     * Creates a new instance
     */
    public EntryBin() {
       this("", true, String.valueOf(0), true, true);
    }

    /**
     * Creates a new instance
     * @param enabled
     */
    public EntryBin(boolean enabled) {
        this("", enabled, "", enabled, enabled);
    }
    
    /**
     * Creates a new instance
     * @param name
     * @param leftEnabled
     * @param value - zero if empty
     * @param rightEnabled
     * @param additionalControlsEnabled
     */
    public EntryBin(String name,
                    boolean leftEnabled,
                    String value,
                    boolean rightEnabled,
                    boolean additionalControlsEnabled) {
        super(Resources.getString("BinEntry.0"), //$NON-NLS-1$
              name,
              leftEnabled,
              new ComponentTextFieldValidator() {
                  @Override
                  public boolean validate(String text) {
                      return !text.trim().isEmpty();
                  }
              },
              Resources.getString("BinEntry.1"), //$NON-NLS-1$
              (value != null && !value.trim().isEmpty()) ? value : String.valueOf(0),
              rightEnabled,
              new ComponentTextFieldValidator() {
                  @Override
                  public boolean validate(String text) {
                      try {
                          return isInRange(new BigDecimal(text.trim().replace(',', '.')));
                      } catch (Exception e) {
                          return false;
                      }
                  }
              },              
              additionalControlsEnabled);
    }

    /**
     * Creates a new instance
     * @param name
     * @param value
     */
    public EntryBin(String name, String value) {
        super(Resources.getString("BinEntry.0"), //$NON-NLS-1$
              name,
              false,
              new ComponentTextFieldValidator() {
                  @Override
                  public boolean validate(String text) {
                      return !text.trim().isEmpty();
                  }
              },
              Resources.getString("BinEntry.1"), //$NON-NLS-1$
              value,
              false,
              new ComponentTextFieldValidator() {
                  @Override
                  public boolean validate(String text) {
                      try {
                          return isInRange(new BigDecimal(text.trim().replace(',', '.')));
                      } catch (Exception e) {
                          return false;
                      }
                  }
              },
              false);
    }
    
    /**
     * Returns true if left value is empty and right value is empty or "0"
     */
    @Override
    public boolean isEmpty() {
        return (this.getLeftValue().trim().isEmpty() &&
                 (this.getRightValue().trim().isEmpty()) ||
                  this.getRightValue().trim().equals(String.valueOf(0)));        
    }
}

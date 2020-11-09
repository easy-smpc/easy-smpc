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
package de.tu_darmstadt.cbs.app.components;

import java.math.BigInteger;

import de.tu_darmstadt.cbs.app.resources.Resources;

/**
 * Entry for bins in the histogram
 * 
 * @author Fabian Prasser
 * @author Felix Wirth
 */
public class EntryBin extends ComponentEntryAddRemove {
    
    /** SVUID*/
    private static final long serialVersionUID = 950691229934119178L;

    /**
     * Creates a new instance
     */
    public EntryBin() {
       this("", true, true, true);
    }

    /**
     * Creates a new instance
     * @param enabled
     */
    public EntryBin(boolean enabled) {
       this("", enabled, enabled, enabled);
    }
    
    /**
     * Creates a new instance
     * @param name
     * @param leftEnabled
     * @param rightEnabled
     * @param buttonsEnabled
     */
    public EntryBin(String name, boolean leftEnabled, boolean rightEnabled, boolean buttonsEnabled) {
        super(Resources.getString("BinEntry.0"), //$NON-NLS-1$
              name,
              leftEnabled,
              new ComponentTextFieldValidator() {
                  @Override
                  public boolean validate(String text) {
                      // TODO: Must ensure that no two bins have the same name
                      return !text.trim().isEmpty();
                  }
              },
              Resources.getString("BinEntry.1"), //$NON-NLS-1$
              String.valueOf(0),
              rightEnabled,
              new ComponentTextFieldValidator() {
                  @Override
                  public boolean validate(String text) {
                      try {
                          new BigInteger(text.trim());
                          return true;
                      } catch (Exception e) {
                          return false;
                      }
                  }
              },
              buttonsEnabled);
    }
    
    
    /**
     * Creates a new instance
     * @param name
     * @param leftEnabled
     * @param rightEnabled
     * @param buttonsEnabled
     */
    public EntryBin(String name, String value) {
        super(Resources.getString("BinEntry.0"), //$NON-NLS-1$
              name,
              false,
              new ComponentTextFieldValidator() {
                  @Override
                  public boolean validate(String text) {
                      // TODO: Must ensure that no two bins have the same name
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
                          Integer.valueOf(text);
                          return true;
                      } catch (Exception e) {
                          return false;
                      }
                  }
              },
              false);
    }
}

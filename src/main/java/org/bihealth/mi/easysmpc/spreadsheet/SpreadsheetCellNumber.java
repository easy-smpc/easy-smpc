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
package org.bihealth.mi.easysmpc.spreadsheet;

import java.math.BigDecimal;

/**
 * @author Felix Wirth
 *
 */
public class SpreadsheetCellNumber extends SpreadsheetCell {
    /** Value if in text */
    private BigDecimal value;
    /** Function to calculate if applicable */
    private ScriptFunction function;
    
    /**
     * Create a new instance
     * 
     * @param value
     */
    public SpreadsheetCellNumber(BigDecimal value) {
        super(SpreadsheetCellType.NUMBER);
        this.value = value;
    }

    @Override
    public String getContentDefinition() {
        return this.value.toPlainString();
    }

    @Override
    public String getDisplayedText() {
        return getContentDefinition();
    }
    
    @Override
    public BigDecimal getContentBigDecimal() {
        return this.value;
    }
    
    @Override
    public boolean isCalculable() {
        return true;
    }
}

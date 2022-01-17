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
public class SpreadsheetCellFunctionMean extends SpreadsheetCellFunction {
    
    /**
     * Create a new instance
     * 
     * @param values
     * @param accessor
     */
    SpreadsheetCellFunctionMean(String values, CellsAccessor accessor) {
        super(values, accessor, true);
    }
    
    @Override
    public BigDecimal calculate() {
        // Check
        if (!isCalculable()) {
            return null;
        }

        // Calculate mean
        BigDecimal sum = new BigDecimal(0);
        int index = 0;
        for (SpreadsheetCell cell : getRelevantCells()) {
            sum = sum.add(cell.getContentBigDecimal());
            index++;
        }
        return sum.divide(BigDecimal.valueOf(index));
    }

    @Override
    public boolean isCalculable() {
        for (SpreadsheetCell cell : getRelevantCells()) {
            if (cell == null || !cell.isCalculable()) {
                return false;
            }
        }
        return true;
    }
}

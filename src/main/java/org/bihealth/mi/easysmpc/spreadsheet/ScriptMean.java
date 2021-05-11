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

import javax.swing.table.TableModel;

/**
 * @author Felix Wirth
 *
 */
public class ScriptMean extends ScriptFunction {
    
    ScriptMean(String values, TableModel tableModel) {
        super(values, tableModel, true);
    }
    
    /**
     * Can the script be calculated?
     * 
     * @return
     */
    public boolean isCaluable() {
        for(SpreadsheetCell cell : getRelevantCells()) {
            if (!cell.isCalculable()) {
                return false;
            }
        }        
        return true;
    }

    @Override
    public BigDecimal calculate() {
        // Check
        if (!isCaluable()) {
            return null;
        }
        
        // Calculate mean
        BigDecimal sum = BigDecimal.valueOf(0);
        int index = 0;
        for(SpreadsheetCell cell : getRelevantCells()) {
            sum.add(cell.getContentBigDecimal());
            index++;
        }
        return sum.divide(BigDecimal.valueOf(index));
    }
}

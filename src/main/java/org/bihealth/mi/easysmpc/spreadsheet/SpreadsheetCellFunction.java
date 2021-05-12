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
import java.util.ArrayList;
import java.util.List;

import javax.swing.table.TableModel;

/**
 * @author Felix Wirth
 *
 */
public abstract class SpreadsheetCellFunction extends SpreadsheetCell {
    /** Value if in text */
    private String value;
    /** Regex for cell reference */
    private static final String CELL_REFERENCE = "(([A-Z]){1,3}([0-9]){1,7})";
    /** Regex for cell range */
    private static final String CELL_RANGE = "(" + CELL_REFERENCE + ":" + CELL_REFERENCE + ")";
    /** Regex for cell reference or range */
    private static final String CELL_REFERENCE_RANGE ="((" + CELL_RANGE + "|" + CELL_REFERENCE + ");)+";
    /** Regex for function name with cell references */
    private static final String FUNCTION_WITH_CELL = "=([a-z]*)(\\(){1}" + CELL_REFERENCE_RANGE + "(\\)){1}";
    /** Base for column letters to numbers and v.v. */
    public static String BASE = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    /** Table model */
    private TableModel tableModel;
    /** Rows of relevant cells */
    private final List<Integer> relevantRows = new ArrayList<>();
    /** Cols of relevant cells */
    private final List<Integer> relevantCols = new ArrayList<>();
    /** Is secret function */
    private final boolean isSecret;
    
    /**
     * Create a new instance
     * 
     * @param value
     */
    public SpreadsheetCellFunction(String values, TableModel tableModel, boolean isSecret) {
        super(SpreadsheetCellType.SCRIPT);

        // Prepare
        this.tableModel = tableModel;
        this.isSecret = isSecret;
        String[] splitValues = values.split(";");

        // Obtain table coordinates out of string
        for (String valuePart : splitValues) {
            if (valuePart.contains(":")) {
                // TODO
            } else {
                // Add row
                this.relevantRows.add(Integer.valueOf(valuePart.replaceFirst("([A-Z]){1,3}", "")));

                // Add cols
                this.relevantCols.add(getExcelColumnName(valuePart.replaceFirst("([0-9]){1,7}","")));
            }
        }
    }
    
    public abstract BigDecimal calculate();

    /**
     * @return the secret
     */
    public boolean isSecret() {
        return isSecret;
    }

    public static SpreadsheetCellFunction createNew(String text, TableModel tableModel) {
        // TODO Fix semicolon
        if (!text.matches(FUNCTION_WITH_CELL)) {
            throw new IllegalArgumentException("Script does not start with the correct sign!");
        }

        String functionName = text.substring(1, text.indexOf("(")).toUpperCase();

        try {
            ScriptFunctionType function = ScriptFunctionType.valueOf(functionName);
            switch (function) {
            case MEAN:
                return new SpreadsheetCellFunctionMean(text.substring(text.indexOf("(") + 1, text.length() - 2), tableModel);
            default:
                throw new IllegalArgumentException(String.format("Unknown script function %s",
                                                                 functionName));
            }
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Unable to understand script", e);
        }
    }
    
    @Override
    public String getContentDefinition() {
        return this.value;
    }

    @Override
    public String getDisplayedText() {
        return calculate().toPlainString();
    }

    @Override
    public BigDecimal getContentBigDecimal() {
        return calculate();
    }

    
    /**
     * @return the relevant cells
     */
    protected List<SpreadsheetCell> getRelevantCells() {
        // Init
        List<SpreadsheetCell> result = new ArrayList<>();
        int index = 0;

        // Loop over relevant rows and cells
        for (int row : relevantRows) {
            result.add((SpreadsheetCell) this.tableModel.getValueAt(row, relevantCols.get(index)));
            index++;
        }

        // Return
        return result;
    }
    
    /**
     * Possible script function names
     * 
     * @author Felix Wirth
     *
     */
    public enum ScriptFunctionType {
                                    MEAN,
                                    SECRET_ADD
    }
    
    /**
     * Converts a colum letter to a number
     * 
     * @param columnAlpha
     * @return number
     */
    public static int getExcelColumnName(String columnAlpha) {
        int result = 0;
        for (int index = 0; index < columnAlpha.length(); index++) {
            // Prepare
            int indexPosition = BASE.indexOf(columnAlpha.substring(index, index + 1));

            // Check
            if (indexPosition == -1) {
               throw new IllegalArgumentException(String.format("Illegal character %s", columnAlpha.substring(index, index + 1)));
            }

            // Add result
            result *= BASE.length();
            result += indexPosition + 1;
        }

        // Return
        return result - 1;
    }
}

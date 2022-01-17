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

/**
 * @author Felix Wirth
 *
 */
public abstract class SpreadsheetCellFunction extends SpreadsheetCell {
    /** Formular definition text */
    private String value;
    /** Regex for cell reference */
    private static final String CELL_REFERENCE = "(([A-Z]){1,3}([0-9]){1,7})";
    /** Regex for cell range */
    private static final String CELL_RANGE = "(" + CELL_REFERENCE + ":" + CELL_REFERENCE + ")";
    /** Regex for cell reference or range */
    private static final String CELL_REFERENCE_RANGE ="((" + CELL_RANGE + "|" + CELL_REFERENCE + ");)+";
    /** Regex for function name with cell references */
    private static final String FUNCTION_WITH_CELL = "=([a-zA-Z]*)(\\(){1}" + CELL_REFERENCE_RANGE + "(\\)){1}";
    /** Base for column letters to numbers and v.v. */
    public static String BASE = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    /** InternalDataProvider */
    private final InternalDataProvider internalDataProvider;
    /** Rows of relevant cells */
    private final List<Integer> relevantRows = new ArrayList<>();
    /** Columns of relevant cells */
    private final List<Integer> relevantCols = new ArrayList<>();
    /** Is secret function */
    private final boolean isSecret;
    /** ExternalDataProvider */
    private final ExternalDataProvider externalDataProvider;
    

    /**
     * Create a new instance
     * 
     * @param text
     * @param internalDataProvider
     * @param externalDataProvider
     * @param isSecret
     */
    SpreadsheetCellFunction(String text, InternalDataProvider internalDataProvider, ExternalDataProvider externalDataProvider, boolean isSecret) {
        super(SpreadsheetCellType.SCRIPT);

        // Prepare
        this.internalDataProvider = internalDataProvider;
        this.externalDataProvider = externalDataProvider;
        this.isSecret = isSecret;
        this.value = text;
        String[] splitValues = text.substring(text.indexOf("(") + 1, text.length() - 2).split(";");

        // Obtain table coordinates out of string
        for (String valuePart : splitValues) {
            if (valuePart.contains(":")) {
                // TODO
            } else {
                // Add row
                this.relevantRows.add(getRowFromString(valuePart));

                // Add cols
                this.relevantCols.add(getColumnNameAlpha(valuePart));
            }
        }
    }

    /**
     * Get a row from a string
     * 
     * @param valuePart
     * @return
     */
    private Integer getRowFromString(String valuePart) {
        return (Integer.valueOf(valuePart.replaceFirst("([A-Z]){1,3}", "")) - 1);
    }
    
    /**
     * Calculates a result
     * 
     * @return
     */
    public abstract BigDecimal calculate();

    /**
     * @return the secret
     */
    public boolean isSecret() {
        return isSecret;
    }
    
    /**
     * Picks the correct sub-type and create a new instance of the type
     * 
     * @param text
     * @param InternalDataProvider
     * @return
     */
    public static SpreadsheetCellFunction createNew(String text, InternalDataProvider internalDataProvider) {
        return createNew(text, internalDataProvider, null);
    }
    
    /**
     * Picks the correct sub-type and create a new instance of the type
     * 
     * @param text
     * @param internalDataProvider
     * @param externalDataProvider
     * @return
     */
    public static SpreadsheetCellFunction createNew(String text, InternalDataProvider internalDataProvider, ExternalDataProvider externalDataProvider) {
        // TODO Fix this semicolon hack
        text = text.substring(0, text.length() - 1) + ";" + text.substring(text.length() - 1, text.length());
        if (!text.matches(FUNCTION_WITH_CELL)) {
            throw new IllegalArgumentException("Script does not start with the correct sign!");
        }
        
        // Create a respective sub class from function name
        String functionName = text.substring(1, text.indexOf("(")).toUpperCase();
        try {
            ScriptFunctionType function = ScriptFunctionType.valueOf(functionName);
            switch (function) {
            case MEAN:
                return new SpreadsheetCellFunctionMean(text, internalDataProvider);
            case SMPCADD:
                return new SpreadsheetCellFunctionSMPCAddition(text, internalDataProvider, externalDataProvider);
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
            result.add(this.internalDataProvider.getCellAt(row, relevantCols.get(index)));
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
                                    SMPCADD
    }
    
    /**
     * Converts a column letter to a number
     * 
     * @param columnAlpha
     * @return number
     */
    public static int getColumnNameAlpha(String columnAlpha) {
        // Remove unnecessary text
        columnAlpha = columnAlpha.replaceFirst("([0-9]){1,7}","");
        
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
        return result;
    }

    /**
     * @return the InternalDataProvider
     */
    protected InternalDataProvider getInternalDataProvider() {
        return internalDataProvider;
    }

    /**
     * @return the relevantRows
     */
    protected List<Integer> getRelevantRows() {
        return relevantRows;
    }

    /**
     * @return the relevantCols
     */
    protected List<Integer> getRelevantCols() {
        return relevantCols;
    }

    /**
     * @return the externalDataProvider
     */
    protected ExternalDataProvider getExternalDataProvider() {
        return externalDataProvider;
    }       
}
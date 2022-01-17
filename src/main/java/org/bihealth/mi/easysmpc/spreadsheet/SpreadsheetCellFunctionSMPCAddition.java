package org.bihealth.mi.easysmpc.spreadsheet;

import java.math.BigDecimal;

import org.bihealth.mi.easysmpc.resources.Resources;

public class SpreadsheetCellFunctionSMPCAddition extends SpreadsheetCellFunction {

    /**
     * Create a new instance
     * 
     * @param values
     * @param internalDataProvider
     * @param externalDataProvider
     */
    SpreadsheetCellFunctionSMPCAddition(String values, InternalDataProvider internalDataProvider, ExternalDataProvider externalDataProvider) {
        super(values, internalDataProvider, externalDataProvider, true);
        
        // Check not null
        if(getInternalDataProvider() == null) {
            throw new IllegalArgumentException("InternalDataProvider must not be null!");
        }
        
        // Check exactly one cell relevant
        if(getRelevantCells().size() != 1) {
            throw new IllegalArgumentException("SpreadsheetCellFunctionAddSMPC needs exactly one cell as an argument");
        }
        
        // Check cell is calcuable
        if (!getRelevantCells().get(0).isCalculable()) {
            throw new IllegalArgumentException("Relevant cell is not calcuable");
        }
    }

    @Override
    public BigDecimal calculate() {
        // Check
        if(!isCalculable()) {
            return null;
        }
        
        // Return
        return getExternalDataProvider().getDataFor(getRelevantRows().get(0), getRelevantCols().get(0));
    }

    @Override
    public boolean isCalculable() {
        return getExternalDataProvider() != null &&
               getExternalDataProvider().getDataFor(getRelevantRows().get(0),
                                                    getRelevantCols().get(0)) != null; 
    }
    
    @Override
    public String getDisplayedText() {
        return isCalculable() ? calculate().toPlainString() : Resources.getString("PerspectiveCreateSpreedsheet.1");
    }
}
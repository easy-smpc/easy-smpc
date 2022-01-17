package org.bihealth.mi.easysmpc.spreadsheet;

/**
 * An interface to access other cells
 * 
 * @author Felix Wirth
 *
 */
public interface InternalDataProvider {
    
    /**
     * Returns a SpreadsheetCell at a specified row and column
     * 
     * @param row
     * @param column
     * @return
     */
    public SpreadsheetCell getCellAt(int row, int column);

}

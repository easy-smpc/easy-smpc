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
package org.bihealth.mi.easysmpc.dataimport;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.bihealth.mi.easysmpc.resources.Resources;

/**
 * Reads Excel content
 * 
 * @author Felix Wirth
 * @author Fabian Prasser
 */
public class ImportExcel extends ImportFile {

    /**
     * Creates a new instance with the default number of expected row/columns
     * 
     * @param file
     * @throws IOException
     * @throws IllegalArgumentException
     */
    public ImportExcel(File file) throws IOException, IllegalArgumentException {
        this(file, DEFAULT_ROW_COL);
    }

    /**
     * Creates a new instance
     * 
     * @param file
     * @param expectedRowCol - Expected number of columns or rows
     * @throws IOException
     * @throws IllegalArgumentException
     */
    public ImportExcel(File file, int expectedRowCol) throws IOException, IllegalArgumentException {
        super(file, expectedRowCol);
    }

    /**
     * Extracts the data in an excel cell as a string
     * 
     * @param cell
     */
    private String getValue(Cell cell, boolean originalCellType) {
        if (cell != null) {
            switch (originalCellType ? cell.getCellType() : cell.getCachedFormulaResultType()) {
            case NUMERIC:
                double number = cell.getNumericCellValue();                
                // Return integer if no decimal part
                return number == Math.floor(number) ? String.valueOf((int) number) : String.valueOf(number);
            case STRING:
                return cell.getStringCellValue();
            case BLANK:
                return "";
            case _NONE:
                return "";
            case FORMULA:
                return getValue(cell, false);
            default:
                return "";
            }
        } else return "";
    }
    
    @Override
    protected String[][] load() throws IOException {
        
        // Prepare
        Sheet sheet;
        List<List<String>> rows = new ArrayList<>();
        
        // Load Excel sheet
        try {
            sheet = WorkbookFactory.create(getFile(), "", true).getSheetAt(0);
        } catch (EncryptedDocumentException | IOException e) {
            throw new IOException(e);
        }
        
        // Prepare bounds
        int numRows = Math.min(sheet.getLastRowNum() + 1, Resources.MAX_COUNT_ROWS);

        // Iterate over cell
        for (int indexRow = 0; indexRow < numRows; indexRow++) {
            
            // Ignore empty rows
            Row _row = sheet.getRow(indexRow);
            if (_row != null) {
                
                // Construct row
                List<String> row = new ArrayList<>();
                
                // Iterate over columns
                int numColumns = Math.min(_row.getLastCellNum(), Resources.MAX_COUNT_COLUMNS);
                for (int indexCol = 0; indexCol < numColumns; indexCol++) {
                    
                    // Get cell
                    Cell cell = sheet.getRow(indexRow).getCell(indexCol);

                    // Check if cell is not empty
                    boolean added = false;
                    if (cell != null && cell.getCellType() != CellType.BLANK) {
                        
                        // Extract content
                        String content = getValue(cell, true).trim();
                        
                        // Check for empty content
                        if (!content.isEmpty()) {
                            
                            // Add
                            row.add(content);
                            added = true;
                        }
                    }
                    
                    // Add null, if nothing added
                    if (!added) {
                        row.add(null);
                    }
                }
                rows.add(row);
            }
        }
        
        // Close
        sheet.getWorkbook().close();
        
        // Done
        return pack(rows);
    }
}

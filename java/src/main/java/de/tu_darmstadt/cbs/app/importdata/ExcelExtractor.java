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
package de.tu_darmstadt.cbs.app.importdata;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import de.tu_darmstadt.cbs.app.resources.Resources;

/**
 * Class to obtain data if exactly two columns or lines can be found in an Excel file
 * 
 * @author Felix Wirth
 *
 */
public class ExcelExtractor extends Extractor {
    /** The excel sheet */
    private Sheet               sheet;
    /** List of non-empty rows in sheet */
    private List<Integer>       listRows                 = new ArrayList<>();
    /** List of non-empty columns in sheet */
    private List<Integer>       listColumns              = new ArrayList<>();
      
    /**
     * Return the extracted data
     * 
     * @return the data
     */
    public Map<String, String> getExtractedData() throws IllegalArgumentException {
        return extractedData;
    }

    /**
     * Creates a new instance
     * 
     * @param  file
     * @throws IOException 
     * @throws EncryptedDocumentException 
     */
    public ExcelExtractor(File file) throws EncryptedDocumentException, IOException {
        Workbook workbook = WorkbookFactory.create(file, "", true);
        this.sheet = workbook.getSheetAt(0);        
        extractData();
    }
    
    /** 
     * Extracts data from the sheet
     * 
     * @return the data
     */
    private void extractData() throws IllegalArgumentException {
        if (listRows.size() == 0 || listColumns.size() == 0) {
            determineFilledRowsColumns();
        }
        int rowDistancePermanent, colDistancePermanent, rowDistanceTemp, colDistanceTemp;
        boolean columnsOriented;
        // Check orientation
        if (listColumns.size() == EXACT_ROW_COLUMNS_LENGTH) {
            rowDistancePermanent = 1;
            colDistancePermanent = 0;      
            rowDistanceTemp = 0;
            colDistanceTemp = listColumns.get(1)-listColumns.get(0);
            columnsOriented = true;
        } else {
            rowDistancePermanent = 0;
            colDistancePermanent = 1;
            rowDistanceTemp = listRows.get(1)-listRows.get(0);
            colDistanceTemp = 0;
            columnsOriented = false;
        }
        // Read data
        int row = listRows.get(0);
        int col = listColumns.get(0);
        while ((columnsOriented && row <= listRows.get(listRows.size() - 1)) ||
               (!columnsOriented && col <= listColumns.get(listColumns.size() - 1))) {
                extractedData.put(extractExcelCellContent(sheet.getRow(row).getCell(col),true),
                                                  extractExcelCellContent(sheet.getRow(row + rowDistanceTemp).getCell(col + colDistanceTemp),true));              
            row = row + rowDistancePermanent;
            col = col + colDistancePermanent;
        }        
    }
    
    /** 
     * Determines filled/non empty rows and columns in sheet
     * 
     * @param sheet
     */
    private void determineFilledRowsColumns() throws IllegalArgumentException {
        for (int row = 0; row < Resources.MAX_COUNT_ROWS_EXCEL; row++) {
            boolean rowHasContent = false;
            if (sheet.getRow(row) != null) {
                for (int column = 0; column < Resources.MAX_COUNT_COLUMN_EXCEL; column++) {
                    if (sheet.getRow(row).getCell(column) != null &&
                        sheet.getRow(row).getCell(column).getCellType() != CellType.BLANK) {
                        rowHasContent = true;
                        if (!listColumns.contains(column)) listColumns.add(column);
                    }
                }
                if (rowHasContent) listRows.add(row);
            }
        }        
        // Throw error, if if more then two columns or rows 
        if (listRows.size() != EXACT_ROW_COLUMNS_LENGTH && listColumns.size() != EXACT_ROW_COLUMNS_LENGTH) {
            throw new IllegalArgumentException(String.format("", EXACT_ROW_COLUMNS_LENGTH));
        }
    }
    
    /**
     * Extracts the data in an excel cell as a string
     * 
     * @param cell
     */
    private String extractExcelCellContent(Cell cell, boolean originalCellType) {
        if (cell != null) {
            switch (originalCellType ? cell.getCellType() : cell.getCachedFormulaResultType()) {
            case NUMERIC:
                double number = cell.getNumericCellValue();
                // return integer if no decimal part
                return number == Math.floor(number) ? String.valueOf((int) number)
                        : String.valueOf(number);
            case STRING:
                return cell.getStringCellValue();
            case BLANK:
                return "";
            case _NONE:
                return "";
            case FORMULA:
                return extractExcelCellContent(cell, false);
            default:
                return "";
            }
        }
        else return "";
    }
}

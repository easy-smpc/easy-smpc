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

import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import de.tu_darmstadt.cbs.app.resources.Resources;

/**
 * Reads Excel content
 * 
 * @author Felix Wirth
 */
public class ExcelExtractor extends Extractor {

    /**
     * Creates a new instance
     * 
     * @param file
     * @throws IOException
     * @throws IllegalArgumentException
     */
    public ExcelExtractor(File file) throws IOException, IllegalArgumentException {
        super(file);
    }

    @Override
    protected String[][] loadRawData() throws IOException {
        String[][] rawData = new String[Resources.MAX_COUNT_ROWS][Resources.MAX_COUNT_COLUMNS];
        Sheet sheet;
        try {
            sheet = WorkbookFactory.create(getFile(), "", true).getSheetAt(0);
        } catch (EncryptedDocumentException | IOException e) {
            throw new IOException(e);
        }

        // Iterate over cell
        for (int indexRow = 0; indexRow < Resources.MAX_COUNT_ROWS; indexRow++) {
            // Check entire row is not null
            if (sheet.getRow(indexRow) != null) {
                for (int indexCol = 0; indexCol < Resources.MAX_COUNT_COLUMNS; indexCol++) {
                    Cell cell = sheet.getRow(indexRow).getCell(indexCol);

                    // Check if cell is not empty
                    if (cell != null && cell.getCellType() != CellType.BLANK &&
                        !extractExcelCellContent(cell, true).trim().isEmpty()) {
                        rawData[indexRow][indexCol] = extractExcelCellContent(cell, true);
                    } else {
                        rawData[indexRow][indexCol] = null;
                    }
                }
            } else {
                rawData[indexRow] = null;
            }
        }
        return rawData;
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
                // Return integer if no decimal part
                return number == Math.floor(number) ? String.valueOf((int) number) : String.valueOf(number);
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
        } else return "";
    }
}

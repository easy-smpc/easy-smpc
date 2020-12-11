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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.EncryptedDocumentException;

import de.tu_darmstadt.cbs.app.resources.Resources;

/**
 * Extracts data from a two dimensional arra which can be filled by different files formats
 * 
 * @author Felix Wirth
 */

public abstract class Extractor {
    /** Exact number rows or columns */
    protected static final int    EXACT_ROW_COLUMNS_LENGTH = 2;
    /** List of extracted data */
    protected Map<String, String> extractedData            = new LinkedHashMap<String, String>();
    /** List of non-empty rows in sheet */
    private List<Integer>         listRows                 = new ArrayList<>();
    /** List of non-empty columns in sheet */
    private List<Integer>         listColumns              = new ArrayList<>();
    /** Raw data */
    protected String[][]          dataRaw = new String[Resources.MAX_COUNT_ROWS][Resources.MAX_COUNT_COLUMNS];
    /** File of data origin */
    protected File                file;    
    
    /**
     * Creates a new instance
     * 
     * @param file
     * @throws IOException
     * @throws EncryptedDocumentException
     */
    protected Extractor(File file) throws IOException, IllegalArgumentException {
        this.file = file;
        loadDataRaw();
        determineDataProperties();
        extractData();
    }

    /**
     * Returns the extracted data
     * 
     * @return the data
     */
    public Map<String, String> getExtractedData() throws IllegalArgumentException {
        return extractedData;
    }    
        
    /**
     * Prepares the data extraction
     */
    protected void determineDataProperties() throws IllegalArgumentException {
        // Identify non-empty rows and columns
        for (int indexRow = 0; indexRow <= dataRaw.length - 1; indexRow++) {
            boolean rowHasContent = false;
            if (dataRaw[indexRow] != null) {
                for (int indexCol = 0; indexCol <= dataRaw[indexRow].length - 1; indexCol++) {
                    if (dataRaw[indexRow][indexCol] != null &&
                        !dataRaw[indexRow][indexCol].trim().isEmpty()) {
                        rowHasContent = true;
                        if (!listColumns.contains(indexCol)) {
                            listColumns.add(indexCol);
                        }
                    }
                }
                if (rowHasContent && !listRows.contains(indexRow)) {
                    listRows.add(indexRow);
                }
            }
        }
        // Throw error, if not expected columns or rows length
        if (listRows.size() != EXACT_ROW_COLUMNS_LENGTH &&
            listColumns.size() != EXACT_ROW_COLUMNS_LENGTH) {
            throw new IllegalArgumentException(String.format("", EXACT_ROW_COLUMNS_LENGTH));
        }
    }
    
    /**
     * Extract the data
     */
    protected void extractData() throws IllegalArgumentException {

        int rowDistancePermanent, colDistancePermanent, rowDistanceTemp, colDistanceTemp;
        boolean columnsOriented;
        // Check orientation
        if (listColumns.size() == EXACT_ROW_COLUMNS_LENGTH) {
            rowDistancePermanent = 1;
            colDistancePermanent = 0;
            rowDistanceTemp = 0;
            colDistanceTemp = listColumns.get(1) - listColumns.get(0);
            columnsOriented = true;
        } else {
            rowDistancePermanent = 0;
            colDistancePermanent = 1;
            rowDistanceTemp = listRows.get(1) - listRows.get(0);
            colDistanceTemp = 0;
            columnsOriented = false;
        }
        
        // Extract data from filled rows or columns
        int row = listRows.get(0);
        int col = listColumns.get(0);
        while ((columnsOriented && row <= listRows.get(listRows.size() - 1)) ||
               (!columnsOriented && col <= listColumns.get(listColumns.size() - 1))) {
            
            // Assume first row/columns has bin name, second has bin value
            extractedData.put(dataRaw[row][col],
                              dataRaw[row + rowDistanceTemp][col + colDistanceTemp]);
            row = row + rowDistancePermanent;
            col = col + colDistancePermanent;
        }
    }
    
    /**
     * Loads the data in a common format of a two dimensional array
     */
    protected abstract void loadDataRaw() throws IOException;
}
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
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.poi.EncryptedDocumentException;
import org.bihealth.mi.easysmpc.resources.Resources;

/**
 * Extracts data from a two dimensional array which can be filled by different file formats
 * 
 * @author Felix Wirth
 * @author Fabian Prasser
 */
public abstract class ImportFile {

    /**
     * Creates a new extractor for a given file with the default of more row oriented data with more than one column
     * 
     * @param file
     * @return
     * @throws IOException 
     * @throws IllegalArgumentException 
     */
    public static ImportFile forFile(File file) throws IllegalArgumentException, IOException {        
        return forFile(file, true, false, false);
    }
    
    /**
     * Creates a new extractor for a given file
     * 
     * @param file
     * @param rowOriented - is data row or column oriented?
     * @param oneRowCol - Is the result supposed to be two or one column. If this parameter is set all data is merge together, if it is unset the last column will be handled separately
     * @param hasHeader - skip first line since it contains the header  
     * @return
     * @throws IllegalArgumentException
     * @throws IOException
     */
    public static ImportFile forFile(File file,
                                     boolean rowOriented,
                                     boolean oneRowCol,
                                     boolean hasHeader) throws IllegalArgumentException,
                                                        IOException {
        
        // Choose correct extractor
        if (file.getName().endsWith(Resources.FILE_ENDING_EXCEL_XLS) || file.getName().endsWith(Resources.FILE_ENDING_EXCEL_XLSX)) {
            return new ImportExcel(file, rowOriented, oneRowCol, hasHeader);
        }
        else {
            return new ImportCSV(file, rowOriented, oneRowCol, hasHeader);
        }   
    }

    /** File of data origin */
    private File file;
    /** Is data row or column oriented? */
    private boolean rowOriented;
    /** Is data only one or multiple rows/columns? */
    private boolean oneRowCol;
    /** Has data a header line? */
    private boolean hasHeader;

    /**
     * Creates a new instance
     * 
     * @param file
     * @param rowOriented - is data row or column oriented?
     * @param oneRowCol - Is the result supposed to be two or one column. If this parameter is set all data is merge together, if it is unset the last column will be handled separately
     * @param hasHeader - skip first line since it contains the header 
     * @throws IOException
     * @throws EncryptedDocumentException
     */
    protected ImportFile(File file,
                         boolean rowOriented,
                         boolean oneRowCol,
                         boolean hasHeader) throws IOException, IllegalArgumentException {
        this.file = file;
        this.rowOriented = rowOriented;
        this.oneRowCol = oneRowCol;
        this.hasHeader = hasHeader;
    }

    /**
     * Returns the data as key-value pairs
     * 
     * @return A list with either all columns per row merged as keys and values as null or all columns but the last merged as keys and the last column as values (see <code>oneRowCol<code> of the constructor)
     * @throws IOException 
     */
    public Map<String, String> getData() throws IllegalArgumentException, IOException {
        return extract(clean(load()));
    }
    
    /**
     * Remove all empty rows and columns
     * 
     * @param data
     * @return the data without empty lines and columns
     */
    private String[][] clean(String[][] data) {

        // Prepare
        int columns = -1;
        List<List<String>> rows = new ArrayList<>();
        
        // Transpose if necessary
        if(!this.rowOriented) {
            data = transposeMatrix(data);
        }

        // Remove empty rows
        for (String[] row : data) {

            // Check
            if (isNotEmpty(row)) {

                // Add non-empty row
                rows.add(new ArrayList<String>(Arrays.asList(row)));

                // Perform a sanity check
                columns = columns == -1 ? row.length : columns;
                if (columns != row.length) {
                    throw new IllegalArgumentException("Array must be rectangular");
                }
            }
        }

        // Remove empty columns
        for (int column = 0; column < columns; column++) {

            // First determine whether the column is empty
            boolean isEmpty = true;
            for (List<String> row : rows) {
                if (isNotEmpty(row.get(column))) {
                    isEmpty = false;
                    break;
                }
            }

            // If column is empty, remove from data
            if (isEmpty) {
                for (List<String> row : rows) {
                    row.remove(column);
                }
                columns--;
                column--;
            }
        }
        
        // Pack
        String[][] result = pack(rows);
        
        // Remove header line
        if (hasHeader) {
            result = ArrayUtils.removeElement(result, result [0]);
        }
        
        // Done
        return result;
    }

    /**
     * Extracts the data out of a stripped array
     * 
     * @param strippedData
     * @return extracted data
     */
    private Map<String, String> extract(String[][] strippedData) {

        // Prepare
        Map<String, String> result = new LinkedHashMap<String, String>();

        if (oneRowCol) {
            
            // Loop over data
            for (int indexRow = 0; indexRow < strippedData.length; indexRow++) {

                // Merge all columns into one column
                String concat = "";
                for (int indexCol = 0; indexCol < strippedData[indexRow].length; indexCol++) {
                    concat = concat + (indexCol != 0 ? Resources.AGGREGATION_DELIMITER : "") + strippedData[indexRow][indexCol];
                }

                // Put
                result.put(concat, null);
            }
        } else {

            // Loop over data
            for (int indexRow = 0; indexRow < strippedData.length; indexRow++) {

                // Merge all columns but the last into one column
                String concat = "";
                for (int indexCol = 0; indexCol < strippedData[indexRow].length - 1; indexCol++) {
                    concat = concat + (indexCol != 0 ? Resources.AGGREGATION_DELIMITER : "") + strippedData[indexRow][indexCol];
                }

                // Put
                result.put(concat, strippedData[indexRow][strippedData[indexRow].length - 1]);
            }
        }

        // Done
        return result;
    }

    /**
     * Checks whether a string is empty
     * 
     * @param o
     * @return
     */
    private boolean isNotEmpty(String o) {
        return o != null && !o.trim().isEmpty();
    }

    /**
     * Checks whether an array is empty
     * 
     * @param array
     * @return
     */
    private boolean isNotEmpty(String[] array) {

        // Check
        if (array == null || array.length == 0) { return false; }

        // Check elements
        boolean empty = true;
        for (String o : array) {
            if (isNotEmpty(o)) {
                empty = false;
                break;
            }
        }
        // Done
        return !empty;
    }

    /**
     * @return the file
     */
    protected File getFile() {
        return file;
    }
    
    /**
     * Loads the data in a row-oriented format of a two dimensional array
     * @return two dimensional array of data
     */
    protected abstract String[][] load() throws IOException;

    /**
     * Convert list of lists to array. Makes sure that the result is rectangular.
     * 
     * @param list of list of strings 
     * @return Array [][]
     */
    protected String[][] pack(List<List<String>> rows) {
        
        // Calculate number of columns
        int columns = 0;
        for (List<String> row : rows) {
            columns = Math.max(row.size(), columns);
        }        
        
        // Convert list of lists to array
        String[][] result = new String[rows.size()][];
        int index = 0;
        for (List<String> row : rows) {
            result[index++] = row.toArray(new String[columns]);
        }       
        
        // Done
        return result;
    }

    /**
     * Transpose a matrix
     * 
     * @param array
     * @return
     */
    protected String[][] transposeMatrix(String[][] array) {
        // Check
        if (array == null || array.length == 0) { return array; }

        // Init
        String[][] result = new String[array[0].length][array.length];

        for (int i = 0; i < array.length; i++) {
            for (int j = 0; j < array[0].length; j++) {
                result[j][i] = array[i][j];
            }
        }

        // Return
        return result;
    }
}
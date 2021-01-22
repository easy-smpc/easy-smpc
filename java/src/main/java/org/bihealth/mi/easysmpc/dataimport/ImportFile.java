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
     * Creates a new extractor for a given file
     * 
     * @param file
     * @return
     * @throws IOException 
     * @throws IllegalArgumentException 
     */
    public static ImportFile forFile(File file) throws IllegalArgumentException, IOException {
        
        // Choose correct extractor
        if (file.getName().contains(Resources.FILE_ENDING_EXCEL_XLS) || file.getName().contains(Resources.FILE_ENDING_EXCEL_XLS)) {
            return new ImportExcel(file);
        }
        else {
            return new ImportCSV(file);
        }   
    }

    /** File of data origin */
    private File          file;

    /**
     * Creates a new instance
     * 
     * @param file
     * @throws IOException
     * @throws EncryptedDocumentException
     */
    protected ImportFile(File file) throws IOException, IllegalArgumentException {
        this.file = file;    
    }

    /**
     * Returns the data as key-value pairs
     * 
     * @return the data
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
            }
        }
        
        // Final sanity check
        String[][] result = pack(rows);
        if (result.length != 2 && result[0] != null && result[0].length != 2) {
            throw new IllegalArgumentException("Array must either have exact two rows or two columns");
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
    private Map<String,String> extract(String[][] strippedData) {
        
        // Prepare
        Map<String, String> result = new LinkedHashMap<String, String>();

        // Two columns
        if (strippedData.length != 2) {
            for (int indexRow = 0; indexRow < strippedData.length; indexRow++) {
                result.put(strippedData[indexRow][0], strippedData[indexRow][1]);
            }
       
        // Two rows
        } else {
            for (int indexColumn = 0; indexColumn < strippedData[0].length; indexColumn++) {
                result.put(strippedData[0][indexColumn], strippedData[1][indexColumn]);
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
}
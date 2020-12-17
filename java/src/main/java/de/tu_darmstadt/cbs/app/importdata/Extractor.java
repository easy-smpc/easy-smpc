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
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.poi.EncryptedDocumentException;

import de.tu_darmstadt.cbs.app.resources.Resources;

/**
 * Extracts data from a two dimensional array which can be filled by different file formats
 * 
 * @author Felix Wirth
 */

public abstract class Extractor {
    
    /** File of data origin */
    private File          file;

    /**
     * Creates a new instance
     * 
     * @param file
     * @throws IOException
     * @throws EncryptedDocumentException
     */
    protected Extractor(File file) throws IOException, IllegalArgumentException {
        this.file = file;    
    }

    /**
     * @return the file
     */
    protected File getFile() {
        return file;
    }

    /**
     * Returns the extracted data
     * 
     * @return the data
     * @throws IOException 
     */
    public Map<String, String> getExtractedData() throws IllegalArgumentException, IOException {
        return extractData(stripRawData(loadRawData()));
    }
    
    /**
     * Remove all empty rows and columns
     * 
     * @param rawData
     * @return the data without empty lines and columns
     */
    private String[][] stripRawData(String[][] rawData) {      
        String[][] strippedData = null;
        int indexRowStrippedData = 0;
        for (int indexRow = 0; indexRow <= rawData.length - 1; indexRow++) {
            boolean rowNotEmpty = false;
            if (rawData[indexRow] != null) {
                int indexColumnStrippedData = 0;
                for (int indexColumn = 0; indexColumn <= rawData[indexRow].length -
                                                         1; indexColumn++) {
                    if (rawData[indexRow][indexColumn] != null &&
                        !rawData[indexRow][indexColumn].trim().isEmpty()) {
                        strippedData = setFieldInArray(strippedData,
                                                       indexRowStrippedData,
                                                       indexColumnStrippedData,
                                                       rawData[indexRow][indexColumn]);
                        rowNotEmpty = true;
                        indexColumnStrippedData++;
                    }
                }
                if (rowNotEmpty) {
                    indexRowStrippedData++;
                }
            }
        }
        return strippedData;
    }
    

    /**
     * Sets a value in an array while extending the array if necessary
     * 
     * @param old array
     * @param row of value
     * @param column of value
     * @param value
     * @return the new array
     */
    private String[][] setFieldInArray(String[][] oldArray, int row, int column, String value) {
        String[][] newArray;
        
        // Initialize if null
        if (oldArray == null) {
            oldArray = new String[row + 1][column + 1];
        }
        
        // If old array is too small, create new one and copy values
        if (oldArray.length <= row || oldArray[0].length <= column) {
            newArray = new String[Math.max(row + 1, oldArray.length)][Math.max(column + 1,
                                                                               oldArray[0].length)];
            for (int indexRow = 0; indexRow < oldArray.length; indexRow++) {
                for (int indexColums = 0; indexColums < oldArray[0].length; indexColums++) {
                    newArray[indexRow][indexColums] = oldArray[indexRow][indexColums];
                }
            }
        } else {
            newArray = oldArray;
        }
        
        // Set new value
        newArray[row][column] = value;
        return newArray;
    }

    /**
     * Extracts the data out of a stripped array
     * 
     * @param strippedData
     * @return extracted data
     * @throws IllegalArgumentException
     */
    private Map<String,String> extractData(String[][] strippedData) throws IllegalArgumentException {
        Map<String, String> extractedData            = new LinkedHashMap<String, String>();
        
        // Throw error, if not expected columns or rows length
        if (strippedData.length != Resources.EXACT_ROW_COLUMNS_LENGTH &&
            strippedData[0].length != Resources.EXACT_ROW_COLUMNS_LENGTH) {
            throw new IllegalArgumentException(Resources.getString("PerspectiveCreate.LoadDataError"));
        }
        
        // Transpose if rows oriented
        if (strippedData[0].length > Resources.EXACT_ROW_COLUMNS_LENGTH) {
            String[][] untransposed = strippedData;
            strippedData = new String[untransposed[0].length][untransposed.length];
            for (int indexRow = 0; indexRow < untransposed.length; indexRow++) {
                for (int indexColumn = 0; indexColumn < untransposed[0].length; indexColumn++) {
                    strippedData[indexColumn][indexRow] = untransposed[indexRow][indexColumn];
                }
            }
        }
           
        // Extract data from filled rows
        for (int indexRow = 0; indexRow < strippedData.length; indexRow++) {
            // Assume first column contains bin name, second has bin value
            extractedData.put(strippedData[indexRow][0], strippedData[indexRow][1]);
        }
        return extractedData;
    }
    
    /**
     * Loads the data in a common format of a two dimensional array
     * @return two dimensional array of data
     */
    protected abstract String[][] loadRawData() throws IOException;

    /**
     * Creates a new extractor for a given file
     * 
     * @param file
     * @return
     * @throws IOException 
     * @throws IllegalArgumentException 
     */
    public static Extractor forFile(File file) throws IllegalArgumentException, IOException {
        
        // Choose correct extractor
        if (file.getName().contains(Resources.FILE_ENDING_EXCEL_XLS) || file.getName().contains(Resources.FILE_ENDING_EXCEL_XLS)) {
            return new ExcelExtractor(file);
        }
        else {
            return new CSVExtractor(file);
        }   
    }
}
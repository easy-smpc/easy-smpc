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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.bihealth.mi.easysmpc.resources.Resources;

import com.carrotsearch.hppc.CharIntOpenHashMap;
import com.carrotsearch.hppc.IntIntOpenHashMap;

/**
 * Reads CSV content
 * 
 * @author Felix Wirth
 * @author Fabian Prasser
 */
public class ImportCSV extends ImportFile {
    
    /**
     * Creates a new instance with default parameters
     * 
     * @param file
     * @throws IOException
     * @throws IllegalArgumentException
     */
    protected ImportCSV(File file) throws IOException, IllegalArgumentException {
        this(file, true, false, false);
    }
    
    /**
     * Creates a new instance
     * 
     * @param file
     * @param rowOriented - is data row or column oriented?
     * @param oneRowCol - Is the result supposed to be two or one column. If this parameter is set all data is merge together, if it is unset the last column will be handled separately  
     * @param hasHeader - skip first line since it contains the header     
     * @throws IOException
     * @throws IllegalArgumentException
     */
    protected ImportCSV(File file, boolean rowOriented, boolean oneRowCol, boolean hasHeader) throws IOException, IllegalArgumentException {
        super(file, rowOriented, oneRowCol, hasHeader);
    }
    

    /**
     * Detects a delimiter.
     * 
     * @param file
     * @throws IOException
     */
    private char getDelimiter() throws IOException {
        
        // Prepare
        char delimiter = Resources.DELIMITERS[0];
        BufferedReader r = null;
        final IntIntOpenHashMap map = new IntIntOpenHashMap();
        final CharIntOpenHashMap delimitors = new CharIntOpenHashMap();
        
        try {
        r = new BufferedReader(new InputStreamReader(new FileInputStream(getFile()), Charset.defaultCharset()));        
        for (int i=0; i<Resources.DELIMITERS.length; i++) {
            delimitors.put(Resources.DELIMITERS[i], i);
        }
        int countLines = 0;
        int countChars = 0;

        // Iterate over data
        String line = r.readLine();
        outer: while ((countLines < Resources.PREVIEW_MAX_LINES) && (line != null)) {

            // Iterate over line character by character
            final char[] a = line.toCharArray();
            for (final char c : a) {
                if (delimitors.containsKey(c)) {
                    map.putOrAdd(delimitors.get(c), 0, 1);
                }
                countChars++;
                if (countChars > Resources.DETECT_MAX_CHARS) {
                    break outer;
                }
            }
            line = r.readLine();
            countLines++;
        }
        r.close();
        } finally {
            if (r != null) {
                r.close();
            }
        }
        // If nothing found, return default
        if (map.isEmpty()) {
            return delimiter;
        }

        // Check which separator was used the most
        int max = Integer.MIN_VALUE;
        final int [] keys = map.keys;
        final int [] values = map.values;
        final boolean [] allocated = map.allocated;
        for (int i = 0; i < allocated.length; i++) {
            if (allocated[i] && values[i] > max) {
                max = values[i];
                delimiter = Resources.DELIMITERS[keys[i]];
            }
        }
        
        // Done
        return delimiter;
    }
    
    @Override
    protected String[][] load() throws IOException {     
        
        // Open
        Iterable<CSVRecord> records = CSVFormat.DEFAULT.withDelimiter(getDelimiter()).parse(new FileReader(getFile()));
        List<List<String>> rows = new ArrayList<>();
        
        // Load per row
        int indexRow = 0;
        for (CSVRecord record : records) {
            
            // Load cell values in columns
            int indexColumn = 0;
            List<String> row = new ArrayList<>();
            for (String value : record) {
                
                value = value == null ? "" : value;
                value = value.trim();
                row.add(value.isEmpty() ? null : value);
                
                // Break if maximum reached
                if (++indexColumn == Resources.MAX_COUNT_COLUMNS) {
                    break;
                }
            }
            
            // Add row
            rows.add(row);
            
            // Break if maximum reached
            if (++indexRow == Resources.MAX_COUNT_ROWS) {
                break;
            }
        }
        
        // Done
        return pack(rows);
    }   
}
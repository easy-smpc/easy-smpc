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

import com.carrotsearch.hppc.CharIntOpenHashMap;
import com.carrotsearch.hppc.IntIntOpenHashMap;

import de.tu_darmstadt.cbs.app.resources.Resources;

/**
 * Reads CSV content
 * 
 * @author Felix Wirth
 */
public class CSVExtractor extends Extractor {
    
    /**
     * Creates a new instance
     * 
     * @param file
     * @throws IOException
     * @throws IllegalArgumentException
     */
    public CSVExtractor(File file) throws IOException, IllegalArgumentException {
        super(file);
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
    protected String[][] loadRawData() throws IOException {     
        Iterable<CSVRecord> records = CSVFormat.DEFAULT.withDelimiter(getDelimiter())
                                                       .parse(new FileReader(getFile()));
        List<List<String>> rows = new ArrayList<>();
        
        int indexRow = 0;
        for (CSVRecord recordRow : records) {
            int indexColumn = 0;
            List<String> column = new ArrayList<>();
            for (String recordField : recordRow) {
                column.add(recordField);
                // Break if maximum reached
                indexColumn++;
                if (indexColumn == Resources.MAX_COUNT_COLUMNS) {
                    break;
                }
            }
            rows.add(column);
            // Break if maximum reached
            indexRow++;
            if (indexRow == Resources.MAX_COUNT_ROWS) {
                break;
            }
        }
        
        // Done
        return rowsListToArray(rows);
    }   
}
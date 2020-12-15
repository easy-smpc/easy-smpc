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

/**
 * @author Fabian Prasser
 *
 */
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

import com.carrotsearch.hppc.CharIntOpenHashMap;
import com.carrotsearch.hppc.IntIntOpenHashMap;

import de.tu_darmstadt.cbs.app.resources.Resources;

public class CSVSyntaxDetector {

    /**
     * Detects a delimiter.
     * 
     * @param file
     * @throws IOException
     */
    public static char getDelimiter(File file) throws IOException {
        
        // Prepare
        char delimiter = Resources.DELIMITERS[0];
        BufferedReader r = null;
        final IntIntOpenHashMap map = new IntIntOpenHashMap();
        final CharIntOpenHashMap delimitors = new CharIntOpenHashMap();
        
        try {
        r = new BufferedReader(new InputStreamReader(new FileInputStream(file), Charset.defaultCharset()));        
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

    
    /**
     * Detects a line break.
     * 
     * @param file
     * @throws IOException
     */
    public static char[] getLinebreak(File file) throws IOException {
        
        // Prepare
        BufferedReader r = null;
        final char[] buffer = new char[Resources.DETECT_MAX_CHARS];
        int read = 0;
        try {
            r = new BufferedReader(new InputStreamReader(new FileInputStream(file), Charset.defaultCharset()));
            read = r.read(buffer);
        } finally {
            if (r != null) {
                r.close();
            }
        }

        // Detect
        if (read > 0) {
            for (int i = 0; i < read; i++) {
                char current = buffer[i];
                if (current == '\r') {
                    if (i < buffer.length - 1 && buffer[i + 1] == '\n') { // Windows
                        return Resources.LINEBREAKS[1];
                    } else { // Mac OS
                        return Resources.LINEBREAKS[2];
                    }
                }
                if (current == '\n') { // Unix
                    return Resources.LINEBREAKS[0];
                }
            }
        }
        
        // If nothing detected
        return Resources.LINEBREAKS[1];
    }
}
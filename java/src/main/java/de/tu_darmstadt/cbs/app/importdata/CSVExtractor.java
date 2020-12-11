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
import java.io.FileReader;
import java.io.IOException;

import de.tu_darmstadt.cbs.app.resources.Resources;

/**
 * Class to obtain data if exactly two columns or lines can be found in an Excel
 * file
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

    @Override
    protected void loadDataRaw() throws IOException {
        String delimiter = "";
        try {
         delimiter = String.valueOf(CSVSyntaxDetector.getDelimiter(file));
        }
        catch(IOException e) {
            e.printStackTrace();
        }
        char lineBreak = CSVSyntaxDetector.getDelimiter(file);
        int indexRow = 0;
        BufferedReader reader = new BufferedReader(new FileReader(file));
        String line;
        
        // Iterate over each line
        while ((line = reader.readLine()) != null && indexRow < Resources.MAX_COUNT_ROWS) {
            // Replace several delimiters next to each other with one 
            line = line.replaceAll(delimiter + "{2,}", delimiter); 
            String[] splittedString = line.split(delimiter);
            int indexCol = 0;
            
         // Iterate over each column
            while (indexCol < splittedString.length && indexCol < Resources.MAX_COUNT_COLUMNS) {
                if (splittedString[indexCol] != null &&
                    !splittedString[indexCol].trim().isEmpty()) {
                    // Set data and replace line break with a simple space
                    dataRaw[indexRow][indexCol] = splittedString[indexCol].replace(lineBreak, ' ');
                } else {
                    dataRaw[indexRow][indexCol] = null;
                }
                indexCol++;
            }
            indexRow++;
        }
        reader.close();     
    }   
}

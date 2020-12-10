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
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;

/**
 * Class to obtain data if exactly two columns or lines can be found in an Excel file
 * 
 * @author Felix Wirth
 */
public class CSVExtractor extends Extractor{
    /** file */
    private File file;
   
    /**
     * Creates a new instance
     * 
     * @param  file
     * @throws IOException 
     * @throws FileNotFoundException 
     */
    public CSVExtractor(File file) throws FileNotFoundException, IOException {
        this.file = file;
        extractData();
    }
    
    /**
     * Extracts the data
     * @throws IOException 
     * @throws FileNotFoundException 
     * 
     */
    private void extractData() throws FileNotFoundException, IOException {
        extractedData = new LinkedHashMap<String, String>();
        Iterable<CSVRecord> records = CSVFormat.newFormat(identifyDelimiter())
                                               .parse(new FileReader(file));
        for (CSVRecord record : records) {
            extractedData.put(record.get(0), record.get(1));
        }
    }

    /**
     * Try to identify delimiter in CSV file
     * 
     * @return
     */
    private char identifyDelimiter() {
     // TODO Do an actual implementation
        return ';'; 
    }

    @Override
    public Map<String, String> getExtractedData() throws IllegalArgumentException {
        return extractedData;
    }
}

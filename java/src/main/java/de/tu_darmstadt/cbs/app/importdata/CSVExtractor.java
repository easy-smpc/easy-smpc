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
import java.io.FileReader;
import java.io.IOException;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;

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
        Iterable<CSVRecord> records = CSVFormat.DEFAULT.withDelimiter(CSVSyntaxDetector.getDelimiter(file))
                                                       .parse(new FileReader(file));
        int indexRow = 0;
        for (CSVRecord recordRow : records) {
            int indexColumn = 0;
            for (String recordField : recordRow) {
                dataRaw[indexRow][indexColumn] = recordField;
                indexColumn++;
                if (indexColumn == Resources.MAX_COUNT_COLUMNS) {
                    break;
                }
            }
            indexRow++;
            if (indexRow == Resources.MAX_COUNT_ROWS) {
                break;
            }
        }
    }
}

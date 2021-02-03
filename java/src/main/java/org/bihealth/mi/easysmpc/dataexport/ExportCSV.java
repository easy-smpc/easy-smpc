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
package org.bihealth.mi.easysmpc.dataexport;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.bihealth.mi.easysmpc.resources.Resources;

/**
 * Writes data in CSV-format
 * 
 * @author Felix Wirth
 * @author Fabian Prasser
 */
public class ExportCSV extends ExportFile {
    
    /**
     * Creates a new instance
     * 
     * @param file
     */
    public ExportCSV(File file) {
        super(file);
    }
    
    /**
     * Exports data in CSV-format
     * 
     * @param file
     */
    @Override
    public void exportData(List<List<String>> data) throws IOException {        
       
        // Create file
        CSVPrinter csvPrinter = new CSVPrinter(new BufferedWriter(new FileWriter(getFile())),
                                               CSVFormat.DEFAULT.withHeader(Resources.getString("Export.1"),
                                                                            Resources.getString("Export.2")));

        // Write per row
        for (List<String> dataEntry : data) {
            csvPrinter.printRecord(dataEntry.get(0), dataEntry.get(1));
        }
        
        // Finalize
        csvPrinter.close();
    }
}

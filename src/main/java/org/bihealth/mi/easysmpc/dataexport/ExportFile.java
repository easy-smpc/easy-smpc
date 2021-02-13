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

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.bihealth.mi.easysmpc.resources.Resources;

/**
 * Exports data to a file
 * 
 * @author Felix Wirth
 * @author Fabian Prasser
 */
public abstract class ExportFile {
    
    /**
     * Creates a new Exporter for a given file
     * 
     * @param file
     * @return ExportFile instance suitable for file type
     */
    public static ExportFile toFile(File file) throws IOException {
        
        // Choose correct extractor
        if (file.getName().contains(Resources.FILE_ENDING_EXCEL_XLSX)) {
            return new ExportExcel(file);
        } else {
            return new ExportCSV(file);
        }
    }

    /** File of data origin */
    private File          file;

    /**
     * Creates a new instance
     * 
     * @param file
     */
    protected ExportFile(File file) {
        this.file = file;    
    }
    
    /**
     * @return the file
     */
    protected File getFile() {
        return file;
    }
    
    /**
     * Exports the data
     * @param data 
     * 
     * @throws IOException 
     */
    public abstract void exportData(List<List<String>> data) throws IOException;
}
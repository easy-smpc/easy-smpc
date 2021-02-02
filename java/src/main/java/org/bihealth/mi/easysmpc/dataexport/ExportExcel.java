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
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.bihealth.mi.easysmpc.resources.Resources;

/**
 * Writes data in Excel-format
 * 
 * @author Felix Wirth
 * @author Fabian Prasser
 */
public class ExportExcel extends ExportFile {

    /**
     * Creates a new instance
     * 
     * @param file
     */
    public ExportExcel(File file) {
        super(file);
    }
    
    /**
     * Exports data in Excel-format
     * 
     * @param file
     */
    @Override
    public void exportData(List<List<String>> data) throws IOException {
        // Create work book
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet(Resources.getString("Export.0"));

        // Write header
        Row row = sheet.createRow(0);
        row.createCell(0).setCellValue(Resources.getString("Export.1"));
        row.createCell(1).setCellValue(Resources.getString("Export.2"));
        
        // Write per row        
        int index = 1;
        for (List<String> dataEntry : data) {
            row = sheet.createRow(index);
            row.createCell(0).setCellValue(dataEntry.get(0));
            row.createCell(1).setCellValue(dataEntry.get(1));
            index++;
        }

        // Finalize
        sheet.autoSizeColumn(0);
        sheet.autoSizeColumn(1);
        workbook.write(new FileOutputStream(getFile()));
        workbook.close();
    }
}

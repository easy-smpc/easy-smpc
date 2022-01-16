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
package org.bihealth.mi.easysmpc.spreadsheet;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.swing.event.TableModelEvent;
import javax.swing.table.AbstractTableModel;

/**
 * A data model for the spread sheet table
 * 
 * @author Felix Wirth
 *
 */
public class SpreadsheetTableModel extends AbstractTableModel implements Serializable {
    
    /** SVUID */
    private static final long serialVersionUID = -1113535298514958205L;
    /** Can fields in the table be edited */
    private boolean isEditable;
    /** The actual data */
    private final List<List<SpreadsheetCell>> data = new ArrayList<>();
    
     /** Creates a new instance
     * 
     * @param isEditable
     * @param numRows
     * @param numColumns
     */
    public SpreadsheetTableModel(boolean isEditable, int numRows, int numColumns) {
        // Store
        this.isEditable = isEditable;        
        
        // Init table
        getCell(numRows, numColumns);
        fireTableChanged(new TableModelEvent(this));
    }

    @Override
    public int getRowCount() {
        return data.size();
    }

    @Override
    public int getColumnCount() {
        if (data.size() > 0) {
            return data.get(0).size();
        }
        return 0;
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return SpreadsheetCell.getClassStatic();
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        // First column only has names
        if (columnIndex == 0) { return false; }
        
        // Return
        return isEditable;
    }

    @Override
    public SpreadsheetCell getValueAt(int rowIndex, int columnIndex) {
        return getCell(rowIndex, columnIndex);
    }

    @Override
    public void setValueAt(Object cell, int rowIndex, int columnIndex) {
        // Create position if not created in data so far
        getCell(rowIndex, columnIndex);
        
        // Set data
        data.get(rowIndex).set(columnIndex, cell != null ? (SpreadsheetCell) cell : null);
        fireTableChanged(new TableModelEvent(this, rowIndex, columnIndex));
    }
    

    private SpreadsheetCell getCell(int rowIndex, int columnIndex) {
        // If row does not exist create
        if (data.size() - 1  < rowIndex) {
            for (int indexRows = data.size(); indexRows <= rowIndex; indexRows++) {
                data.add(new ArrayList<SpreadsheetCell>());
            }
        }
        
        // If column does not exist extend cols quadratically
        if (data.get(0).size() - 1  < columnIndex) {
            int colStart =  data.get(0).size();
            for (int indexRows = 0; indexRows < data.size(); indexRows++) {
                for (int indexCols = colStart; indexCols <= columnIndex; indexCols++) {
                    data.get(indexRows).add(null);
                }
            }
        }
        
        // Return
        return data.get(rowIndex).get(columnIndex);
    }
}
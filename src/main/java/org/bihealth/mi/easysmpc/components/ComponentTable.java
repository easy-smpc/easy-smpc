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
package org.bihealth.mi.easysmpc.components;

import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;

import org.bihealth.mi.easysmpc.spreadsheet.CellsAccessor;
import org.bihealth.mi.easysmpc.spreadsheet.SpreadsheetCell;
import org.bihealth.mi.easysmpc.spreadsheet.SpreadsheetCell.SpreadsheetCellEditor;
import org.bihealth.mi.easysmpc.spreadsheet.SpreadsheetCell.SpreadsheetCellRenderer;
import org.bihealth.mi.easysmpc.spreadsheet.SpreadsheetTableModel;

/**
 * A component to display a table
 * 
 * @author Felix Wirth
 *
 */
public class ComponentTable extends JTable {

    /** SVUID */
    private static final long serialVersionUID = 5834997292839255995L;
    
     
    public ComponentTable(int numRows, int numColumns) {
        super(new SpreadsheetTableModel(true, numRows, numColumns));
        
        // Configure
        this.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
        this.setCellSelectionEnabled(true);
        this.setDefaultRenderer(SpreadsheetCell.class, new SpreadsheetCellRenderer());
        this.setDefaultEditor(SpreadsheetCell.getClassStatic(), new SpreadsheetCellEditor(new JTextField(), new CellsAccessor() {
            
            @Override
            public SpreadsheetCell getCellAt(int row, int column) {
                return (SpreadsheetCell) getModel().getValueAt(row, column);
            }
        }));
        
        // Set first column as row names
        setRowNames();
        
        // Set first editable cell as selected 
        changeSelection(0, 1, false, false);
    }


    /**
     * Sets the first columns as names for the rows
     */
    private void setRowNames() {
        
        // Set number as name
        for(int i = 0; i < getModel().getRowCount(); i++) {
            getModel().setValueAt(SpreadsheetCell.createNew(String.valueOf(i)), i, 0);
        }
        
        // Set cell renderer to look like header
        getColumnModel().getColumn(0).setCellRenderer(getTableHeader().getDefaultRenderer());        
    }


    /**
     * Returns the data of the current selected cell
     * 
     * @return
     */
    public SpreadsheetCell currentSelectedCellData() {
        if(this.getSelectedRow() < 0 || this.getSelectedColumn() < 0 ) {
            return null;
        } 
        return (SpreadsheetCell) this.getModel().getValueAt(this.getSelectedRow(), this.getSelectedColumn());
    }
}
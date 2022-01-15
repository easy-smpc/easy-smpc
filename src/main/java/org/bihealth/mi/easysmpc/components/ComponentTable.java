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
        this.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
        this.setCellSelectionEnabled(true);
        this.setDefaultRenderer(SpreadsheetCell.getClassStatic(), new SpreadsheetCellRenderer());
        this.setDefaultEditor(SpreadsheetCell.getClassStatic(), new SpreadsheetCellEditor(new JTextField(), this.getModel()));
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
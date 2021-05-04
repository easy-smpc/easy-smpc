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
package org.bihealth.mi.easysmpc.dataimport;

import java.math.BigDecimal;

import javax.swing.DefaultCellEditor;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableCellRenderer;

import org.apache.commons.lang3.math.NumberUtils;

/**
 * A cell in the spreadsheet table
 * 
 * @author Felix Wirth
 *
 */
public class SpreadsheetCell {
    
    /** Type */
    private SpreadsheetCellType type;
    /** Value if in big decimal */
    private BigDecimal valueBigDecimal;
    /** Value if in text */
    private String valueText;
    
    /**
     * Empty default constructor
     */
    public SpreadsheetCell() {
        
    }
    
    /**
     * Creates a new instance
     * 
     * @param value
     */
    SpreadsheetCell(String value) {
        
        // Check
        if (value == null) {
            throw new IllegalArgumentException("Value must not be null");
        }
        
        // Set as script if applicable 
        if (value.charAt(0) == '=') {
            this.type = SpreadsheetCellType.SCRIPT;
            this.valueText = value;
            // TODO Validate script
        }
        

        try {
            // Set as decimal if applicable
            this.valueBigDecimal = NumberUtils.createBigDecimal(value);
            this.type = SpreadsheetCellType.NUMBER;
            
        } catch (Exception e) {
            // Set as text if applicable
            this.type = SpreadsheetCellType.TEXT;
            this.valueText = value;
        }
    }
    
    /**
     * Gets the content of the cell
     * 
     * @return
     */
    public String getContentDefinition() {
        switch(this.type) {
        case TEXT: return valueText;
        case NUMBER: return valueBigDecimal.toPlainString();
        case SCRIPT: return valueText;
        default: return "";
        }
    }
    
    /**
     * @return
     */
    public String getDisplayedText() {
        switch(this.type) {
        case TEXT: return valueText;
        case NUMBER: return valueBigDecimal.toPlainString();
        case SCRIPT: return "";
        default: return "";
        }
    }
    
    /**
     * Can the cell be calculated?
     * 
     * @return
     */
    public boolean iscalculable() {
        if (type == SpreadsheetCellType.TEXT || type == SpreadsheetCellType.SCRIPT) {
            return false;
        }
        
        return true;
    }
    
    /**
     * Data types for a spreadsheet cell
     * 
     * @author Felix Wirth
     *
     */
    public enum SpreadsheetCellType {
        TEXT, NUMBER, SCRIPT
    }
    
    /**
     * Cell renderer for table
     * 
     * @author Felix Wirth
     *
     */
    public static class SpreadsheetCellRenderer extends DefaultTableCellRenderer {

        /** SVUID */
        private static final long serialVersionUID = -8959341140434408618L;

        public SpreadsheetCellRenderer() { super(); }

        public void setValue(Object value) {
            
            // Set empty if null
            if (value == null) {
                setText("");
                return;
            }
            
            // Check for correct object if not null
            if (!(value instanceof SpreadsheetCell)) {
                throw new IllegalArgumentException("Unexpected object type for SpreadsheetCellRenderer!");
            }
            
            // Set text
            setText(((SpreadsheetCell) value).getDisplayedText());
        }
    }
    
    /**
     * Cell editor for table
     * 
     * @author Felix Wirth
     *
     */
    
    public static class SpreadsheetCellEditor extends DefaultCellEditor {
        
        /** SVUID */
        private static final long serialVersionUID = -3934596706570236505L;

        public SpreadsheetCellEditor(final JTextField textField) {
            super(textField);
            delegate = new EditorDelegate() {
                /** SVUID */
                private static final long serialVersionUID = 2088491318086396011L;

                public void setValue(Object cell) {
                    textField.setText((cell != null) ? ((SpreadsheetCell) cell).getDisplayedText() : "");
                }

                public Object getCellEditorValue() {
                    String text = textField.getText();
                    return text == null || text.length() > 0 ? new SpreadsheetCell(text) : null;
                }
            };
            textField.addActionListener(delegate);
        }
    }
}
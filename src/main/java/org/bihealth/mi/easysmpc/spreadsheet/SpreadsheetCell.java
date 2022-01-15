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

import java.math.BigDecimal;

import javax.swing.DefaultCellEditor;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableModel;

import org.apache.commons.lang3.math.NumberUtils;

/**
 * A cell in the spreadsheet table
 * 
 * @author Felix Wirth
 *
 */
public abstract class SpreadsheetCell {

    /** Type */
    private SpreadsheetCellType type;

    /**
     * Creates a new instance
     * 
     * @param value
     */
    SpreadsheetCell(SpreadsheetCellType type) {
        this.type = type;
    }

    /**
     * Gets the content of the cell
     * 
     * @return
     */
    public abstract String getContentDefinition();

    /**
     * Returns the displayed text
     * 
     * @return
     */
    public abstract String getDisplayedText();

    /**
     * Gets the content of the cell as big decimal
     * 
     * @return
     */
    public abstract BigDecimal getContentBigDecimal();

    /**
     * Can the cell be calculated?
     * 
     * @return
     */
    public abstract boolean isCalculable();

    /**
     * @return the type
     */
    public SpreadsheetCellType getType() {
        return type;
    }

    public static Class<SpreadsheetCell> getClassStatic() {
        return SpreadsheetCell.class;
    }

    /**
     * Create a new, suitable sub class
     * 
     * @param value
     * @param tableModel
     * @return
     */
    public static SpreadsheetCell createNew(String value, TableModel tableModel) {

        // Check
        if (value == null) { throw new IllegalArgumentException("Value must not be null"); }

        // Set as script if applicable
        if (value.charAt(0) == '=') { return SpreadsheetCellFunction.createNew(value, tableModel); }

        try {
            // Set as decimal if applicable
            BigDecimal valueBigDecimal = NumberUtils.createBigDecimal(value);
            return new SpreadsheetCellNumber(valueBigDecimal);

        } catch (Exception e) {
            // Set as text if applicable
            return new SpreadsheetCellText(value);
        }
    }

    /**
     * Data types for a spreadsheet cell
     * 
     * @author Felix Wirth
     *
     */
    public static enum SpreadsheetCellType {
                                            TEXT,
                                            NUMBER,
                                            SCRIPT
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

        public SpreadsheetCellRenderer() {
            super();
        }

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
            String text = ((SpreadsheetCell) value).getDisplayedText();
            setText(text != null ? text : "");
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
        /** Text field*/
        private JTextField textField;
 
        public void setTextField(String text) {
            textField.setText(text);
        }
        public SpreadsheetCellEditor(final JTextField textField, TableModel tableModel) {
            super(textField);
            this.textField = textField;
            delegate = new EditorDelegate() {
                /** SVUID */
                private static final long serialVersionUID = 2088491318086396011L;

                public void setValue(Object cell) {
                    textField.setText((cell != null) ? ((SpreadsheetCell) cell).getDisplayedText()
                            : "");
                }

                public Object getCellEditorValue() {
                    String text = textField.getText();
                    return text == null || text.length() > 0
                            ? SpreadsheetCell.createNew(text, tableModel)
                            : null;
                }
            };
            textField.addActionListener(delegate);
        }
    }
}
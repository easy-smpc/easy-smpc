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
package org.bihealth.mi.easysmpc;

import java.awt.BorderLayout;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.bihealth.mi.easysmpc.components.ComponentTable;
import org.bihealth.mi.easysmpc.components.ComponentTextArea;
import org.bihealth.mi.easysmpc.resources.Resources;

/**
 * A perspective
 * 
 * @author Fabian Prasser
 * @author Felix Wirth
 */
public class Perspective1CreateSpreadsheet extends Perspective implements ListSelectionListener {
    
    /** The formula field */
    private ComponentTextArea formularField;
    private ComponentTable table;

    /**
     * Creates the perspective
     * @param app
     */
    protected Perspective1CreateSpreadsheet(App app) {
        super(app, Resources.getString("PerspectiveCreateSpreedsheet.0"), 1, false); //$NON-NLS-1$
    }
    
    /**
     * Initialize perspective based on model
     */
    @Override
    public void initialize() {
     
        
        // Update GUI
        getPanel().revalidate();
        getPanel().repaint(); 
    }

    /**
     *Creates and adds UI elements
     */
    @Override
    protected void createContents(JPanel panel) {

        // Layout
        panel.setLayout(new BorderLayout());
        
        // Formular field        
        formularField = new ComponentTextArea(null);
        
        
        // Table
        table = new ComponentTable(Resources.TABLE_SIZE, Resources.TABLE_SIZE);
        table.getSelectionModel().addListSelectionListener(this);
        JScrollPane scrollPane = new JScrollPane(table, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, formularField, scrollPane);
        splitPane.setDividerLocation(20);
        panel.add(splitPane, BorderLayout.CENTER);
    }

    @Override
    public void valueChanged(ListSelectionEvent e) {
        setFormularFieldByCurrentTableIndex();
        
    }

    /**
     * Sets the content of the formular field by the current selected cell
     */
    private void setFormularFieldByCurrentTableIndex() {
        
        // Check
        if (table.currentSelectedCellData() == null) {
            formularField.setText("");
            return;
        }
        formularField.setText(table.currentSelectedCellData().getContentDefinition());        
    }
}

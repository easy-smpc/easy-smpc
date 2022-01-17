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
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.IOException;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.bihealth.mi.easysmpc.components.ComponentTable;
import org.bihealth.mi.easysmpc.resources.Resources;
import org.bihealth.mi.easysmpc.spreadsheet.SpreadsheetCell.SpreadsheetCellEditor;

/**
 * A perspective
 * 
 * @author Fabian Prasser
 * @author Felix Wirth
 */
public class Perspective1CreateSpreadsheet extends Perspective implements ListSelectionListener {
    
    /** The formula field */
    private JTextField formulaField;
    /** The table */
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
        
        // Formula field
        formulaField = new JTextField();
        formulaField.addKeyListener(new KeyListener() {

            @Override
            public void keyTyped(KeyEvent e) {
                if(!table.isEditing()) {
                    table.editCellAt(table.getSelectedRow(), table.getSelectedColumn());
                } 
            }
            
            @Override
            public void keyReleased(KeyEvent e) {
                // Empty
            }
            
            @Override
            public void keyPressed(KeyEvent e) {
                if(e.getKeyCode() == KeyEvent.VK_ESCAPE){
                    actionTableEditCancel();
                    return;
                }
                
                if(e.getKeyCode() == KeyEvent.VK_ENTER) {
                    actionTableEditOk();
                }
            }
        });
        formulaField.getDocument().addDocumentListener(new DocumentListener() {            
            @Override
            public void removeUpdate(DocumentEvent e) {
                ((SpreadsheetCellEditor) table.getCellEditor(table.getSelectedRow(), table.getSelectedColumn())).setTextField(formulaField.getText());                 
            }
            
            @Override
            public void insertUpdate(DocumentEvent e) {
                ((SpreadsheetCellEditor) table.getCellEditor(table.getSelectedRow(), table.getSelectedColumn())).setTextField(formulaField.getText());
            }
            
            @Override
            public void changedUpdate(DocumentEvent e) {
                ((SpreadsheetCellEditor) table.getCellEditor(table.getSelectedRow(), table.getSelectedColumn())).setTextField(formulaField.getText());
            }
        });
        
        // Table
        table = new ComponentTable(Resources.TABLE_SIZE, Resources.TABLE_SIZE);
        table.getSelectionModel().addListSelectionListener(this);
        table.getColumnModel().getSelectionModel().addListSelectionListener(this);
        JScrollPane scrollPane = new JScrollPane(table, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        
        // Buttons pane for formula
        JPanel buttonsPane = new JPanel();
        buttonsPane.setLayout(new GridLayout(2, 1));
        JButton okButton = new JButton();
        try {
            okButton.setIcon(new ImageIcon(Resources.getCheckmark()));
        } catch (IOException e) {
            JOptionPane.showMessageDialog(getPanel(), Resources.getString("Participant.3"), Resources.getString("App.13"), JOptionPane.ERROR_MESSAGE);
        }
        okButton.addActionListener(new ActionListener() {           
            @Override
            public void actionPerformed(ActionEvent e) {
                actionTableEditOk();
            }
        });
        JButton cancelButton = new JButton();
        try {
            cancelButton.setIcon(new ImageIcon(Resources.getCancel()));
        } catch (IOException e) {
            JOptionPane.showMessageDialog(getPanel(), Resources.getString("Participant.3"), Resources.getString("App.13"), JOptionPane.ERROR_MESSAGE);
        }
        cancelButton.addActionListener(new ActionListener() {           
            @Override
            public void actionPerformed(ActionEvent e) {
                actionTableEditCancel();
            }
        });
        buttonsPane.add(okButton);
        buttonsPane.add(cancelButton);
        
        // Formula pane
        JPanel formulaPane = new JPanel(new BorderLayout());
        formulaPane.add(buttonsPane, BorderLayout.EAST);
        formulaPane.add(formulaField, BorderLayout.CENTER);
        
        // Split pane
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, formulaPane, scrollPane);
        splitPane.setDividerLocation(35);
        panel.add(splitPane, BorderLayout.CENTER);
    }

    @Override
    public void valueChanged(ListSelectionEvent e) {
        System.out.println(table.getSelectedRow() + " " + table.getSelectedColumn() );
        setFormularFieldByCurrentTableIndex();
        
    }

    /**
     * Sets the content of the formula field by the current selected cell
     */
    private void setFormularFieldByCurrentTableIndex() {
        
        // Check
        if (table.currentSelectedCellData() == null || table.getSelectedColumn() == 0) {
            formulaField.setText("");
            return;
        }
        formulaField.setText(table.currentSelectedCellData().getContentDefinition());        
    }
    
    /**
     * Action when table editing and canceled pressed
     */
    private void actionTableEditCancel() {
        // Cancel editing
        table.editingCanceled(null);
        
        // Set old value in formula field
        setFormularFieldByCurrentTableIndex();
    }
    
    /**
     * Action when table editing and ok pressed
     */
    private void actionTableEditOk() {
        table.editingStopped(null);
                
        // Select one cell below if possible
        if (table.getSelectedRow() + 1 <= table.getModel().getRowCount()) {
            table.changeSelection(table.getSelectedRow() + 1, table.getSelectedColumn(), false, false);
        }
    }
}

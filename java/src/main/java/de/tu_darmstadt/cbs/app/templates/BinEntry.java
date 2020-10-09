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
package de.tu_darmstadt.cbs.app.templates;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.math.BigInteger;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import de.tu_darmstadt.cbs.app.Resources;
import lombok.Getter;

/**
 * @author Felix Wirth
 *
 */
public class BinEntry extends JPanel {
    /** SVUID */
    private static final long serialVersionUID = 7347360570010162977L;
    
    /** Text field containing the name of the bin */
    /**
     * Returns binNameTextField
     * @return
     */
    @Getter
    private JTextField binNameTextField;
    /**  Text field containing the data/value of the bin */
    /**
     * returns binValueField
     * @return
     */
    @Getter
    private JTextField binValueField;
    /** Listener to check for changes in the text fields for validation*/
    DocumentListener textfieldsChangeDocumentListener = new DocumentListener() {
        
        @Override
        public void removeUpdate(DocumentEvent e) {
            validateEnteredData();
        }
        @Override
        public void insertUpdate(DocumentEvent e) {
            validateEnteredData();
        }
        @Override
        public void changedUpdate(DocumentEvent e) {
            validateEnteredData();
        }
    };
    /**
     * Creates a new instance
     */
    public BinEntry() {
        
        this.setLayout(new GridLayout(0, 2, Resources.ROW_GAP, Resources.ROW_GAP));
        this.setMaximumSize(new Dimension(Integer.MAX_VALUE, Resources.ROW_HEIGHT));
        
        // Left
        JPanel left = new JPanel();
        left.setLayout(new BorderLayout());
        this.add(left);
        
        JLabel binNameLabel = new JLabel(Resources.getString("BinEntry.0")); //$NON-NLS-1$
        binNameTextField = new JTextField();
        binNameTextField.setColumns(Resources.DEFAULT_COLUMN_SIZE);
        binNameTextField.getDocument().addDocumentListener(this.textfieldsChangeDocumentListener);
        
        left.add(binNameLabel, BorderLayout.WEST);
        left.add(binNameTextField, BorderLayout.CENTER);

        // Right
        JPanel right = new JPanel();
        right.setLayout(new BorderLayout());
        this.add(right);
        
        JLabel binValueLabel = new JLabel(Resources.getString("BinEntry.1")); //$NON-NLS-1$
        this.binValueField = new JTextField();
        this.binValueField.setColumns(Resources.DEFAULT_COLUMN_SIZE);
        this.binValueField.getDocument().addDocumentListener(this.textfieldsChangeDocumentListener);
        right.add(this.binValueField, BorderLayout.WEST);
        right.add(this.binValueField, BorderLayout.CENTER);
    }

    /**
     * Creates a new instance
     * @param name Name of the bin
     * @param enabled Indicates whether the bin can be edited or not
     */
    public BinEntry(String name, boolean enabled) {
        this();
        this.binNameTextField.setText(name);
        this.binNameTextField.setEnabled(enabled);
    }
    
    /**
     * Validates entered data
     * @return
     */
    public boolean validateEnteredData() {
        boolean dataValid = true;
      //validate Name
        if (this.getBinNameTextField().getText().isBlank() || this.getBinNameTextField().getText().isEmpty() )
        {
            dataValid = false;
            this.getBinNameTextField().setBorder(BorderFactory.createLineBorder(Color.RED));
        }
        else this.getBinNameTextField().setBorder(BorderFactory.createEmptyBorder());
        //validate Value
        try {
            new BigInteger(this.getBinValueField().getText());
            this.getBinValueField().setBorder(BorderFactory.createEmptyBorder());
        }
        catch(NumberFormatException e){
            dataValid = false;
            this.getBinValueField().setBorder(BorderFactory.createLineBorder(Color.RED));
        }
        this.revalidate();
        this.repaint();
        return dataValid;
    }
}

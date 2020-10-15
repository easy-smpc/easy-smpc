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
package de.tu_darmstadt.cbs.app.components;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeListener;

import de.tu_darmstadt.cbs.app.Resources;

/**
 * Abstract entry with two fields
 * 
 * @author Felix Wirth
 * @author Fabian Prasser
 */
public abstract class ComponentEntry extends JPanel {

    /** SVUID */
    private static final long  serialVersionUID = -5730763994045143676L;

    /** Left field */
    private ComponentTextField fieldLeft;

    /** Right field */
    private ComponentTextField fieldRight;

    /** Change listener */
    private ActionListener     addListener;

    /** Change listener */
    private ActionListener     removeListener;

    /** Add */
    private JButton            add;

    /** Remove */
    private JButton            remove;

    /**
     * Creates a new instance
     * @param leftString
     * @param leftValue
     * @param leftEnabled
     * @param leftValidator
     * @param rightString
     * @param rightValue
     * @param rightEnabled
     * @param rightValidator
     * @param addRemoveEnabled
     */
    public ComponentEntry(String leftString,
                         String leftValue,
                         boolean leftEnabled, 
                         ComponentTextFieldValidator leftValidator,
                         String rightString, 
                         String rightValue,
                         boolean rightEnabled, 
                         ComponentTextFieldValidator rightValidator,
                         boolean addRemoveEnabled) {
        
        // Layout
        this.setBorder(new EmptyBorder(Resources.ROW_GAP, Resources.ROW_GAP, Resources.ROW_GAP, Resources.ROW_GAP));
        this.setLayout(new GridLayout(1, 2, Resources.ROW_GAP, Resources.ROW_GAP));
        this.setMaximumSize(new Dimension(Integer.MAX_VALUE, Resources.ROW_HEIGHT));
        this.setMinimumSize(new Dimension(0, Resources.ROW_HEIGHT));
        
        // Left
        JPanel left = new JPanel();
        left.setLayout(new BorderLayout());
        this.add(left);
        
        JLabel labelLeft = new JLabel(leftString); //$NON-NLS-1$
        fieldLeft = new ComponentTextField(leftValidator);
        
        // Value
        if (leftValue != null) {
            fieldLeft.setText(leftValue);
        }
        
        // Status
        if (!leftEnabled) {
            fieldLeft.setEnabled(false);
        }
  
        // Add
        left.add(labelLeft, BorderLayout.WEST);
        left.add(fieldLeft, BorderLayout.CENTER);

        // Right
        JPanel right = new JPanel();
        right.setLayout(new BorderLayout());
        this.add(right);
        
        JLabel labelRight = new JLabel(rightString); //$NON-NLS-1$
        this.fieldRight = new ComponentTextField(rightValidator);

        // Value
        if (rightValue != null) {
            fieldRight.setText(rightValue);
        }
        
        // Status
        if (!rightEnabled) {
            fieldRight.setEnabled(false);
        } 
        
        // Add
        right.add(labelRight, BorderLayout.WEST);
        right.add(this.fieldRight, BorderLayout.CENTER);
        
        // Add/remove
        if (addRemoveEnabled) {
            
            // Panels
            JPanel addremove = new JPanel();
            addremove.setLayout(new GridLayout(1, 2));
            right.add(addremove, BorderLayout.EAST);
            
            // Buttons
            this.add = new JButton("+");
            addremove.add(add);
            this.remove = new JButton("-");
            addremove.add(remove);
            
            // Listeners
            this.add.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    add();
                }
            });
            this.remove.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    remove();
                }
            });
        }
    }

    /**
     * Returns whether the settings are valid
     */
    public boolean areValuesValid() {
        return this.fieldLeft.isValueValid() && this.fieldRight.isValueValid();
    }
    
    /**
     * Returns whether the right field is valid
     */
    public boolean isFieldRightValueValid() {
        return this.fieldRight.isValueValid();
    }
    
    /**
     * @return
     */
    public String getLeftValue() {
        return this.fieldLeft.getText();
    }

    /**
     * Returns the right value
     * @return
     */
    public String getRightValue() {
        return this.fieldRight.getText();
    }
    
    /**
     * Sets a change listener
     * @param listener
     */
    public void setAddListener(ActionListener listener) {
        this.addListener = listener;
    }
    
    /**
     * Sets a change listener
     * @param listener
     */
    public void setChangeListener(ChangeListener listener) {
        this.fieldLeft.setChangeListener(listener);
        this.fieldRight.setChangeListener(listener);
    }
    
    /**
     * Sets a change listener
     * @param listener
     */
    public void setRemoveListener(ActionListener listener) {
        this.removeListener = listener;
    }
    
    /**
     * Add action
     */
    private void add() {
        if (addListener != null) {
            addListener.actionPerformed(new ActionEvent(this, 0, null));
        }        
    }

    /** 
     * Remove action
     */
    private void remove() {
        if (removeListener != null) {
            removeListener.actionPerformed(new ActionEvent(this, 0, null));
        }
    }
}

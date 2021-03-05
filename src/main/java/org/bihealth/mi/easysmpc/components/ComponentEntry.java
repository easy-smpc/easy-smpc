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

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeListener;

import org.bihealth.mi.easysmpc.resources.Resources;

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

    /**
     * Creates a new instance
     * @param leftString
     * @param leftValue
     * @param leftEnabled
     * @param leftValidator
     * @param leftIsPassword
     * @param rightString
     * @param rightValue
     * @param rightEnabled
     * @param rightValidator
     * @param rightIsPassword
     * @param additionalControlsEnabled
     */
    public ComponentEntry(String leftString,
                         String leftValue,
                         boolean leftEnabled, 
                         ComponentTextFieldValidator leftValidator,
                         boolean leftIsPassword,
                         String rightString, 
                         String rightValue,
                         boolean rightEnabled, 
                         ComponentTextFieldValidator rightValidator,
                         boolean rightIsPassword,
                         boolean additionalControlsEnabled) {
        
        // Layout
        this.setBorder(new EmptyBorder(Resources.ROW_GAP, Resources.ROW_GAP, Resources.ROW_GAP, Resources.ROW_GAP));
        this.setLayout(new GridLayout(1, 2, Resources.ROW_GAP, Resources.ROW_GAP));

        // Left
        JPanel left = new JPanel();
        left.setLayout(new BorderLayout());
        this.add(left);
        
        JLabel labelLeft = new JLabel(leftString); //$NON-NLS-1$
        fieldLeft = new ComponentTextField(leftValidator, leftIsPassword);
        
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
        this.fieldRight = new ComponentTextField(rightValidator, rightIsPassword);

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
        
        // Additional controls
        JPanel additionalControls = createAdditionalControls();
        if (additionalControls != null) {
            right.add(additionalControls, BorderLayout.EAST);
            for (Component c : additionalControls.getComponents()) {
                c.setEnabled(additionalControlsEnabled);
            }
        }
        
        // Set size to fix layout issues
        this.setMaximumSize(new Dimension(Integer.MAX_VALUE, this.fieldLeft.getPreferredSize().height + Resources.ROW_GAP * 2));
        this.setMinimumSize(new Dimension(0, this.fieldLeft.getPreferredSize().height + Resources.ROW_GAP * 2));
    }
    
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
     * @param additionalControlsEnabled
     */
    public ComponentEntry(String leftString,
                         String leftValue,
                         boolean leftEnabled, 
                         ComponentTextFieldValidator leftValidator,
                         String rightString, 
                         String rightValue,
                         boolean rightEnabled, 
                         ComponentTextFieldValidator rightValidator,
                         boolean additionalControlsEnabled) {
        this (leftString, leftValue, leftEnabled, leftValidator, false,
              rightString, rightValue, rightEnabled, rightValidator, false,
              additionalControlsEnabled);
    }

    /**
     * Returns whether the settings are valid
     */
    public boolean areValuesValid() {
        return this.fieldLeft.isValueValid() && this.fieldRight.isValueValid();
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
     * Returns whether the right field is valid
     */
    public boolean isFieldRightValueValid() {
        return this.fieldRight.isValueValid();
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
     * Sets the left value
     * @return
     */
    public void setLeftValue(String text) {
        this.fieldLeft.setText(text);
    }
    
    /**
     * Sets the right value
     * @return
     */
    public void setRightValue(String text) {
        this.fieldRight.setText(text);
    }
    
    /** Creates an additional control panel
     * @return
     */
    protected abstract JPanel createAdditionalControls();
}

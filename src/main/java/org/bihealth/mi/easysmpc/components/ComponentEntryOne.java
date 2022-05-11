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
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeListener;

import org.bihealth.mi.easysmpc.resources.Resources;

/**
 * Entry with one field and a label
 * 
 * @author Felix Wirth
 * @author Fabian Prasser
 */
public class ComponentEntryOne extends JPanel {

    /** SVUID */
    private static final long serialVersionUID = 1L;

    /** Field */
    private ComponentTextField field;


    /**
     * Creates a new instance
     * 
     * @param labelText
     * @param value
     * @param enabled
     * @param validator
     * @param leftIsPassword
     * @param isOwnParticipant
     */
    public ComponentEntryOne(String labelText,
                             String value,
                             boolean enabled,
                             ComponentTextFieldValidator validator,
                             boolean leftIsPassword,
                             boolean isOwnParticipant) {
        
        // Layout
        this.setBorder(new EmptyBorder(Resources.ROW_GAP, Resources.ROW_GAP, Resources.ROW_GAP, Resources.ROW_GAP));
        this.setLayout(new BorderLayout(Resources.ROW_GAP, Resources.ROW_GAP));
        
        
        // Label
        JLabel label = new JLabel(labelText); //$NON-NLS-1$
        
        // Field
        field = new ComponentTextField(validator, leftIsPassword);
        
        // Value
        if (value != null) {
            field.setText(value);
        }
        
        // Status
        if (!enabled) {
            field.setEnabled(false);
        }
  
        // Add
        this.add(label, BorderLayout.WEST);
        this.add(field, BorderLayout.CENTER);              
        
        // Set size to fix layout issues
        this.setMaximumSize(new Dimension(Integer.MAX_VALUE, this.field.getPreferredSize().height + Resources.ROW_GAP * 2));
        this.setMinimumSize(new Dimension(0, this.field.getPreferredSize().height + Resources.ROW_GAP * 2));
        
        // Set background
        if (isOwnParticipant) {
            setBackgroundAllSubComponents(this);
        }
    }    

    /**
     * Get value
     * 
     * @return
     */
    public String getValue() {
        return this.field.getText(); 
   }
    
    /**
     * Returns whether the right field is valid
     * @return
     */
    public boolean isValueValid() {
        return this.field.isValueValid();
    }
    
    /**
     * Sets a change listener
     * @param listener
     */
    public void setChangeListener(ChangeListener listener) {
        this.field.setChangeListener(listener);
    }

    /**
     * Sets value
     * @return
     */
    public void setValue(String text) {
        this.field.setText(text);
    }
    
    /**
     * @param componentEntry
     */
    private void setBackgroundAllSubComponents(Component c) {
        c.setBackground(new Color(223,223,223));
        if (c instanceof Container)
        for(Component childComponent : ((Container) c).getComponents()) {
            setBackgroundAllSubComponents(childComponent);
        }
    }
    
    /**
     * Set field enabled
     * 
     * @param enabled
     */
    public void setFieldEnabled(boolean enabled) {
        this.field.setEnabled(enabled);
    }
}

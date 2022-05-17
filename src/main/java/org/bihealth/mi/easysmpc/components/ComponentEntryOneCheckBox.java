package org.bihealth.mi.easysmpc.components;

import java.awt.BorderLayout;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * A field which can be activated an deactivated with a checkbox on the left
 * 
 * @author Felix Wirth
 *
 */
public class ComponentEntryOneCheckBox extends JPanel {

    /** SVUID */
    private static final long serialVersionUID = -5270653689813794513L;
    /** Field */
    private ComponentEntryOne field;
    /** Check box */
    private JCheckBox         checkBox;
    
    /**
     * Creates a new instance
     * 
     * @param text
     * @param validator
     */
    public ComponentEntryOneCheckBox(String text, ComponentTextFieldValidator validator) {
        
        // Create field
        field = new ComponentEntryOne(text, null, true, validator, false, false);
        field.setMouseListener(new MouseListener() {

            @Override
            public void mouseReleased(MouseEvent e) {
                // Empty
            }

            @Override
            public void mousePressed(MouseEvent e) {
                // Empty
            }

            @Override
            public void mouseExited(MouseEvent e) {
                // Empty
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                // Empty
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                checkBox.doClick();
            }
        });
        
        // Create check box
        checkBox = new JCheckBox();
        checkBox.addChangeListener(new ChangeListener() {

            @Override
            public void stateChanged(ChangeEvent e) {
                if (checkBox.isSelected()) {
                    field.setFieldEnabled(true);
                } else {
                    field.setFieldEnabled(false);
                    field.setValue(null);
                }
            }
        });
        
        // Set panel
        this.setLayout(new BorderLayout());
        
        // Add
        this.add(checkBox, BorderLayout.WEST);
        this.add(field, BorderLayout.CENTER);

        // Deactivate as default
        checkBox.setSelected(false);      
    }

    /**
     * Returns whether the field is valid
     * 
     * @return
     */
    public boolean isValueValid() {
        return this.field.isValueValid();
    }


    /**
     * Set change listener
     * 
     * @param listener
     */
    public void setChangeListener(ChangeListener listener) {
        this.field.setChangeListener(listener);
    }


    /**
     * Get value
     * 
     * @return
     */
    public String getValue() {
        return this.field.getValue();
    }
    
    /**
     * Set value
     * 
     * @param text
     */
    public void setValue(String text) {
        // Set text field value
        this.field.setValue(text);

        // Set field enabled and checkbox clicked if not null and vv
        if (text != null) {
            field.setFieldEnabled(true);
            checkBox.setSelected(true);
        } else {
            field.setFieldEnabled(false);
            checkBox.setSelected(false);
        }
    }
}

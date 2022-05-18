package org.bihealth.mi.easysmpc.components;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
    private static final long       serialVersionUID = -5270653689813794513L;
    /** Field */
    private final ComponentEntryOne field;
    /** Check box */
    private final JCheckBox         checkBox;
    /** Target bit */
    private final boolean           selectCheckBoxToEdit;
    /** Change listener */
    private ChangeListener          listener;
    
    /**
     * Creates a new instance
     * 
     * @param text
     * @param validator
     */
    public ComponentEntryOneCheckBox(String text, ComponentTextFieldValidator validator) {
        this(text, validator, true, false);
    }
        
    /**
     * Creates a new instance
     * 
     * @param text
     * @param validator
     * @param selectCheckBoxToEdit - if true the text field is enabled if check box is selected, if false the text field is enabled if check box is not selected
     * @param isPasswordField
     */
    public ComponentEntryOneCheckBox(String text,
                                     ComponentTextFieldValidator validator,
                                     boolean selectCheckBoxToEdit,
                                     boolean isPasswordField) {
        // Store
        this.selectCheckBoxToEdit = selectCheckBoxToEdit;
        
        // Create validator wrapper        
        ComponentTextFieldValidator validatorWrapper = new ComponentTextFieldValidator() {

            @Override
            public boolean validate(String text) {
                
                // Is field validator necessary?
                if (checkBox != null && selectCheckBoxToEdit == !checkBox.isSelected()) {
                    return true;
                }

                // Return validator result
                return validator != null ? validator.validate(text) : true;
            }            
        };
        
        // Create field
        field = new ComponentEntryOne(text, null, true, validatorWrapper, isPasswordField, false);
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
                    field.setFieldEnabled(selectCheckBoxToEdit);
                    field.setValue(field.getValue());
                } else {
                    field.setFieldEnabled(!selectCheckBoxToEdit);
                    field.setValue(null);
                }                
            }
        });
        checkBox.addActionListener(new ActionListener() {
            
            @Override
            public void actionPerformed(ActionEvent e) {
                if(listener != null) {                    
                    listener.stateChanged(new ChangeEvent(this));
                }
            }
        });
        
        // Set panel
        this.setLayout(new BorderLayout());
        
        // Add
        this.add(checkBox, BorderLayout.WEST);
        this.add(field, BorderLayout.CENTER);

        // Set default
        checkBox.setSelected(!selectCheckBoxToEdit);      
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
        this.listener = listener;
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

        // Set field enabled and check box clicked if not null and vv
        if (text != null) {
            field.setFieldEnabled(selectCheckBoxToEdit);
            checkBox.setSelected(selectCheckBoxToEdit);
        } else {
            field.setFieldEnabled(!selectCheckBoxToEdit);
            checkBox.setSelected(!selectCheckBoxToEdit);
        }
    }
}

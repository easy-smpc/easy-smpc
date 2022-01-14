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

import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.bihealth.mi.easysmpc.resources.Resources;

/**
 * A validated text field
 * 
 * @author Fabian Prasser
 */
public class ComponentTextField extends JPanel {

    /** SVUID */
    private static final long           serialVersionUID = 5588848051841828428L;

    /** Validator */
    private ComponentTextFieldValidator validator;

    /** Listener */
    private ChangeListener              listener;

    /** Text field */
    private JTextField                  field;

    /**
     * Creates a new instance
     * @param validator
     */
    public ComponentTextField(ComponentTextFieldValidator validator) {
        this(validator, false);
    }
    
    /**
     * Creates a new instance
     * @param validator
     * @param password
     */
    public ComponentTextField(ComponentTextFieldValidator validator, boolean password) {
        this.validator = validator;
        this.field = password ? new JPasswordField() : new JTextField();
        this.setLayout(new BorderLayout());
        this.add(field, BorderLayout.CENTER);
        this.validateValue();
        this.field.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void changedUpdate(DocumentEvent e) {
                validateValue();
            }

            @Override
            public void insertUpdate(DocumentEvent e) {
                validateValue();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                validateValue();                    
            }
        });
    }

    /**
     * Gets text
     */
    public String getText() {
        return this.field.getText().trim();
    }

    /**
     * Is the text field enabled
     */
    public boolean isEnabled() {
        return  this.field.isEditable();
    }
    
    /**
     * Returns whether the value is valid
     * @return
     */
    public boolean isValueValid() {
        return this.validator.validate(getText());
    }
    
    /**
     * Sets a change listener
     * @param listener
     */
    public void setChangeListener(ChangeListener listener) {
        this.listener = listener;
    }
    
    /**
     * Enables the text field
     */
    @Override
    public void setEnabled(boolean enabled) {
        this.field.setEditable(enabled);
    }
    
    /**
     * Sets text for button
     * @param text
     */
    public void setText(String t) {
        this.field.setText(t);
        this.validateValue();
    }
    
    /**
     * Validates the value
     */
    private void validateValue() {
        boolean isValid;
        if (validator != null && this.field != null) {
            isValid = validator.validate(getText());
        } else {
            isValid = true;
        }
        this.setBorder(isValid ? Resources.DEFAULT_BORDER : Resources.INVALID_BORDER);
        if (listener != null) {
            listener.stateChanged(new ChangeEvent(this));
        }
    }
}
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

import javax.swing.JComponent;
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
public class ComponentTextField extends JComponent {

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
        this.validateValue();
        this.field = password ? new JPasswordField() : new JTextField();
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
     * Returns whether the value is valid
     * @return
     */
    public boolean isValueValid() {
        return this.validator.validate(this.field.getText());
    }

    /**
     * Sets a change listener
     * @param listener
     */
    public void setChangeListener(ChangeListener listener) {
        this.listener = listener;
    }
    
    /**
     * Gets text
     */
    public String getText() {
        return this.field.getText();
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
        if (validator != null) {
            isValid = validator.validate(this.field.getText());
        } else {
            isValid = true;
        }
        this.setBorder(isValid ? Resources.DEFAULT_BORDER : Resources.INVALID_BORDER);
        if (listener != null) {
            listener.stateChanged(new ChangeEvent(this));
        }
    }
}
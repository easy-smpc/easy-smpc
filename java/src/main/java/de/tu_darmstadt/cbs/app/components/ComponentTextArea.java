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

import javax.swing.Action;
import javax.swing.JPopupMenu;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.DefaultEditorKit;

import de.tu_darmstadt.cbs.app.resources.Resources;

/**
 * A validated text area
 * 
 * @author Fabian Prasser
 */
public class ComponentTextArea extends JTextArea {

    /** SVID */
    private static final long           serialVersionUID = 2813095397984608993L;

    /** Validator */
    private ComponentTextFieldValidator validator;

    /** Listener */
    private ChangeListener              listener;

    /**
     * Creates a new instance
     * @param validator
     */
    public ComponentTextArea(ComponentTextFieldValidator validator) {
        this.setLineWrap(true);
        this.validator = validator;
        this.validateValue();
        // Pop up menu in textfield
        JPopupMenu menu = new JPopupMenu();
        Action paste = new DefaultEditorKit.PasteAction();
        paste.putValue(Action.NAME, Resources.getString("TextArea.paste"));
        paste.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("control V"));
        menu.add(paste);
        this.setComponentPopupMenu(menu);
        //listener for validation
        this.getDocument().addDocumentListener(new DocumentListener() {
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
    
    public ComponentTextArea(String textDefault, ComponentTextFieldValidator validator) {
        this(validator);
        this.setText(textDefault);        
    }

    /**
     * Returns whether the value is valid
     * @return
     */
    public boolean isValueValid() {
        if (validator != null)
        {
        return this.validator.validate(this.getText());
        }
        return true;
    }

    /**
     * Sets a change listener
     * @param listener
     */
    public void setChangeListener(ChangeListener listener) {
        this.listener = listener;
    }
    
    /**
     * Validates the value
     */
    private void validateValue() {
        this.setBorder(isValueValid() ? Resources.DEFAULT_BORDER : Resources.INVALID_BORDER);
        if (listener != null) {
            listener.stateChanged(new ChangeEvent(this));
        }
    }
}

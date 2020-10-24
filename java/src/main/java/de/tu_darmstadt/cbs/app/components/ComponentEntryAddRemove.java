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

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JPanel;

/**
 * Abstract entry with two fields and add/remove button
 * 
 * @author Felix Wirth
 * @author Fabian Prasser
 */
public abstract class ComponentEntryAddRemove extends ComponentEntry {

    /** SVUID */
    private static final long serialVersionUID = -3748298443786331047L;

    /** Add */
    private JButton           add;

    /** Remove */
    private JButton           remove;

    /** Change listener */
    private ActionListener    addListener;

    /** Change listener */
    private ActionListener    removeListener;

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
     */
    public ComponentEntryAddRemove(String leftString,
                                   String leftValue,
                                   boolean leftEnabled,
                                   ComponentTextFieldValidator leftValidator,
                                   String rightString,
                                   String rightValue,
                                   boolean rightEnabled,
                                   ComponentTextFieldValidator rightValidator,
                                   boolean buttonsEnabled) {
        super(leftString,
              leftValue,
              leftEnabled,
              leftValidator,
              rightString,
              rightValue,
              rightEnabled,
              rightValidator,
              buttonsEnabled);
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
    
    
    /**
     * Creates and additional control panel
     */
    @Override
    protected JPanel createAdditionalControls() {
        
        // Panels
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(1, 2));

        // Buttons
        this.add = new JButton("+");
        panel.add(add);
        this.remove = new JButton("-");
        panel.add(remove);

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
        
        // Done
        return panel;
    }
}

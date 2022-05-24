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

import java.awt.event.ActionEvent;
import java.util.function.BooleanSupplier;

import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;

/**
 * Choose between two radio buttons and asks with a confirm dialog (if dialog is necessary) before a change from the second to the first happens.
 * If the change is confirmed, the set listener is called with the instance of this class.
 * A change from the first to second happens without a dialog.
 * 
 * @author Felix Wirth
 *
 */
public class ComponentRadioComfirmSwitchEntry extends ComponentRadioEntry {

    /** SVUID */
    private static final long serialVersionUID = -1564178389286520613L;
    /** Parent panel*/
    private JPanel parent;
    /** Title of confirm dialog */
    private String confirmTitle;
    /** Message of confirm dialog*/
    private String confirmMessage;
    /** Allows to check whether a dialog is necessary*/
    private BooleanSupplier dialogNecessary;

    /**
     * Creates a new instance
     * 
     * @param title
     * @param firstOptionText
     * @param secondOptionText
     * @param orientationRadios - true if y-oriented, else x-oriented
     * @param parent
     * @param confirmMessage
     * @param confirmTitle
     * @param dialogNecessary - Boolean supplier to check wether a confirm dialog is necessary
     */
    public ComponentRadioComfirmSwitchEntry(String title,
                                  String firstOptionText,
                                  String secondOptionText,
                                  boolean orientationRadios,
                                  JPanel parent,
                                  String confirmMessage,
                                  String confirmTitle,
                                  BooleanSupplier dialogNecessary) {
        // Super
        super(title, firstOptionText, secondOptionText, orientationRadios);
        
        // Store
        this.parent = parent;
        this.confirmMessage = confirmMessage;
        this.confirmTitle = confirmTitle;
        this.dialogNecessary = dialogNecessary;
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        
        // If swap from simple to advanced pass on
        if (!this.isFirstOptionSelected() && this.getChangeListener() != null) {
            this.getChangeListener().stateChanged(new ChangeEvent(this));
            return;
        }
        
        // If swap from advanced to simple and no dialog necessary
        if (!dialogNecessary.getAsBoolean() && this.isFirstOptionSelected() && this.getChangeListener() != null) {
            this.getChangeListener().stateChanged(new ChangeEvent(this));
            return;
        }
        
        // If swap from advanced to simple ask before
        if (this.isFirstOptionSelected() 
             && JOptionPane.showConfirmDialog(this.parent, confirmMessage, confirmTitle, JOptionPane.OK_CANCEL_OPTION) == JOptionPane.YES_OPTION
             && this.getChangeListener() != null) {
            
            // Pass change on
            this.getChangeListener().stateChanged(new ChangeEvent(this));
        } else {
            // Reset
            this.setFirstOptionSelected(false);
        }
    }
}

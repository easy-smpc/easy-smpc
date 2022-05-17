package org.bihealth.mi.easysmpc.components;

import java.awt.event.ActionEvent;
import java.util.function.BooleanSupplier;

import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;

/**
 * Choose between two radio buttons and ask a before a change from the second to the first happens 
 * When a change happens the set listener is called with the instance of this class 
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

    public ComponentRadioComfirmSwitchEntry(String title,
                                  String firstOptionText,
                                  String secondOptionText,
                                  boolean orientationRadiosYAxi,
                                  JPanel parent,
                                  String confirmMessage,
                                  String confirmTitle,
                                  BooleanSupplier dialogNecessary) {
        // Super
        super(title, firstOptionText, secondOptionText, orientationRadiosYAxi);
        
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

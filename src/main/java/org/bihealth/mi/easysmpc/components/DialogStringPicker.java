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
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.bihealth.mi.easysmpc.resources.Resources;

/**
 * Dialog for entering a string
 * 
 * @author Felix Wirth
 * @author Fabian Prasser
 */
public class DialogStringPicker extends JDialog implements ChangeListener {

    /** SVID */
    private static final long serialVersionUID = -2803385597185044215L;
    /** Component to enter string */
    private ComponentTextArea text;
    /** Result */
    private String            result;
    /** Button*/
    private JButton           buttonOK;
        
    /**
     * Create a new instance
     * @param parent Component to set the location of JDialog relative to
     * @param additionalAction  Action which will be performed when clicking the okButton
     */
    public DialogStringPicker(String textDefault, ComponentTextFieldValidator validator, JFrame parent) {

        // Dialog properties
        this.setSize(Resources.SIZE_DIALOG_SMALL_X, Resources.SIZE_DIALOG_SMALL_Y);
        this.setTitle(Resources.getString("PerspectiveParticipate.PickerTitle"));
        this.getContentPane().setLayout(new BorderLayout());
        this.setIconImage(parent.getIconImage());
        
        // Title
        ((JComponent) this.getContentPane()).setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED),
                                                                                        Resources.getString("PerspectiveParticipate.PickerText"),
                                                                                        TitledBorder.CENTER,
                                                                                        TitledBorder.DEFAULT_POSITION));
        
        // Text
        this.text = new ComponentTextArea(textDefault == null ? "" : textDefault, validator);
        this.text.setChangeListener(this);
        this.add(text, BorderLayout.CENTER);
        
        // Button
        JPanel buttonsPane = new JPanel();
        buttonsPane.setLayout(new GridLayout(1, 2));
        this.getContentPane().add(buttonsPane, BorderLayout.SOUTH);
        this.buttonOK = new JButton(Resources.getString("PerspectiveParticipate.ok"));
        this.buttonOK.setEnabled(this.areValuesValid());
        JButton buttonCancel = new JButton(Resources.getString("PerspectiveParticipate.cancel"));
        buttonsPane.add(buttonCancel);
        buttonsPane.add(buttonOK);
        
        // Listeners
        this.buttonOK.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                actionProceed();
            }
        });
        buttonCancel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                actionCancel();
            }
        });
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent windowEvent) {
                DialogStringPicker.this.result = null;
            }
        });
        
        // Add shortcut key for escape
        JPanel dialogPanel = (JPanel) getContentPane();
        dialogPanel.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT)
                   .put(KeyStroke.getKeyStroke("ESCAPE"), "cancel");
        dialogPanel.getActionMap().put("cancel", new AbstractAction() {
            /** SVUID */
            private static final long serialVersionUID = -5809172959090943313L;

            @Override
            public void actionPerformed(ActionEvent e) {
                actionCancel();
            }
        });
        
        // Set location
        this.setLocationRelativeTo(parent);
    }

    /**
     * Show this dialog
     */
    public String showDialog(){
        this.setModal(true);
        this.setVisible(true);
        return this.result;
    }
    
    /**
     * Reacts to changes
     */
    @Override
    public void stateChanged(ChangeEvent e) {
        this.buttonOK.setEnabled(this.areValuesValid());
    }
      
    /**
     * Action cancel and close
     */
    private void actionCancel() {
        this.result = null;
        this.dispose();
    }

    /**
     * Action proceed and close
     */
    private void actionProceed() {
        this.result = text.getText();
        this.dispose();
    }

    /**
     * Checks string for validity
     * @return
     */
    private boolean areValuesValid() {
        return this.text.isValueValid();
    } 
}
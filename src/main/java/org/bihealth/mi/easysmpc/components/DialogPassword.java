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
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
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
public class DialogPassword extends JDialog implements ChangeListener {

    /** SVUID */
    private static final long         serialVersionUID = 2844321619003751907L;
    /** Component to enter IMAP password */
    private ComponentEntryOne         passwordEntryIMAP;
    /** Result */
    private String                    result;
    /** Button */
    private JButton                   buttonOK;
    /** Parent */
    private JFrame                    parent;
    /** Component to enter SMTP password */
    private ComponentEntryOneCheckBox passwordEntrySMTP;
        
    /**
     * Create a new instance
     * 
     * @param textDefault
     * @param validator
     * @param parent
     */
    public DialogPassword(JFrame parent) {
        
        // Store
        this.parent = parent;

        // Dialog properties        
        this.setTitle(Resources.getString("EmailConfig.24"));
        this.getContentPane().setLayout(new BorderLayout());
        this.setIconImage(parent.getIconImage());
        this.setResizable(false);
        
        // Border
        ((JComponent) this.getContentPane()).setBorder(BorderFactory.createEmptyBorder(Resources.ROW_GAP_LARGE + 1, Resources.ROW_GAP_LARGE + 1, Resources.ROW_GAP_LARGE + 1, Resources.ROW_GAP_LARGE + 1));
        
        // Password disclaimer 
        this.add(new JLabel(Resources.getString("EmailConfig.25")), BorderLayout.NORTH);
        
        // Create central panel 
        JPanel central = new JPanel(); 
        central.setLayout(new BoxLayout(central, BoxLayout.Y_AXIS));
        this.add(central, BorderLayout.CENTER);
        
        // IMAP panel
        JPanel imapPanel = new JPanel();
        imapPanel.setLayout(new BorderLayout());
        imapPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED),
                                                                      Resources.getString("EmailConfig.29"),
                                                                      TitledBorder.LEFT,
                                                                      TitledBorder.DEFAULT_POSITION));
        central.add(imapPanel);
        
        // IMAP Password entry
        this.passwordEntryIMAP = new ComponentEntryOne(Resources.getString("EmailConfig.33"),
                                                   "",
                                                   true,
                                                   new ComponentTextFieldValidator() {
                                                       @Override
                                                       public boolean validate(String text) {
                                                           if (text == null ||
                                                               text.equals("")) { return false; }
                                                           return true;
                                                       }
                                                   },
                                                   true,
                                                   false);
        imapPanel.add(passwordEntryIMAP, BorderLayout.CENTER);
        passwordEntryIMAP.setChangeListener(this);
        
        // SMTP panel
        JPanel smtpPanel = new JPanel();
        smtpPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED),
                                                                      Resources.getString("EmailConfig.31"),
                                                                      TitledBorder.LEFT,
                                                                      TitledBorder.DEFAULT_POSITION));
        smtpPanel.setLayout(new BorderLayout());
        central.add(smtpPanel);
        
        // SMTP Password entry
        smtpPanel.add(new JLabel(Resources.getString("EmailConfig.33")), BorderLayout.WEST);
        this.passwordEntrySMTP = new ComponentEntryOneCheckBox(null, new ComponentTextFieldValidator() {
            @Override
            public boolean validate(String text) {
                if (text == null ||
                    text.equals("")) { return false; }
                return true;
            }
        }, true, true);
        passwordEntrySMTP.setChangeListener(this);
        smtpPanel.add(passwordEntrySMTP, BorderLayout.CENTER);
        
        // Buttons 
        JPanel buttonsPane = new JPanel();
        buttonsPane.setLayout(new GridLayout(1, 2));
        this.buttonOK = new JButton(Resources.getString("PerspectiveParticipate.ok"));
        JButton buttonCancel = new JButton(Resources.getString("EmailConfig.7"));
        buttonsPane.add(buttonCancel);
        buttonsPane.add(buttonOK);
        this.add(buttonsPane, BorderLayout.SOUTH);
        this.stateChanged(new ChangeEvent(this));
        
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
                DialogPassword.this.result = null;
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
        
        // Add shortcut key for enter
        dialogPanel.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT)
                   .put(KeyStroke.getKeyStroke("ENTER"), "proceed");
        dialogPanel.getActionMap().put("proceed", new AbstractAction() {

            /** SVUID */
            private static final long serialVersionUID = -4085624272147282716L;

            @Override
            public void actionPerformed(ActionEvent e) {
                buttonOK.doClick();
            }
        });
        
        // Pack and set location
        this.pack();
        this.setLocationRelativeTo(parent);
    }

    /**
     * Show this dialog
     */
    public String showDialog(){
        this.setModal(true);
        this.setLocationRelativeTo(this.parent);
        this.setVisible(true);
        return this.result;
    }

    /**
     * Reacts to changes
     */
    @Override
    public void stateChanged(ChangeEvent e) {
        if (this.buttonOK != null) {
            this.buttonOK.setEnabled(this.areValuesValid());
        }
    }
    
    /**
     * Action cancel and close
     */
    private void actionCancel() {
        this.result = null;
        this.dispose();
    }
      
    /**
     * Checks string for validity
     * @return
     */
    private boolean areValuesValid() {
        return this.passwordEntryIMAP.isValueValid() && this.passwordEntrySMTP.isValueValid();
    }
    
    /**
     * Action proceed and close
     */
    protected void actionProceed() {
        this.result = passwordEntryIMAP.getValue();
        this.dispose();
    }
}
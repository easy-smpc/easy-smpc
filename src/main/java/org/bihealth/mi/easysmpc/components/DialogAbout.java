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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextPane;
import javax.swing.SwingConstants;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import org.bihealth.mi.easysmpc.App;
import org.bihealth.mi.easysmpc.resources.Resources;

/**
 * An about dialog.
 * @author Felix Wirth
 *
 */

public class DialogAbout extends JDialog {
    
    /** SVID */
    private static final long serialVersionUID = -3124059494844509921L;

    /**
     * Create a new instance
     * @param parent Component to set the location of JDialog relative to
     */
    public DialogAbout(JFrame parent) {
        
        this.setTitle(Resources.getString("About.AboutTitle"));
        this.getContentPane().setLayout(new BorderLayout());
        this.setIconImage(parent.getIconImage());
        JPanel central = new JPanel();

        // Version
        central.setLayout(new BorderLayout());
        central.add(new JLabel(String.format(Resources.getString("About.NameWithVersion"), App.VERSION), SwingConstants.CENTER), BorderLayout.NORTH);

        // Load license
        String license = null;
        try {
            license = Resources.getLicenseText();
        } catch (Exception e) {
            // Ignore
        }
        
        // Add license
        if (license != null) {
            JTextPane pane = new JTextPane();
            pane.setText("\n" + license);
            pane.setEditable(false);
            StyledDocument doc = pane.getStyledDocument();
            SimpleAttributeSet center = new SimpleAttributeSet();
            StyleConstants.setAlignment(center, StyleConstants.ALIGN_CENTER);
            doc.setParagraphAttributes(0, doc.getLength(), center, false);
            central.add(pane, BorderLayout.CENTER);
        }
        
        // Authors
        central.add(new JLabel(Resources.getString("About.Contributors"), SwingConstants.CENTER), BorderLayout.SOUTH);
        this.getContentPane().add(central, BorderLayout.CENTER);

        // Buttons
        JPanel buttonPane = new JPanel();
        buttonPane.setLayout(new BorderLayout());
        this.getContentPane().add(buttonPane, BorderLayout.SOUTH);
        JButton buttonOK = new JButton(Resources.getString("About.ok"));
        buttonPane.add(buttonOK, BorderLayout.CENTER);
        buttonOK.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                DialogAbout.this.dispose();
            }
        });
        this.setSize(Resources.SIZE_DIALOG_X, Resources.SIZE_DIALOG_Y);
        this.setLocationRelativeTo(parent);
        this.setModal(true);
        this.setResizable(false);
        this.setVisible(true);
    }
}

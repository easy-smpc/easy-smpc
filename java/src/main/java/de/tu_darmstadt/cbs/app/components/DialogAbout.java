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

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;

import de.tu_darmstadt.cbs.app.resources.Resources;

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
    public DialogAbout(JFrame parent)
    {
        this.setTitle(Resources.getString("About.AboutTitle"));
        this.getContentPane().setLayout(new BorderLayout());
        this.setIconImage(parent.getIconImage());
        JPanel central = new JPanel();

        // Texts
        central.setLayout(new BorderLayout());
        central.add(new JPanel().add(new ComponentTextAreaNoEntry(Resources.getLicenseText(), this)), BorderLayout.CENTER);
        central.add(new JPanel().add(new ComponentTextAreaNoEntry(Resources.getString("About.Contributors"), this)), BorderLayout.SOUTH);
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
        this.setVisible(true);
    }
}

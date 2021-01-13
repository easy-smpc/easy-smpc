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
package org.bihealth.mi.easysmpc;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JPanel;

import org.bihealth.mi.easysmpc.resources.Resources;

/**
 * A perspective
 * 
 * @author Fabian Prasser
 */

public class Perspective0Start extends Perspective {
    
    /**
     * Creates the perspective
     * @param app
     */
    protected Perspective0Start(App app) {
        super(app, Resources.getString("PerspectiveStart.0"), 0, false); //$NON-NLS-1$
    }

    /**
     * Creates and adds UI elements
     */
    @Override
    protected void createContents(JPanel panel) {

        // Buttons panel
        JPanel buttons = new JPanel();
        buttons.setLayout(new GridLayout(3, 1));

        // Action 1
        JButton button1 = new JButton(Resources.getString("App.7")); //$NON-NLS-1$
        buttons.add(button1);
        button1.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                getApp().actionCreate();
            }
        });

        // Action 2
        JButton button2 = new JButton(Resources.getString("App.8")); //$NON-NLS-1$
        buttons.add(button2);
        button2.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                getApp().actionParticipate();
            }
        });

        // Action 3
        JButton button3 = new JButton(Resources.getString("App.9")); //$NON-NLS-1$
        buttons.add(button3);
        button3.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                getApp().actionLoad();
            }
        });

        // Add
        panel.setLayout(new GridBagLayout());
        panel.add(buttons, new GridBagConstraints());
    }

    @Override
    protected void initialize() {
    }
}

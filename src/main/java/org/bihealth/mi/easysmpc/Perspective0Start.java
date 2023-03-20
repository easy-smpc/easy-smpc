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
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.bihealth.mi.easybus.implementations.local.ConnectionSettingsManual;
import org.bihealth.mi.easysmpc.resources.Resources;

/**
 * A perspective
 * 
 * @author Felix Wirth
 * @author Fabian Prasser
 */
public class Perspective0Start extends Perspective implements ChangeListener {
    
    /** Button 3 */
    JButton button3;
    /** Button 4 */
    JButton button4;
    
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
        buttons.setLayout(new GridLayout(5, 1));


        
        // Action 1
        JButton button1 = new JButton(Resources.getString("App.26")); //$NON-NLS-1$
        button1.addChangeListener(this);
        buttons.add(button1);
        button1.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                getApp().actionSetConnection();
            }
        });

        // Action 2
        JButton button2 = new JButton(Resources.getString("App.9")); //$NON-NLS-1$
        button2.addChangeListener(this);
        buttons.add(button2);
        button2.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                getApp().actionLoad();
            }
        });
        
        buttons.add(new JLabel(""));
        
        // Action 3
        button3 = new JButton(Resources.getString("App.7")); //$NON-NLS-1$
        button3.setEnabled(false);
        buttons.add(button3);
        button3.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                getApp().actionCreate();
            }
        });
        
        // Action 4
        button4 = new JButton(Resources.getString("App.8")); //$NON-NLS-1$
        button4.setEnabled(false);
        buttons.add(button4);
        button4.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (getApp().getConnectionSettings() instanceof ConnectionSettingsManual) {
                    getApp().actionParticipateManual();
                } else {
                    getApp().actionParticipateBackend();
                }
            }
        });
        
        
        // Add
        panel.setLayout(new GridBagLayout());
        panel.add(buttons, new GridBagConstraints());
    }

    @Override
    protected void initialize() {
        // Empty by design
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        if(getApp().getConnectionSettings() != null) {
            button3.setEnabled(true);
            button4.setEnabled(true);
        } else {
            button3.setEnabled(false);
            button4.setEnabled(false);
        }
    }
}

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
import java.awt.Color;
import java.io.IOException;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import org.bihealth.mi.easysmpc.resources.Resources;

/**
 * Displays a loading visualization and a respective text
 * 
 * @author Felix Wirth
 *
 */
public class ComponentLoadingVisual extends JPanel {

    /** SVUID */
    private static final long serialVersionUID = 6439753947234873179L;
    /** Text to be displayed if loading is in progress*/
    private String textLoading;
    /** Text to be displayed if loading stopped as an error*/
    private String textError;
    /** Text label */
    private JLabel captionLabel;
    /** Label holding animation */
    private JPanel centerPanel;
    /** Image icon */
    private ImageIcon imageIcon;
    
    /**
     * Creates a new instance
     * @throws IOException 
     */
    public ComponentLoadingVisual(String textLoading, String textError) throws IOException{
        // Initalize
        this.textLoading = textLoading;
        this.textError = textError;
        this.imageIcon = Resources.getLoadingAnimation();
        
        // Create layout and elements
        this.setLayout(new BorderLayout());
        this.centerPanel = new JPanel();
        this.captionLabel = new JLabel("", SwingConstants.CENTER);
        
        // Add
        this.add(centerPanel, BorderLayout.CENTER);
        this.add(this.captionLabel, BorderLayout.SOUTH);
    }
     
    /**
     * Sets the component to display loading is in progress
     */
    public void setLoadingProgress() {
        this.centerPanel.removeAll();
        this.centerPanel.add(new JLabel(this.imageIcon));
        this.captionLabel.setText(textLoading);
        this.captionLabel.setForeground(Color.BLACK);
        this.revalidate();
        this.repaint();
    }
    
    /**
     * Sets the component to display loading is in progress
     * @throws IOException 
     */
    public void setLoadingError() throws IOException {
        this.centerPanel.removeAll();
        this.captionLabel.setText(textError);
        this.captionLabel.setForeground(Color.RED);
        this.revalidate();
        this.repaint();
    }
}

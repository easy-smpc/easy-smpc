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
import java.awt.Image;
import java.io.IOException;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.bihealth.mi.easysmpc.resources.Resources;

/**
 * Entry for participants in the histogram
 * 
 * @author Fabian Prasser
 * @author Felix Wirth
 */
public class EntryParticipantCheckmark extends EntryParticipant {
    
    /** SVID*/
    private static final long serialVersionUID = -8287188327564633383L;

    /** label for checkmark */
    private JLabel imageLabel;    
    
    /**
     * Creates a new instance
     * @param name
     * @param value
     */
    public EntryParticipantCheckmark(String name, String value){
        super(name, value, false, true);
    }

    /**
     * Set the checkmark enabled
     */
    public void setCheckmarkEnabled(boolean enabled){
        imageLabel.setEnabled(enabled);
        imageLabel.repaint();
        imageLabel.revalidate();
    }  
    
    /**
     * Creates and additional control panel
     */
    @Override
    protected JPanel createAdditionalControls() {
        JPanel panel = new JPanel();
        try {
            Image imageScaled = Resources.getCheckmark();
            imageLabel = new JLabel();
            imageLabel.setIcon(new ImageIcon(imageScaled));
            panel.add(imageLabel, BorderLayout.EAST);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, Resources.getString("Participant.3"), Resources.getString("App.13"), JOptionPane.ERROR_MESSAGE);
        }
        return panel;
    }   
}

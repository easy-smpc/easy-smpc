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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.BevelBorder;

import de.tu_darmstadt.cbs.app.resources.Resources;

/**
 * A progress bar for the app
 * 
 * @author Fabian Prasser
 */
public class ComponentProgress extends JPanel {

    /** SVUID */
    private static final long    serialVersionUID = -7413394015204651280L;

    /** Transparent color */
    private static final Color[] COLORS           = new Color[] { Color.decode("#F7D8BA"),
                                                                  Color.decode("#FFE7C7"),
                                                                  Color.decode("#FEF8DD"),
                                                                  Color.decode("#E1F8DC"),
                                                                  Color.decode("#CAF1DE"),
                                                                  Color.decode("#ACDDDE") };

    /** Text areas */
    private JLabel[]          texts            = new JLabel[6];
    
    /**
     * Creates a new instance
     * @param progress
     */
    public ComponentProgress(int progress) {
        
        // Create texts
        this.setLayout(new GridLayout(1, this.texts.length));
        for (int i = 0; i < this.texts.length; i++) {
            
            // Text pane with horizontally centered text
            this.texts[i] = new JLabel();
            this.texts[i].setFocusable(false);
            this.texts[i].setBackground(new Color(255, 255, 255, 0));
            this.texts[i].setForeground(Color.BLACK);
            this.texts[i].setText(Resources.getString("Progress." + i));
            this.texts[i].setFont(this.texts[i].getFont().deriveFont(Font.BOLD));
            
            // Center vertically
            JPanel panel = new JPanel();
            panel.setBackground(COLORS[i]);
            panel.setLayout(new GridBagLayout());
            panel.add(this.texts[i]);
            panel.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
            this.add(panel);
        }
        
        // Init
        this.setProgress(progress);
    }
    
    @Override
    public Dimension getPreferredSize() {
        return new Dimension(super.getPreferredSize().width, Resources.PROGRESS_PREFERRED_HEIGHT);
    }

    /**
     * Progress
     * @param progress
     */
    public void setProgress(int progress) {
        if (progress < 0 || progress > 6) {
            throw new IllegalArgumentException("Progress must be in [0, 6]");
        }
        int index = progress == 0 ? -1 : progress - 1;
        for (int i = 0; i < this.texts.length; i++) {
            this.texts[i].setForeground(i == index ? Color.WHITE : Color.BLACK);
            this.texts[i].getParent().setBackground(i == index ? Color.BLACK : COLORS[i]);
            this.texts[i].getParent().invalidate();
            this.texts[i].invalidate();
            this.texts[i].getParent().repaint();
            this.texts[i].repaint();
        }
    }
}

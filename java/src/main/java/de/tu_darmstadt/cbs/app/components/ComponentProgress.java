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
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Image;
import java.io.IOException;

import javax.swing.JPanel;
import javax.swing.JTextPane;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import de.tu_darmstadt.cbs.app.resources.Resources;

/**
 * A progress bar for the app
 * 
 * @author Fabian Prasser
 */
public class ComponentProgress extends JPanel {

    /** SVUID */
    private static final long    serialVersionUID = -7413394015204651280L;

    /** Images to use by the control */
    private static final Image[] images;

    /** Progress */
    private int                  progress         = 0;

    /** Text areas */
    private JTextPane[]          texts            = new JTextPane[6];

    static {
        try {
            images = new Image[] { Resources.getProgress(0),
                                   Resources.getProgress(1),
                                   Resources.getProgress(2),
                                   Resources.getProgress(3),
                                   Resources.getProgress(4),
                                   Resources.getProgress(5),
                                   Resources.getProgress(6) };
        } catch (IOException e) {
            throw new RuntimeException("Problem initializing progress control");
        }
    }
    
    /**
     * Creates a new instance
     * @param progress
     */
    public ComponentProgress(int progress) {
        this.setProgress(progress);
        this.setLayout(new GridLayout(1, this.texts.length));
        for (int i = 0; i < this.texts.length; i++) {
            this.texts[i] = new JTextPane();
            this.texts[i].setFocusable(false);
            this.texts[i].setBackground(new Color(0, 0, 0, 0));
            this.texts[i].setForeground(Color.WHITE);
            this.texts[i].setText(Resources.getProgressText(i));
            this.texts[i].setFont(this.texts[i].getFont().deriveFont(Font.BOLD));
            StyledDocument doc = this.texts[i].getStyledDocument();
            SimpleAttributeSet center = new SimpleAttributeSet();
            StyleConstants.setAlignment(center, StyleConstants.ALIGN_CENTER);
            doc.setParagraphAttributes(0, doc.getLength(), center, false);
            this.add(this.texts[i]);
        }
    }
    
    /**
     * Progress
     * @param progress
     */
    public void setProgress(int progress) {
        if (progress < 0 || progress > 6) {
            throw new IllegalArgumentException("Progress must be in [0, 6]");
        }
        this.progress = progress;
        this.repaint();
    }

    @Override
    public void paintComponent(Graphics p) {
        super.paintComponent(p);
        p.drawImage(images[progress], 0, 0, this.getWidth(), this.getHeight(), this);
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(super.getPreferredSize().width, Resources.PROGRESS_PREFERRED_HEIGHT);
    }
}

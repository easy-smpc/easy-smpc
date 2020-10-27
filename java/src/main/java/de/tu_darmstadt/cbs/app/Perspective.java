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
package de.tu_darmstadt.cbs.app;

import java.awt.Component;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

/**
 * Implements a perspective in the GUI
 * 
 * @author Fabian Prasser
 */
public abstract class Perspective {

    /** App */
    private final App    app;
    /** Title */
    private final String title;
    /** Progress*/
    private final int progress;
    /** Panel*/
    private final JPanel panel;

    /**
     * Creates a new instance
     * 
     * @param app
     * @param title
     * @param progress
     */
    protected Perspective(App app, String title, int progress) {
        this.app = app;
        this.title = title;
        this.progress = progress;
        this.panel = new JPanel();
        this.panel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED),
                                                              title,
                                                              TitledBorder.CENTER,
                                                              TitledBorder.DEFAULT_POSITION));
        this.createContents(panel);
    }

    /**
     * Returns the app
     * 
     * @return
     */
    public App getApp() {
        return this.app;
    }

    /**
     * The actual content
     * 
     * @return
     */
    public Component getPanel() {
        return panel;
    }

    /**
     * Returns the title
     * 
     * @return
     */
    public String getTitle() {
        return this.title;
    }

    /**
     * Creates the contents
     * 
     * @param panel
     */
    protected abstract void createContents(JPanel panel);
    
    /**
     * Returns the progress associated with this perspective
     * @return
     */
    protected int getProgress() {
        return this.progress;
    }
    
    /**
     * Initialize perspective based on model
     */
    protected abstract void initialize();
}

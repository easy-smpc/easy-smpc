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

import java.awt.Component;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

/**
 * Implements a perspective in the GUI
 * 
 * @author Felix Wirth
 * @author Fabian Prasser
 */
public abstract class Perspective {

    /** App */
    private final App    app;
    /** Title */
    private final String title;
    /** Progress */
    private final int    progress;
    /** Panel */
    private final JPanel panel;
    /** Is interim saving in this perspective possible */
    private boolean      canSave = false;
    /** Can the password of the user be reset?*/
    private boolean canReenterPassword = false;
    

    /**
     * Creates a new instance
     * 
     * @param app
     * @param title
     * @param progress
     * @param canSave
     */
    protected Perspective(App app, String title, int progress, boolean canSave) {
        this(app, title, progress, canSave, false);
    }

    /**
     * Creates a new instance
     * 
     * @param app
     * @param title
     * @param progress
     * @param canSave
     * @param canReenterPassword
     */
    protected Perspective(App app,
                          String title,
                          int progress,
                          boolean canSave,
                          boolean canReenterPassword) {
        
        this.app = app;
        this.title = title;
        this.progress = progress;
        this.canSave = canSave;
        this.canReenterPassword  = canReenterPassword;
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
     * Is perspective visible
     */
    public boolean isVisible() {
        return this.panel.isVisible();
    }

    /**
     * Does perspective allow reset the password
     */
    protected boolean canReenterPassword() {
        return canReenterPassword;
    }
    
    /**
     * Does perspective allow interim saving
     */
    protected boolean canSave() {
        return canSave;
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
    
    /**
     * Call when before other perspective is shown
     */
    protected void uninitialize() {
        // Stop the bus for automatic processing if running
        if (getApp().getModel() != null) {
            getApp().getModel().stopBus();
        }      
        
        // Reset status message and loading visual
        getApp().setStatusMessage("", false);
        getApp().stopAnimation();
    }
}

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

    /**
     * Creates a new instance
     * 
     * @param app
     * @param title
     */
    protected Perspective(App app, String title) {
        this.app = app;
        this.title = title;
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
     * Returns the title
     * 
     * @return
     */
    public String getTitle() {
        return this.title;
    }

    /**
     * The actual content
     * 
     * @return
     */
    public Component getPanel() {
        JPanel panel = new JPanel();
        panel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED),
                                                         title,
                                                         TitledBorder.CENTER,
                                                         TitledBorder.DEFAULT_POSITION));
        this.createContents(panel);
        return panel;
    }

    /**
     * Creates the contents
     * 
     * @param panel
     */
    protected abstract void createContents(JPanel panel);
}

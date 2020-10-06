package de.tu_darmstadt.cbs.app;

import java.awt.Image;
import java.io.IOException;
import java.io.InputStream;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

/**
 * Accessor class for messages used by the UI
 * 
 * @author Fabian Prasser
 */
public class Resources {

    /** Bundle name */
    private static final String         BUNDLE_NAME     = "de.tu_darmstadt.cbs.app.messages";   //$NON-NLS-1$

    /** Bundle */
    private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle(BUNDLE_NAME);

    /**
     * No instantiation
     */
    private Resources() {
        // Empty by design
    }

    /**
     * Returns a message
     * 
     * @param key
     * @return
     */
    public static String getString(String key) {
        try {
            return RESOURCE_BUNDLE.getString(key);
        } catch (MissingResourceException e) {
            return '!' + key + '!';
        }
    }

    /**
     * Icon
     * 
     * @return
     * @throws IOException
     */
    public static Image getIcon() throws IOException {
        InputStream stream = Resources.class.getResourceAsStream("icon.png");
        return ImageIO.read(stream);
    }

    /**
     * Menu item
     * 
     * @return
     * @throws IOException
     */
    public static ImageIcon getMenuItem() throws IOException {
        InputStream stream = Resources.class.getResourceAsStream("icon.png");
        return new ImageIcon(ImageIO.read(stream).getScaledInstance(16, 16, Image.SCALE_DEFAULT));
    }
}

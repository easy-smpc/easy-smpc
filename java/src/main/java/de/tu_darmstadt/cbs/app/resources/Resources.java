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
package de.tu_darmstadt.cbs.app.resources;

import java.awt.Color;
import java.awt.Image;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.UIManager;
import javax.swing.border.Border;

/**
 * Accessor class for messages and settings used by the UI
 * 
 * @author Fabian Prasser
 */
public class Resources {

    /** Invalid border */
    public static final Border          INVALID_BORDER            = BorderFactory.createLineBorder(new Color(255, 69, 0));

    /** Default border */
    public static final Border          DEFAULT_BORDER            = UIManager.getLookAndFeel().getDefaults().getBorder("TextField.border");

    /** Row gap */
    public static final int             ROW_GAP                   = 2;

    /** Min x-size of a text area */
    public static final int             SIZE_TEXTAREA_X           = 400;

    /** Min y-size of a text area */
    public static final int             SIZE_TEXTAREA_Y           = 200;
    
    /** String indicating start of exchange string */
    public static final String          exchangeStringStartTag              = "BEGIN_PAYLOAD:";
    
    /** String indicating end of exchange string */
    public static final String          exchangeStringEndTag              = ":END_PAYLOAD";
    
    /** Char length for exchange before line break */
    public static final String          exchangeStringNewLineCountChar              = "150";
    
    /** Bundle name */
    private static final String         BUNDLE_NAME               = "de.tu_darmstadt.cbs.app.resources.messages"; //$NON-NLS-1$

    /** Bundle */
    private static final ResourceBundle RESOURCE_BUNDLE           = ResourceBundle.getBundle(BUNDLE_NAME);

    /** Preferred height for the progress container */
    public static final int             PROGRESS_PREFERRED_HEIGHT = 50;

    /** Ending for project files */
    public static final String          FILE_ENDING               = "smpc";
    
    /** About dialog size x*/
    public static final int SIZE_DIALOG_X = 500;
    
    /** About dialog size y*/
    public static final int SIZE_DIALOG_Y = 300;
    
    /** The charset used to read the license text */
    private final static Charset        CHARSET         = StandardCharsets.UTF_8;

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
     * Reads the content from the file license.txt located in the package
     * org.deidentifier.arx.gui.resources and returns the content as string.
     * 
     * @return
     */
    public static String getLicenseText() {
        InputStream stream = Resources.class.getResourceAsStream("license.txt"); //$NON-NLS-1$
        BufferedReader br = new BufferedReader(new InputStreamReader(stream, CHARSET));
        String content = ""; //$NON-NLS-1$
        try {
            StringBuilder sb = new StringBuilder();
            String line;
            line = br.readLine();
            while (line != null) {
                sb.append(line);
                sb.append(System.lineSeparator());
                line = br.readLine();
            }
            content = sb.toString();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                br.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return content;
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
     * Icon
     * 
     * @return
     * @throws IOException
     */
    public static Image getCheckmark() throws IOException {
        InputStream stream = Resources.class.getResourceAsStream("checkmark.png");
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

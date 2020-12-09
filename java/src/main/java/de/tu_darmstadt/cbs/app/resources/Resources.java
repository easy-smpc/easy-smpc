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
import java.util.Locale;
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

    /** Bundle name */
    private static final String         BUNDLE_NAME               = "de.tu_darmstadt.cbs.app.resources.messages"; //$NON-NLS-1$

    /** Bundle */
    private static ResourceBundle resource_bundle           = ResourceBundle.getBundle(BUNDLE_NAME);

    /** Invalid border */
    public static final Border          INVALID_BORDER            = BorderFactory.createLineBorder(new Color(255, 69, 0));

    /** Default border */
    public static final Border          DEFAULT_BORDER            = UIManager.getLookAndFeel().getDefaults().getBorder("TextField.border"); //$NON-NLS-1$

    /** Row gap */
    public static final int             ROW_GAP                   = 2;

    /** Min x-size of a text area */
    public static final int             SIZE_TEXTAREA_X           = 400;

    /** Min y-size of a text area */
    public static final int             SIZE_TEXTAREA_Y           = 200;

    /** String indicating start of exchange string */
    public static final String          MESSAGE_START_TAG         = "BEGIN_PAYLOAD"; //$NON-NLS-1$

    /** String indicating end of exchange string */
    public static final String          MESSAGE_END_TAG           = "END_PAYLOAD"; //$NON-NLS-1$

    /** Char length for exchange before line break */
    public static final int             MESSAGE_LINE_WIDTH        = 150;

    /** Preferred height for the progress container */
    public static final int             PROGRESS_PREFERRED_HEIGHT = 50;

    /** Ending for project files */
    public static final String          FILE_ENDING               = "smpc"; //$NON-NLS-1$

    /** About dialog size x */
    public static final int             SIZE_DIALOG_X             = 500;

    /** About dialog size y */
    public static final int             SIZE_DIALOG_Y             = 300;

    /** The charset used to read the license text */
    private final static Charset        CHARSET                   = StandardCharsets.UTF_8;

    /** Size of checkmark clipart x */
    public static final int             SIZE_CHECKMARK_X          = 15;

    /** Size of checkmark clipart y */
    public static final int             SIZE_CHECKMARK_Y          = 12;
    /** Available languages */
    private static final Locale[] AVAILABLE_LANGUAGES       = { Locale.ENGLISH, Locale.GERMAN };
    /** Interval schedule for tasks in background */
    public static final long      INTERVAL_SCHEDULER_SECONDS = 3;
    /** File ending for CSV-files */
    public static final String    FILE_ENDING_CSV            = "csv";
    /** File ending for Excel-2007-files */
    public static final String    FILE_ENDING_EXCEL_XLSX     = "xlsx";
    /** Maximal rows in Excel 2007 format */
    public static final int       MAX_COUNT_ROWS_EXCEL       = 1048576;
    /** Maximal columns in Excel 2007 format */
    public static final int       MAX_COUNT_COLUMN_EXCEL     = 16384;
    /** Standard delimiter for CSV files */
    public static final String[]  DELIMITERS                 = { ";", ",", "|" };

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
            return resource_bundle.getString(key);
        } catch (MissingResourceException e) {
            return '!' + key + '!';
        }
    }
    
    /**
     * Get locale of resource bundle
     * 
     * @return
     */
    public static Locale getResourceBundleLocale(){
        return resource_bundle.getLocale();
    }
    
    /**
     * Set locale of resource bundle
     * 
     * @return
     */
    public static void setResourceBundleLocale(Locale locale)
    {
        Locale.setDefault(Locale.ENGLISH);
        resource_bundle = ResourceBundle.getBundle(BUNDLE_NAME, locale);
    }
    
    /**
     * Returns all available languages
     * 
     * @return
     */
    public static Locale[] getAvailableLanguages()
    {
        return AVAILABLE_LANGUAGES;
    }
    
    /**
     * Reads the content from the file license.txt and returns the content as string.
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
        return ImageIO.read(stream).getScaledInstance(Resources.SIZE_CHECKMARK_X, Resources.SIZE_CHECKMARK_Y,  java.awt.Image.SCALE_SMOOTH);
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

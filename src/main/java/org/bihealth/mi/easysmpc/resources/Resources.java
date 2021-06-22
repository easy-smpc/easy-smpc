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
package org.bihealth.mi.easysmpc.resources;

import java.awt.Color;
import java.awt.Image;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
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
 * @author Felix Wirth
 * @author Fabian Prasser
 */
public class Resources {

    /** Bundle name */
    private static final String   BUNDLE_NAME                         = "org.bihealth.mi.easysmpc.resources.messages"; //$NON-NLS-1$

    /** Bundle */
    private static ResourceBundle resource_bundle                     = ResourceBundle.getBundle(BUNDLE_NAME);

    /** Invalid border */
    public static final Border    INVALID_BORDER                      = BorderFactory.createLineBorder(new Color(255, 69, 0));

    /** Default border */
    public static final Border    DEFAULT_BORDER                      = UIManager.getLookAndFeel().getDefaults().getBorder("TextField.border");       //$NON-NLS-1$

    /** Row gap */
    public static final int       ROW_GAP                             = 2;
    
    /** Row gap large */
    public static final int       ROW_GAP_LARGE                       = 5;

    /** X-size of small dialog */
    public static final int       SIZE_DIALOG_SMALL_X                 = 450;

    /** Y-size of small dialog */
    public static final int       SIZE_DIALOG_SMALL_Y                 = 185;

    /** String indicating start of exchange string */
    public static final String    MESSAGE_START_TAG                   = "BEGIN_PAYLOAD";                               //$NON-NLS-1$

    /** String indicating end of exchange string */
    public static final String    MESSAGE_END_TAG                     = "END_PAYLOAD";                                 //$NON-NLS-1$

    /** Char length for exchange before line break */
    public static final int       MESSAGE_LINE_WIDTH                  = 150;

    /** Preferred height for the progress container */
    public static final int       PROGRESS_PREFERRED_HEIGHT           = 50;

    /** Ending for project files */
    public static final String    FILE_ENDING                         = "smpc";                                        //$NON-NLS-1$

    /** About dialog size x */
    public static final int       SIZE_DIALOG_X                       = 500;

    /** About dialog size y */
    public static final int       SIZE_DIALOG_Y                       = 300;

    /** The charset used to read the license text */
    private final static Charset  CHARSET                             = StandardCharsets.UTF_8;
    
    /** Size of loading animation */
    public static final int       SIZE_LOADING_ANIMATION              = 15;
    
    /** Size of checkmark clipart x */
    public static final int       SIZE_CHECKMARK_X                    = 15;

    /** Size of checkmark clipart y */
    public static final int       SIZE_CHECKMARK_Y                    = 12;
    /** Available languages */
    private static final Locale[] AVAILABLE_LANGUAGES                 = { Locale.ENGLISH, Locale.GERMAN };
    
    /** Interval schedule for tasks in background */
    public static final long      INTERVAL_SCHEDULER_MILLISECONDS     = 200;

    /** File ending for CSV-files */
    public static final String    FILE_ENDING_CSV                     = "csv";                                         //$NON-NLS-1$

    /** File ending for Excel-2007-files */
    public static final String    FILE_ENDING_EXCEL_XLSX              = "xlsx";                                        //$NON-NLS-1$

    /** File ending for Excel-97-files */
    public static final String    FILE_ENDING_EXCEL_XLS               = "xls";                                         //$NON-NLS-1$

    /** Maximal rows considered */
    public static final int       MAX_COUNT_ROWS                      = 250000;

    /** Maximal columns considered */
    public static final int       MAX_COUNT_COLUMNS                   = 250000;

    /** Delimiters considered for CSV files */
    public static final char[]    DELIMITERS                          = { ';', ',', '|', '\t' };                       // $NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$

    /** Maximum number of lines to be loaded for preview purposes for CSV file detection. */
    public static final int       PREVIEW_MAX_LINES                   = 25;

    /** Maximum number of chars to be loaded for detecting separators for CSV file detection. */
    public static final int       DETECT_MAX_CHARS                    = 100000;

    /** Interval to check mail box automatically */
    public static final int       INTERVAL_CHECK_MAILBOX_MILLISECONDS = 30000;
    
    /** Fetch size for messages with IMAP */
    public static final int FETCH_SIZE_IMAP = 1048576;
    
    /** Step 1 identifier */
    public static final String    ROUND_1                             = "_round1";

    /** Step 2 identifier */
    public static final String    ROUND_2                             = "_round2";
    
    /** Light green color */
    public static final Color COLOR_LIGHT_GREEN = new Color(82, 153, 75);
    
    /** Interval to check existing mailbox connection */
    public static final int       INTERVAL_CHECK_MAILBOX_CONNECTED = 3000;

    /** Fractional bits for decimal values */
    public static final int FRACTIONAL_BITS = 32;
        
    /**
     * Returns all available languages
     * 
     * @return
     */
    public static Locale[] getAvailableLanguages() {
        return AVAILABLE_LANGUAGES;
    }
    
    /**
     * Icon
     * 
     * @return
     * @throws IOException
     */
    public static Image getCheckmark() throws IOException {
        InputStream stream = Resources.class.getResourceAsStream("checkmark.png"); //$NON-NLS-1$
        return ImageIO.read(stream).getScaledInstance(Resources.SIZE_CHECKMARK_X, Resources.SIZE_CHECKMARK_Y,  java.awt.Image.SCALE_SMOOTH);
    }

    /**
     * Icon
     * 
     * @return
     * @throws IOException
     */
    public static Image getIcon() throws IOException {
        InputStream stream = Resources.class.getResourceAsStream("icon.png"); //$NON-NLS-1$
        return ImageIO.read(stream);
    }
    
    /**
     * Loading animation
     * 
     * @return
     * @throws IOException
     */
    public static ImageIcon getLoadingAnimation() throws IOException {        
        URL url = Resources.class.getResource("loading.gif"); //$NON-NLS-1$
        Image image = new ImageIcon(url).getImage().getScaledInstance(SIZE_LOADING_ANIMATION, SIZE_LOADING_ANIMATION, Image.SCALE_DEFAULT);
        return new ImageIcon(image);
      }
    
    /**
     * Reads the content from the file license.txt and returns the content as string.
     * 
     * @return
     * @throws IOException 
     */
    public static String getLicenseText() throws IOException {
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
            throw e;
        } finally {
            try {
                br.close();
            } catch (IOException e) {
                throw e;
            }
        }
        return content;
    }
    
    /**
     * Menu item
     * 
     * @return
     * @throws IOException
     */
    public static ImageIcon getMenuItem() throws IOException {
        InputStream stream = Resources.class.getResourceAsStream("icon.png"); //$NON-NLS-1$
        return new ImageIcon(ImageIO.read(stream).getScaledInstance(16, 16, Image.SCALE_DEFAULT));
    }
    
    /**
     * Get locale of resource bundle
     * 
     * @return
     */
    public static Locale getResourceBundleLocale() {
        return resource_bundle.getLocale();
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
     * Set locale of resource bundle
     * 
     * @return
     */
    public static void setResourceBundleLocale(Locale locale) {
        Locale.setDefault(Locale.ENGLISH);
        resource_bundle = ResourceBundle.getBundle(BUNDLE_NAME, locale);
    }
    
    /**
     * No instantiation
     */
    private Resources() {
        // Empty by design
    }
}

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

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.bihealth.mi.easysmpc.components.ComponentProgress;
import org.bihealth.mi.easysmpc.components.ComponentTextFieldValidator;
import org.bihealth.mi.easysmpc.components.DialogAbout;
import org.bihealth.mi.easysmpc.components.DialogStringPicker;
import org.bihealth.mi.easysmpc.dataimport.ImportClipboard;
import org.bihealth.mi.easysmpc.dataimport.ImportFile;
import org.bihealth.mi.easysmpc.resources.Resources;

import com.formdev.flatlaf.FlatLightLaf;

import de.tu_darmstadt.cbs.emailsmpc.AppModel;
import de.tu_darmstadt.cbs.emailsmpc.AppState;
import de.tu_darmstadt.cbs.emailsmpc.Bin;
import de.tu_darmstadt.cbs.emailsmpc.InitialMessage;
import de.tu_darmstadt.cbs.emailsmpc.Message;
import de.tu_darmstadt.cbs.emailsmpc.Participant;

/**
 * Main UI of the app
 * 
 * @author Felix Wirth
 * @author Fabian Prasser
 */
public class App extends JFrame {

    /** SVUID */
    private static final long serialVersionUID = 8047583915796168387L;
        
    /**
     * Main entry point
     * 
     * @throws UnsupportedLookAndFeelException
     * @throws IllegalAccessException
     * @throws InstantiationException
     * @throws ClassNotFoundException
     * @throws IOException
     */
    public static void main(String[] args) throws ClassNotFoundException,
                                           InstantiationException,
                                           IllegalAccessException,
                                           UnsupportedLookAndFeelException,
                                           IOException {
        try {
            UIManager.setLookAndFeel( new FlatLightLaf() ); // OR FlatDarculaLaf
        } catch( Exception ex ) {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        }
               
        // Start App
        new App();
    }

    /** Model */
    private AppModel          model;

    /** Cards */
    private JPanel            cards;
    /** Menu */
    private JMenu             actionMenu;
    /** Progress*/
    private ComponentProgress progress;
    /** List of perspectives */
    private List<Perspective> perspectives = new ArrayList<Perspective>();
    /** Interim save menu item */
    private JMenuItem jmiInterimSave;

    /**
     * Creates a new instance
     * 
     * @throws IOException
     */
    public App() throws IOException {

        // Title
        super(Resources.getString("App.0")); //$NON-NLS-1$

        // Settings
        this.setSize(800, 600);
        this.setLocationRelativeTo(null);
        this.setIconImage(Resources.getIcon());
        this.setLayout(new BorderLayout());
        this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);// Close only if user clicks yes in dialog
        
        // Progress
        this.progress = new ComponentProgress(0);
        this.add(this.progress, BorderLayout.NORTH);
        
        // Panels
        this.cards = new JPanel(new CardLayout());
        this.add(this.cards, BorderLayout.CENTER);
        
        // Intercept close event
        this.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                actionExit();
            }
        });     

        // Menu
        JMenuBar jmb = new JMenuBar();
        this.setJMenuBar(jmb);

        // Action menu
        actionMenu = new JMenu(Resources.getString("App.1")); //$NON-NLS-1$
        jmb.add(actionMenu);

        // Start
        JMenuItem jmiStart = new JMenuItem(Resources.getString("App.14"), Resources.getMenuItem()); //$NON-NLS-1$
        actionMenu.add(jmiStart);
        jmiStart.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                actionStart();
            }
        });

        // Create
        JMenuItem jmiCreate = new JMenuItem(Resources.getString("App.7"), Resources.getMenuItem()); //$NON-NLS-1$
        actionMenu.add(jmiCreate);
        jmiCreate.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                actionCreate();
            }
        });

        // Participate
        JMenuItem jmiParticipate = new JMenuItem(Resources.getString("App.8"), Resources.getMenuItem()); //$NON-NLS-1$
        actionMenu.add(jmiParticipate);
        jmiParticipate.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                actionParticipate();
            }
        });        
        
        // Load
        JMenuItem jmiLoad = new JMenuItem(Resources.getString("App.9"), Resources.getMenuItem()); //$NON-NLS-1$
        actionMenu.add(jmiLoad);
        jmiLoad.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                actionLoad();
            }
        });
        
        // Interim save
        jmiInterimSave = new JMenuItem(Resources.getString("App.16"), Resources.getMenuItem()); //$NON-NLS-1$
        actionMenu.add(jmiInterimSave);
        jmiInterimSave.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                actionSave();
            }
        });
        
        // Exit menu
        JMenuItem jmiExit = new JMenuItem(Resources.getString("App.4"), Resources.getMenuItem()); //$NON-NLS-1$
        actionMenu.add(jmiExit);
        jmiExit.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                actionExit();
            }
        });
        
        // Help menu
        JMenu jmHelp = new JMenu(Resources.getString("App.2")); //$NON-NLS-1$
        jmb.add(jmHelp);
        
        // Change language
        JMenuItem jmiChangeLanguage = new JMenuItem(Resources.getString("App.17"), Resources.getMenuItem()); //$NON-NLS-1$
        jmHelp.add(jmiChangeLanguage);
        jmiChangeLanguage.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                actionChangeLanguage();
            }
        });
        
        // About menu
        JMenuItem jmiAbout = new JMenuItem(Resources.getString("App.3"), Resources.getMenuItem()); //$NON-NLS-1$
        jmHelp.add(jmiAbout);
        jmiAbout.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                actionAbout();
            }
        });       

        // Add perspectives
        addPerspective(new Perspective6Result(this));
        addPerspective(new Perspective5Receive(this));
        addPerspective(new Perspective4Send(this));
        addPerspective(new Perspective3Receive(this));
        addPerspective(new Perspective2Send(this));
        addPerspective(new Perspective1BParticipate(this));
        addPerspective(new Perspective1ACreate(this));
        addPerspective(new Perspective0Start(this));
        
        // Show the first perspective
        showPerspective(0);
             
        // Finally, make the frame visible
        this.setVisible(true);
    }

    /**
     * Reads data from a file
     * @return List of data
     */
    public Map<String, String> getDataFromFile() {               
        // Get file
        File file = getFile(true, new FileNameExtensionFilter(Resources.getString("PerspectiveCreate.ExcelFileDescription"), Resources.FILE_ENDING_EXCEL_XLSX), 
                                  new FileNameExtensionFilter(Resources.getString("PerspectiveCreate.ExcelFileDescription97"), Resources.FILE_ENDING_EXCEL_XLS),
                                  new FileNameExtensionFilter(Resources.getString("PerspectiveCreate.CSVFileDescription"), Resources.FILE_ENDING_CSV));
        
        if (file != null) {
            try {
                return ImportFile.forFile(file).getExtractedData();       
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, Resources.getString("PerspectiveCreate.LoadFromFileError"), Resources.getString("App.11"), JOptionPane.ERROR_MESSAGE); //$NON-NLS-1$               
            }
            catch (IllegalArgumentException e) {
                JOptionPane.showMessageDialog(this, Resources.getString("PerspectiveCreate.LoadDataError"), Resources.getString("App.11"),  JOptionPane.ERROR_MESSAGE); //$NON-NLS-1$               
            }
        }
        return null;
    }
    
    /**
     * Opens a file chooser
     * @param load 
     * @param fileNameExtensionFilter
     * @return
     */
    public File getFile(boolean load, FileNameExtensionFilter... filters) {
        // Prepare
        JFileChooser fileChooser = new JFileChooser();
        
        // Set possible file filters
        for (int i = 0; i < filters.length; i++) {
            fileChooser.addChoosableFileFilter(filters[i]);
        }
        
        // Set default file filters
        if (filters.length > 0) {
            fileChooser.setFileFilter(filters[0]);
        }
        
        // Open or save dialog
        int state = load ? fileChooser.showOpenDialog(this) : fileChooser.showSaveDialog(this);
        // Check file
        if (state != JFileChooser.APPROVE_OPTION) {
            return null;
            }
        File file = fileChooser.getSelectedFile();
        
        // Fix extension on save
        if (!load) {
            String fname = file.getAbsolutePath();
            if(!fname.endsWith("." + Resources.FILE_ENDING) ) {
                file = new File(fname + ("." + Resources.FILE_ENDING));
            }
        }
        
        // Check permissions
        if ((load && !file.canRead()) || (file.exists() && !load && !file.canWrite())) {
            JOptionPane.showMessageDialog(this, Resources.getString("App.12"), Resources.getString("App.13"), JOptionPane.ERROR_MESSAGE); //$NON-NLS-1$ //$NON-NLS-2$
            return null;
        }
        
        // Should work
        return file;
    }    
    
    /**
     * Get model state
     * 
     * @return AppState
     */
    public AppState getModelState() {
        if (getModel() != null) {
            return getModel().state;
        } else {
            return null;
        }
    }
    
    /**
     * Check whether message is valid
     * 
     * @param text
     * @return
     */
    public boolean isMessageShareResultValid(String text) {
        if (model == null || text == null || text.trim().isEmpty()) {
            return false;
            }
        try {
            return model.isMessageShareResultValid(Message.deserializeMessage(text));
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Set message share (message in perspectives receive)
     * 
     * @param message
     */
    public void setMessageShare(String message) {
        AppModel snapshot = this.beginTransaction();        
        try {
            this.model.setShareFromMessage(Message.deserializeMessage(message));
        } catch (IllegalStateException | IllegalArgumentException | NoSuchAlgorithmException | ClassNotFoundException | IOException e) {
            this.rollback(snapshot);
            JOptionPane.showMessageDialog(this, Resources.getString("PerspectiveReceive.messageError"), Resources.getString("PerspectiveReceive.messageErrorTitle"), JOptionPane.ERROR_MESSAGE);
        }        
    }
    
    /**
     * Adds a new perspective
     * 
     * @param perspective
     * @throws IOException
     */
    private void addPerspective(Perspective perspective) throws IOException {

        perspectives.add(0, perspective);
        cards.add(perspective.getPanel(), perspective.getTitle());
    }

    /**
     * Starts a transaction
     * @return
     */
    private AppModel beginTransaction() {
        return this.model != null ? (AppModel)this.model.clone() : null;
    }

    /**
     * Check whether initial participation message is valid
     * 
     * @param text
     * @return
     */
    private boolean isInitialParticipationMessageValid(String text) {
        if (text == null) {
            return false;
        }
        try {
            String data =  Message.deserializeMessage(text).data;
            InitialMessage.getAppModel(InitialMessage.decodeMessage(Message.getMessageData(data)));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Rolls back a transaction
     * @param snapshot
     */
    private void rollback(AppModel snapshot) {
        if (this.model != null && snapshot != null) {
            this.model.update(snapshot);
        }
    }
    
    /**
     * Shows a perspective
     * 
     * @param clazz
     */
    private void showPerspective(Class<?> clazz) {
        int index = 0;
        for (Perspective p : perspectives) {
            if (p.getClass().equals(clazz)) {
                showPerspective(index);
            }
            index++;
        }
    }

    /**
     * Shows the perspective with the given index
     * 
     * @param index
     */
    private void showPerspective(int index) {
        showPerspective(perspectives.get(index));
    }
    
    /**
     * Shows a certain perspective
     * 
     * @param perspective
     */
    private void showPerspective(Perspective perspective) {
        perspective.initialize();
        CardLayout cl = (CardLayout) (cards.getLayout());
        cl.show(cards, perspective.getTitle());
        progress.setProgress(perspective.getProgress());
        jmiInterimSave.setEnabled(perspective.isInterimSavingPossible());
        progress.repaint();
    }

    /**
     * Shows the about dialog
     */
    protected void actionAbout() {
        new DialogAbout(this);
    }

    /**
     * Change the language
     */
    protected void actionChangeLanguage() {
        // Prepare
        Locale newLocale;
        Locale oldLocale = Resources.getResourceBundleLocale();        
        
        // Get new locale
        newLocale = (Locale) JOptionPane.showInputDialog(this,
                                                      Resources.getString("App.18"),
                                                      Resources.getString("App.17"),
                                                      JOptionPane.QUESTION_MESSAGE,
                                                      null,
                                                      Resources.getAvailableLanguages(),
                                                      Resources.getAvailableLanguages()[0]);
        // Confirm restart
        if (!oldLocale.equals(newLocale) && newLocale != null && JOptionPane.showConfirmDialog(this,
                                                            Resources.getString("App.20"), //$NON-NLS-1$
                                                            Resources.getString("App.19"), //$NON-NLS-1$
                                                            JOptionPane.YES_NO_OPTION,
                                                            JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION) {
            try {
                Resources.setResourceBundleLocale(newLocale);
                new App();
                dispose();
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, Resources.getString("App.21") , Resources.getString("App.19"), JOptionPane.ERROR_MESSAGE);
                Resources.setResourceBundleLocale(oldLocale);
            }
        }
    }

    /**
     * Create action
     */
    protected void actionCreate() {
        AppModel snapshot = this.beginTransaction();
        try {
            this.model = new AppModel();
            this.model.toStarting();
        } catch (IllegalStateException | IOException e) {
            this.rollback(snapshot);
            JOptionPane.showMessageDialog(this, Resources.getString("App.15"), Resources.getString("App.22"), JOptionPane.ERROR_MESSAGE);
            return;
        }
        this.showPerspective(Perspective1ACreate.class);
    }

    /**
     * Called when action create is done
     * @param participants
     * @param bins
     */
    protected void actionCreateDone(String title, Participant[] participants, Bin[] bins) {

        // Pass over bins and participants
        AppModel snapshot = this.beginTransaction();
        try {
            model.toInitialSending(title, participants, bins);
            if (actionSave()) {
                this.showPerspective(Perspective2Send.class);
            } else {
                this.rollback(snapshot);
            }
        } catch (IllegalStateException | IOException e) {
            this.rollback(snapshot);
            JOptionPane.showMessageDialog(this, Resources.getString("App.15"), Resources.getString("App.22"), JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Shows the exit dialog
     */
    protected void actionExit() {
        if (JOptionPane.showConfirmDialog(this, 
                                          Resources.getString("App.5"), Resources.getString("App.6"),  //$NON-NLS-1$ //$NON-NLS-2$
                                          JOptionPane.YES_NO_OPTION,
                                          JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION){
                                          System.exit(0);
                                      }
    }
    
    /**
     * Action performed when first receiving done
     */
    protected void actionFirstReceivingDone() {
        AppModel snapshot = this.beginTransaction();
        try {
            this.model.toSendingResult();
            this.model.saveProgram();
            this.showPerspective(Perspective4Send.class);
        } catch (Exception e) {
            this.rollback(snapshot);    
            JOptionPane.showMessageDialog(this, Resources.getString("PerspectiveReceive.saveError"),Resources.getString("App.13"), JOptionPane.ERROR_MESSAGE);
        }        
    }

    /**
     * First sending done
     */
    protected void actionFirstSendingDone() {
        AppModel snapshot = this.beginTransaction();
        try {
            this.model.toRecievingShares();
            this.model.saveProgram();
        } catch (IllegalStateException | IOException e) {
            this.rollback(snapshot);      
            JOptionPane.showMessageDialog(this, Resources.getString("PerspectiveSend.saveError"),Resources.getString("App.13"), JOptionPane.ERROR_MESSAGE);
            return;
        }
        this.showPerspective(Perspective3Receive.class);
    }

    /**
     * Load action
     */
    protected void actionLoad() {       
        // Open dialog
        File file = getFile(true, new FileNameExtensionFilter(Resources.getString("App.10"), Resources.FILE_ENDING));
        
        // Check
        if (file == null) {
            return;
        }

        // Load file
        try {
            this.model = AppModel.loadModel(file);
        } catch (Exception e) {
            this.model = null;
            JOptionPane.showMessageDialog(this, Resources.getString("App.11"), Resources.getString("App.13"), JOptionPane.ERROR_MESSAGE ); //$NON-NLS-1$
            return;
        }
        
        // Set and switch to correct perspective        
        switch (this.model.state) {
        case NONE:
            showPerspective(Perspective0Start.class);
            break;
        case STARTING:
            showPerspective(Perspective1ACreate.class);
            break;
        case PARTICIPATING:
            showPerspective(Perspective1BParticipate.class);
            break;
        case INITIAL_SENDING:
            showPerspective(Perspective2Send.class);
            break;
        case ENTERING_VALUES:
            showPerspective(Perspective1BParticipate.class);
            break;
        case SENDING_SHARE:
            showPerspective(Perspective2Send.class);
            break;
        case RECIEVING_SHARE:
            showPerspective(Perspective3Receive.class);
            break;
        case SENDING_RESULT:
            showPerspective(Perspective4Send.class);
            break;
        case RECIEVING_RESULT:
            showPerspective(Perspective5Receive.class);
            break;
        case FINISHED:
            showPerspective(Perspective6Result.class);
            break;
        }
    }

    /**
     * Marks a message as sent
     * @param index
     */
    protected void actionMarkMessageSent(int index) {
        this.model.markMessageSent(index);
    }

    /**
     * Participate action
     */
    protected void actionParticipate() {
         // Try to get string from clip board
        String clipboardText = ImportClipboard.getStrippedExchangeMessage(ImportClipboard.getTextFromClipBoard());
        clipboardText = isInitialParticipationMessageValid(clipboardText) ? clipboardText : null;
        
        // Ask for string
        String message = new DialogStringPicker(clipboardText, new ComponentTextFieldValidator() {
            @Override
            public boolean validate(String text) {
                return isInitialParticipationMessageValid(ImportClipboard.getStrippedExchangeMessage(text));
            }
        }, this).showDialog();
        
        // If valid string provided
        if (message != null) {
            message = ImportClipboard.getStrippedExchangeMessage(message); 
            // Initialize
            try {
                String data = Message.deserializeMessage(message).data;
                this.model = InitialMessage.getAppModel(InitialMessage.decodeMessage(Message.getMessageData(data)));
                this.model.toEnteringValues(data);
                this.showPerspective(Perspective1BParticipate.class);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, Resources.getString("PerspectiveParticipate.stringError"), Resources.getString("PerspectiveParticipate.stringErrorTitle"), JOptionPane.ERROR_MESSAGE);
                this.model = null;
            }
        }
    }

    /**
     * Action called when done with participating
     * @param secret
     */
    protected void actionParticipateDone(BigInteger[] secret) {

        // Pass over bins and participants
        AppModel snapshot = this.beginTransaction();
        try {
            model.toSendingShares(secret);
            if (actionSave()) {
                this.showPerspective(Perspective2Send.class);
            } else {
                this.rollback(snapshot);
            }
        } catch (IOException | IllegalStateException | IllegalArgumentException e) {
            this.rollback(snapshot);
            JOptionPane.showMessageDialog(this, Resources.getString("App.15"), Resources.getString("App.22"), JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Action to receive a message
     * 
     * @return
     */
    protected void actionReceiveMessage() {       
       // Try to get string from clip board
        String clipboardText = ImportClipboard.getStrippedExchangeMessage(ImportClipboard.getTextFromClipBoard());
        clipboardText = isMessageShareResultValid(clipboardText) ? clipboardText : null;

        // Ask for message
        String message = new DialogStringPicker(clipboardText, new ComponentTextFieldValidator() {
            @Override
            public boolean validate(String text) {
                return isMessageShareResultValid(ImportClipboard.getStrippedExchangeMessage(text));
                }
            }, this).showDialog();           
            
        // If message selected
        if (message != null) {
            message = ImportClipboard.getStrippedExchangeMessage(message);
            setMessageShare(ImportClipboard.getStrippedExchangeMessage(message));           
        }  
    }
    
    /**
     * Saves the project
     */
    protected boolean actionSave() {
        
        if (model.filename == null) {    
            // Open dialog
            File file = getFile(false, new FileNameExtensionFilter(Resources.getString("App.10"), Resources.FILE_ENDING));

            // Check
            if (file == null) { return false; }
            model.filename = file;
        }
        // Try to save file
        AppModel snapshot = this.beginTransaction();
        try {           
            model.saveProgram();
            return true;
        } catch (IOException e) {
            this.rollback(snapshot);
            JOptionPane.showMessageDialog(this, Resources.getString("PerspectiveCreate.saveError"),Resources.getString("App.13"), JOptionPane.ERROR_MESSAGE); //$NON-NLS-1$
            return false;
        }
    }

    /**
     * Action performed when second receiving done
     */
    protected void actionSecondReceivingDone() {
        AppModel snapshot = this.beginTransaction();
        try {
            this.model.toFinished();
            this.model.saveProgram();
        } catch (Exception e) {
            this.rollback(snapshot);
            JOptionPane.showMessageDialog(this, Resources.getString("PerspectiveReceive.saveError"), Resources.getString("App.13"), JOptionPane.ERROR_MESSAGE);
        }
        this.showPerspective(Perspective6Result.class);
    }

    /**
     * Second sending done
     */
    protected void actionSecondSendingDone() {
        AppModel snapshot = this.beginTransaction();
        try {
            this.model.toRecievingResult();
            this.model.saveProgram();
        } catch (IOException e) {
            this.rollback(snapshot);
            JOptionPane.showMessageDialog(this, Resources.getString("PerspectiveSend.saveError"),Resources.getString("App.13"), JOptionPane.ERROR_MESSAGE);
            return;
        }
        this.showPerspective(Perspective5Receive.class);
    }
    
    /**
     * Start action
     */
    protected void actionStart() {
        this.showPerspective(Perspective0Start.class);
    }
    
    /**
     * Returns the model
     * @return
     */
    protected AppModel getModel() {
        return this.model;
    }    
    
    /**
     * Returns a perspective
     * 
     * @param clazz
     * @return Found perspective - null if not found!
     */
    protected Perspective getPerspective(Class<?> clazz) {
        Perspective returnPerspective = null;
        for (Perspective p : perspectives) {
            if (p.getClass().equals(clazz)) {
                returnPerspective = p;
                break;
            }
        }
        return returnPerspective;
    }
}
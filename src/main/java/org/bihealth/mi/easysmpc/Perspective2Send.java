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
import java.awt.Component;
import java.awt.Desktop;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.FutureTask;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.ProgressMonitor;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.apache.http.client.utils.URIBuilder;
import org.bihealth.mi.easybus.ConnectionSettings;
import org.bihealth.mi.easybus.ConnectionSettings.ExchangeMode;
import org.bihealth.mi.easybus.Scope;
import org.bihealth.mi.easysmpc.components.ComponentTextField;
import org.bihealth.mi.easysmpc.components.EntryParticipantCheckmarkSendMail;
import org.bihealth.mi.easysmpc.components.ScrollablePanel;
import org.bihealth.mi.easysmpc.resources.Resources;

import de.tu_darmstadt.cbs.emailsmpc.Message;
import de.tu_darmstadt.cbs.emailsmpc.Participant;
import de.tu_darmstadt.cbs.emailsmpc.Study.StudyState;

/**
 * A perspective
 * 
 * @author Fabian Prasser
 * @author Felix Wirth
 */
public class Perspective2Send extends Perspective implements ChangeListener {
    
    /** Panel for participants */
    private ScrollablePanel    panelParticipants;

    /** Text field containing title of study */
    private ComponentTextField fieldTitle;

    /** Proceed button */
    protected JButton            buttonProceed;

    /** Send button manual */
    private JButton            buttonSendAllManually;

    /** Send button automatic */
    private JButton            buttonSendAllAutomatically;

    /** Buttons pane */
    private JPanel             buttonsPane;

    /**
     * Creates the perspective
     * @param app
     */
    protected Perspective2Send(App app) {
        super(app, Resources.getString("PerspectiveSend.send"), 2, true, true); //$NON-NLS-1$
    }
    
    /**
     * Creates the perspective
     * @param app
     * @param progress
     */
    protected Perspective2Send(App app, int progress) {
        super(app, Resources.getString("PerspectiveSend.send"), progress, true, true); //$NON-NLS-1$
    }
    
    /**
     * Creates the perspective
     * @param app
     * @param progress
     */
    protected Perspective2Send(App app, String title , int progress) {
        super(app, title, progress, true, true); //$NON-NLS-1$
    }
    
    /**
      * Reacts on all changes in any components
      */
     @Override
     public void stateChanged(ChangeEvent e) {
         // Update checkmarks
         updateCheckmarks();
         
         // Check click able send all mails button and save button
         boolean allMessagesRetrieved = getApp().getModel().areAllMessagesRetrieved();
         this.buttonProceed.setEnabled(allMessagesRetrieved);
        
        // If no more messages and automatic processing proceed automatically
        if (allMessagesRetrieved && isAutomaticProcessingEnabled()) {
            actionProceed();
        }
     }
    
    /**
     * @param entry
     */
   private void actionCopyButton(EntryParticipantCheckmarkSendMail entry) {
        // Push email body into clipboard
        try {
            String body = generateEMailBody(entry, generateFormatedExchangeString(entry));
            
            // Fill clipboard. Do this only if getExchangeString() was successful to avoid overwriting the users clipboard with nothing
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(body), null);
            
            // Send a dialog to confirm copying
            if (!wasMessageretrieved(entry) && JOptionPane.showConfirmDialog(this.getPanel(), String.format(Resources.getString("PerspectiveSend.confirmSendCopyGeneric")), "", JOptionPane.OK_CANCEL_OPTION) == 0) {
                int index = Arrays.asList(this.panelParticipants.getComponents()).indexOf(entry);
                getApp().actionMarkMessageRetrieved(index);
                
                // Persist changes
                getApp().actionSave();
                
                // Update status
                this.stateChanged(new ChangeEvent(this));
            }
            
        } catch (IOException exception) {
            JOptionPane.showMessageDialog(null,Resources.getString("PerspectiveSend.copyToClipboardError"), Resources.getString("PerspectiveSend.copyToClipboardErrorTitle"), JOptionPane.ERROR_MESSAGE);
        }        
    }
    
    
    /**
     * Sends an message to the participant entry automatically
     * 
     * @param list
     */
    private void actionSendMessageAutomatically(List<EntryParticipantCheckmarkSendMail> list) {
        
        // Deactivate buttons at start: Will be re-enabled if needed by the thread spawned below
        buttonSendAllAutomatically.setEnabled(false);
        buttonSendAllManually.setEnabled(false);
        for (Component c : this.panelParticipants.getComponents()) {
            ((EntryParticipantCheckmarkSendMail) c).setButtonEnabled(false);
        }
        
        // Create progress monitor
        ProgressMonitor monitor = new ProgressMonitor(Perspective2Send.this.getPanel(), 
                                                      Resources.getString("PerspectiveSend.ProgressTitle"),
                                                      Resources.getString("PerspectiveSend.ProgressNote"),
                                                      0, list.size());
        
        // Timing
        monitor.setMillisToDecideToPopup(0);
        monitor.setMillisToPopup(0);
        monitor.setProgress(1);
        
        // Create async task
        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            /** Has message been sent */
            boolean messageSent = false;
            /** Did an error occurred */
            boolean error = false;
            
            @Override
            public void done() {
                // Persist changes
                if (messageSent) {
                    getApp().actionSave();
                }

                // Display error message if applicable
                if (error) {
                    
                    // Ask to change settings
                    if (JOptionPane.showConfirmDialog(getPanel(),
                                                      String.format(Resources.getString("PerspectiveSend.sendAutomaticError")),
                                                      "",
                                                      JOptionPane.OK_CANCEL_OPTION) == JOptionPane.YES_OPTION) {
                        
                        // Get new settings
                        ConnectionSettings newSettings = getApp().editExchangeConf(getApp().getModel().getConnectionSettings());
                        
                        // Use new settings if given
                        if (newSettings != null) {
                            // Set settings
                            getApp().getModel().setConnectionSettings(newSettings);

                            // Stop bus and restart send
                            SwingUtilities.invokeLater(new Runnable() {

                                @Override
                                public void run() {
                                    getApp().getModel().stopBus();
                                    actionSendMessageAutomatically(list);
                                }
                            });

                        }
                    }
                }
                
                // Finalize
                monitor.setProgress(list.size());                
                wrapUp();
            }

            /**
             * Finalizes after the worker thread
             */
            private void wrapUp() {
                // Re-activate buttons
                buttonSendAllAutomatically.setEnabled(true);
                buttonSendAllManually.setEnabled(true);
                for (Component c : panelParticipants.getComponents()) {
                    if (!isOwnEntry(c)) {
                        ((EntryParticipantCheckmarkSendMail) c).setButtonEnabled(true);
                    }
                }                
                stateChanged(new ChangeEvent(this));
            }
            
            @Override
            protected Void doInBackground() throws Exception {

                // Init loop
                int workDone = 0;

                // Loop over messages
                for (EntryParticipantCheckmarkSendMail entry : list) {
                    
                    // Stop if cancel button hit
                    if(monitor.isCanceled()) {
                        wrapUp();
                        break;
                    }
                    
                    // Init
                    FutureTask<Void> future;

                    if (isInitialSending() && getApp().getModel().getExchangeMode() == ExchangeMode.EMAIL) {
                        // Send plain message
                        future = getApp().getModel()
                                         .getBus()
                                         .sendPlain(entry.getRightValue(),
                                                    generateEMailSubject(),
                                                    generateEMailBody(entry,
                                                                      generateFormatedExchangeString(entry)));
                    } else {
                     // Send message in bus mode
                        future = getApp().getModel()
                                         .getBus()
                                         .send(getExchangeString(entry),
                                               new Scope(getApp().getModel().getStudyUID() + (isInitialSending() ? Resources.ROUND_0 : getRoundIdentifier())),
                                               new org.bihealth.mi.easybus.Participant(entry.getLeftValue(),
                                                                                       entry.getRightValue()));

                    }

                    try {
                        
                        // Wait for result with a timeout
                        long endTime = System.currentTimeMillis() + getApp().getModel().getConnectionSettings().getSendTimeout();
                        while (true){
                            if(future.isDone()) {
                                break;
                            }
                            if(monitor.isCanceled() || endTime < System.currentTimeMillis()) {
                                throw new InterruptedException();
                            }
                        }
                        
                        // Mark message sent
                        int index = Arrays.asList(panelParticipants.getComponents()).indexOf(entry);
                        getApp().actionMarkMessageRetrieved(index);
                        messageSent = true;
                    } catch (Exception e) {
                        future.cancel(true);
                        error = !monitor.isCanceled();
                    }
                    this.setProgress((++workDone / list.size()) * 100);
                }              

                // Done
                return null;
            }
        };
        
        // Add change listener to worker
        worker.addPropertyChangeListener(new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent evt) {

                // Set progress
                if (evt.getPropertyName().equals("progress")) {
                    monitor.setProgress((((Integer) evt.getNewValue()) / 100) * list.size());
                }
            }
        });

        // Start worker
        worker.execute();
    }

    /**
     * Sends an e-mail to the participant entry
     * 
     * @param list
     */
    private void actionSendMailManual(List<EntryParticipantCheckmarkSendMail> list) {
        
        try {
            
            // For each entry
            for (EntryParticipantCheckmarkSendMail entry : list) {              
                
                // Build URI
                URIBuilder builder = new URIBuilder().setScheme("mailto");
                builder.setPath(entry.getRightValue()).addParameter("subject", generateEMailSubject())
                                                      .addParameter("body",
                                                                    generateEMailBody(entry, generateFormatedExchangeString(entry)));
                
                // Open email
                Desktop.getDesktop().mail(new URI(builder.toString().replace("+", "%20").replace(":/", ":")));
            }
            
            // Check at lest one message not marked as retrieved
            boolean retrieved = true;
            for (EntryParticipantCheckmarkSendMail entry : list) {
                if(!wasMessageretrieved(entry)) {
                    retrieved = false;
                    break;
                }
            }
            
            // Send a dialog to confirm mail sending
            if (!retrieved && JOptionPane.showConfirmDialog(this.getPanel(), String.format(Resources.getString("PerspectiveSend.confirmSendMailGeneric")), "", JOptionPane.OK_CANCEL_OPTION) == 0) {
                
                for (EntryParticipantCheckmarkSendMail entry : list) {
                    int index = Arrays.asList(this.panelParticipants.getComponents()).indexOf(entry);
                    getApp().actionMarkMessageRetrieved(index);
                }
                
                // Persist changes
                getApp().actionSave();
            }

        } catch (IOException | URISyntaxException e) {
            JOptionPane.showMessageDialog(this.getPanel(), Resources.getString("PerspectiveSend.mailToError"), Resources.getString("PerspectiveSend.mailToErrorTitle"), JOptionPane.ERROR_MESSAGE);
        }
        this.stateChanged(new ChangeEvent(this));
    }

    /**
     * List all participants with unsent messages
     * 
     * @return list of participants
     */
    private List<EntryParticipantCheckmarkSendMail> getAllParticipants() {
        List<EntryParticipantCheckmarkSendMail> list = new ArrayList<>();
        for (Component c : panelParticipants.getComponents()) {
            if (!isOwnEntry(c)) {
                list.add((EntryParticipantCheckmarkSendMail)  c);
            }
        }
        return list;
    }

    /**
     * Returns the exchange string for the given entry
     * 
     * @param entry
     * @throws IOException
     */
    private String getExchangeString(EntryParticipantCheckmarkSendMail entry) throws IOException {
        int index = Arrays.asList(panelParticipants.getComponents()).indexOf(entry);
        return Message.serializeMessage(getApp().getModel().getUnsentMessageFor(index));
    }

    /**
     * Indicates whether the automatic processing enabled
     * 
     * @return enabled
     */
    private boolean isAutomaticProcessingEnabled() {
        return getApp().getModel().isAutomatedMode();
    }
    
     /**
     * Indicates whether it is the initial sending of the creator
     * 
     * @return enabled
     */
    private boolean isInitialSending() {
        return getApp().getModelState() == StudyState.INITIAL_SENDING;
    }
    
    /**
      * Returns whether this is the own entry
      * @param entry
      * @return
      */
    private boolean isOwnEntry(Component entry) {
        return Arrays.asList(panelParticipants.getComponents()).indexOf(entry) == getApp().getModel().getOwnId();
    }
     
    /**
     * Draws the buttons pane with or without the automatic resend button
     * 
     * @param showResend
     */
    private void updateButtonsPane(boolean showResend) {
        
        // Remove
        this.buttonsPane.removeAll();
        
        // Create with two or three rows and add resend button if necessary
        if (showResend) {
            this.buttonsPane.setLayout(new GridLayout(3, 1));
            this.buttonsPane.add(this.buttonSendAllAutomatically, 0, 0);
            this.buttonsPane.add(this.buttonSendAllManually, 0, 1);
            this.buttonsPane.add(this.buttonProceed, 0, 2);
            
        } else {
            this.buttonsPane.setLayout(new GridLayout(2, 1));
            this.buttonsPane.add(this.buttonSendAllManually, 0, 0);
            this.buttonsPane.add(this.buttonProceed, 0, 1);
        }
    }
    
     /**
     * Check participant entries visually if complete
     */
    private void updateCheckmarks() {
        int index = 0;
        for (Component c : this.panelParticipants.getComponents()) {
            EntryParticipantCheckmarkSendMail entry = (EntryParticipantCheckmarkSendMail) c;
            entry.setCheckmarkEnabled(index == getApp().getModel().getOwnId() || // Always mark own id as "received"
                                                                wasMessageretrieved(entry)); // Mark if share complete
            index++;
        }
    }
    
    /**
     * Returns whether a message was already retrieved
     * @param entry
     * @return
     */
   private boolean wasMessageretrieved(Component entry) {
       return getApp().getModel().wasMessageRetrieved(Arrays.asList(panelParticipants.getComponents()).indexOf(entry));                     
   }

    /**
     * Proceed action
     */
    protected void actionProceed() {
        // Prevent a second click on proceed button
        buttonProceed.setEnabled(false);
        
        // Mark all messages as sent
        markAllMessagesSent();
        
        // Execute action
        getApp().actionFirstSendingDone();
        
        // Re-enable proceed button
        buttonProceed.setEnabled(true);
    }

    /**
     *Creates and adds UI elements
     */
    @Override
    protected void createContents(JPanel panel) {

        // Layout
        panel.setLayout(new BorderLayout());

        // General data data of study
        JPanel generalDataPanel = new JPanel();
        generalDataPanel.setLayout(new GridLayout(1, 1, Resources.ROW_GAP, Resources.ROW_GAP));
        panel.add(generalDataPanel, BorderLayout.NORTH);
        generalDataPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED),
                                                                    Resources.getString("PerspectiveCreate.General"),
                                                                    TitledBorder.LEFT,
                                                                    TitledBorder.DEFAULT_POSITION));
        
        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new BorderLayout(Resources.ROW_GAP, Resources.ROW_GAP));
        titlePanel.add(new JLabel(Resources.getString("PerspectiveCreate.studyTitle")), BorderLayout.WEST);
        this.fieldTitle = new ComponentTextField(null);
        this.fieldTitle.setEnabled(false);
        this.fieldTitle.setChangeListener(this);
        titlePanel.add(this.fieldTitle, BorderLayout.CENTER);
        generalDataPanel.add(titlePanel);
        
        // Participants
        this.panelParticipants = new ScrollablePanel();
        this.panelParticipants.setLayout(new BoxLayout(this.panelParticipants, BoxLayout.Y_AXIS));
        JScrollPane pane = new JScrollPane(panelParticipants, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        pane.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED),
                                                                           Resources.getString("PerspectiveSend.participants"),
                                                                           TitledBorder.LEFT,
                                                                           TitledBorder.DEFAULT_POSITION));
        panel.add(pane, BorderLayout.CENTER);
        
        
        // Send all messages automatically button
        buttonsPane = new JPanel();
        buttonSendAllAutomatically = new JButton(Resources.getString("PerspectiveSend.sendAllEmailsButtonAutomatic"));
        buttonSendAllAutomatically.addActionListener(new ActionListener() {            
            @Override
            public void actionPerformed(ActionEvent e) {
                actionSendMessageAutomatically(getAllParticipants());
            }
        });
        
        // Send all message button manually
        buttonSendAllManually = new JButton(Resources.getString("PerspectiveSend.sendAllEmailsButtonManual"));
        buttonSendAllManually.addActionListener(new ActionListener() {            
            @Override
            public void actionPerformed(ActionEvent e) {
                actionSendMailManual(getAllParticipants());
            }
        });
        
        // Proceed button
        buttonProceed = new JButton(Resources.getString("PerspectiveSend.proceed"));
        buttonProceed.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                actionProceed();
            }
        });
        panel.add(buttonsPane, BorderLayout.SOUTH);
    }

    /**
     * Generate formated e-mail body
     * 
     * @param entry
     * @param exchangeString
     * @return
     */
    protected String generateEMailBody(EntryParticipantCheckmarkSendMail entry,
                                       String exchangeString) {
        return String.format(Resources.getString("PerspectiveSend.mailBody"),
                                    entry.getLeftValue(), // Name of participant
                                    getApp().getModel().getState() == StudyState.INITIAL_SENDING
                                            ? Resources.getString("PerspectiveSend.mailBodyParticipateStartFragement")
                                            : String.format(Resources.getString("PerspectiveSend.mailBodyParticapteProceedFragement"),
                                                            getApp().getModel().getState() == StudyState.SENDING_RESULT? 5: 3), // Step number
                                    exchangeString,
                                    getApp().getModel().getParticipants()[getApp().getModel().getOwnId()].name);
    }

    /**
     * Generates the formated e-mail subject
     * @return
     */
    protected String generateEMailSubject() {
        return String.format(Resources.getString("PerspectiveSend.mailSubject"),
                             getApp().getModel().getName(),
                             getApp().getModel().getState() == StudyState.INITIAL_SENDING ? 0
                                     : getApp().getModel().getState() == StudyState.SENDING_RESULT
                                             ? 2
                                             : 1);
    }

    /**
     * Generates the formated exchange string
     * 
     * @param entry
     * @return
     * @throws IOException
     */
    protected String generateFormatedExchangeString(EntryParticipantCheckmarkSendMail entry) throws IOException {
        // Get exchange string and add start/end tags
        String exchangeString =  Resources.MESSAGE_START_TAG + "\n" + getExchangeString(entry) + "\n" + Resources.MESSAGE_END_TAG;
        
        // Add line breaks
        return exchangeString.replaceAll("(.{" + Resources.MESSAGE_LINE_WIDTH + "})", "$1\n");
    }
    
    /**
     * Returns an identifier for the current round of EasySMPC 
     * This is needed to make sure the correct message are sent to the correct receivers
     * 
     * @return round
     */
    protected String getRoundIdentifier() {
        return Resources.ROUND_1;
    }

    /**
     * Initialize perspective based on model
     */
    @Override
    protected void initialize() {
               
        // Set title and clear participants
        this.fieldTitle.setText(getApp().getModel().getName());
        this.panelParticipants.removeAll();
        
        int i = 0; // Index count for participants to access messages
        for (Participant currentParticipant : getApp().getModel().getParticipants()) {
            
            // Add participant
            EntryParticipantCheckmarkSendMail entry = new EntryParticipantCheckmarkSendMail(currentParticipant.name, currentParticipant.emailAddress, i == getApp().getModel().getOwnId());
            panelParticipants.add(entry);
            
            // Create popup menu for the send-email-button
            final JPopupMenu popUp = new JPopupMenu(Resources.getString("PerspectiveSend.popupMenuTitle"));
            
            // Manual sending
            JMenuItem manualSend = new JMenuItem(Resources.getString("PerspectiveSend.popupMenuSendManually"));
            manualSend.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    actionSendMailManual(Arrays.asList(entry));
                }
            });
            popUp.add(manualSend);
            
            // Automatic sending
            JMenuItem automaticSend = new JMenuItem(Resources.getString("PerspectiveSend.popupMenuSendAutomatically"));
            automaticSend.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    actionSendMessageAutomatically(Arrays.asList(entry));
                }
            });
            automaticSend.setVisible(isInitialSending());
            automaticSend.setEnabled(isAutomaticProcessingEnabled());
            popUp.add(automaticSend);
            
            // Copy content to clip board, when the mailto-link doesn't work
            JMenuItem copy = new JMenuItem(Resources.getString("PerspectiveSend.popupMenuCopy"));
            copy.addActionListener(new ActionListener() {
                
                @Override
                public void actionPerformed(ActionEvent e) {
                    actionCopyButton(entry);
                }
            });
            popUp.add(copy);

            // Add popup menu to the button
            entry.setButtonListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    Component right = entry.getComponent(entry.getComponentCount() - 1);
                    popUp.show(entry, right.getBounds().x + right.getBounds().width -
                                      popUp.getPreferredSize().width, right.getBounds().y + right.getBounds().height);
                }
            });
            
            // Next element
            i++;
        }
        
        // Hide or show button to send automatically
        updateButtonsPane(isAutomaticProcessingEnabled());
        
        
        // Update state
        this.stateChanged(new ChangeEvent(this));
        
        // Update GUI
        getPanel().revalidate();
        getPanel().repaint();
        
        // Send messages automatically if enabled and password set
        if (isAutomaticProcessingEnabled()) {
            actionSendMessageAutomatically(getAllParticipants());
        }
    }
    
    /**
     * Marks all messages as sent
     */
    protected void markAllMessagesSent() {
        for (int index = 0; index < panelParticipants.getComponentCount(); index++) {
            if (index != getApp().getModel().getOwnId()) {
                getApp().actionMarkMessageSend(index);
            }
        }
    }
}
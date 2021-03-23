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
import java.awt.Container;
import java.awt.Desktop;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
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
import org.bihealth.mi.easybus.BusException;
import org.bihealth.mi.easybus.Scope;
import org.bihealth.mi.easysmpc.components.ComponentTextField;
import org.bihealth.mi.easysmpc.components.EntryParticipantSendMail;
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
    
    /**
     * Return all descendants of a certain type
     * @param <T>
     * @param clazz
     * @param container
     * @param nested
     * @return
     */
    @SuppressWarnings("unused")
    private static <T extends JComponent> List<T> getDescendantsOfType(Class<T> clazz, Container container, boolean nested) {
        List<T> tList = new ArrayList<T>();
        for (Component component : container.getComponents()) {
            if (clazz.isAssignableFrom(component.getClass())) {
                tList.add(clazz.cast(component));
            }
            if (nested || !clazz.isAssignableFrom(component.getClass())) {
                tList.addAll(getDescendantsOfType(clazz, (Container) component, nested));
            }
        }
        return tList;
    }

    /** Panel for participants */
    private ScrollablePanel    panelParticipants;

    /** Text field containing title of study */
    private ComponentTextField fieldTitle;

    /** Proceed button */
    private JButton            buttonProceed;

    /** Send button manual */
    private JButton            buttonSendAllManually;

    /** Send button automatic */
    private JButton            buttonSendAllAutomatically;

    /** Buttons pane */
    private JPanel             panelButtons;

    /**
     * Creates the perspective
     * @param app
     */
    protected Perspective2Send(App app) {
        super(app, Resources.getString("PerspectiveSend.send"), 2, true); //$NON-NLS-1$
    }
    
    /**
     * Creates the perspective
     * @param app
     * @param progress
     */
    protected Perspective2Send(App app, int progress) {
        super(app, Resources.getString("PerspectiveSend.send"), progress, true); //$NON-NLS-1$
    }
    
    /**
     * Creates the perspective
     * @param app
     * @param progress
     */
    protected Perspective2Send(App app, String title , int progress) {
        super(app, title, progress, true); //$NON-NLS-1$
    }
    
    /**
      * Reacts on all changes in any components
      */
     @Override
     public void stateChanged(ChangeEvent e) {
         
         // Check click able send all mails button and save button
         boolean messagesUnsent = getApp().getModel().messagesUnsent();
         this.buttonProceed.setEnabled(!messagesUnsent);
         this.buttonSendAllManually.setEnabled(messagesUnsent);
         this.buttonSendAllAutomatically.setEnabled(messagesUnsent && isAutomaticProcessingEnabled());

        // Check buttons clickable
        for (Component c : this.panelParticipants.getComponents()) {
            ((EntryParticipantSendMail) c).setButtonEnabled(isMailButtonClickable(c));
        }
        
        // If no more messages and automatic processing proceed automatically
        if (!messagesUnsent && isAutomaticProcessingEnabled()) {
            actionProceed();
        }
     }
    
    /**
     * @param entry
     */
   private void actionCopyButton(EntryParticipantSendMail entry) {
        // Push email body into clipboard
        try {
            String exchangeString = Resources.MESSAGE_START_TAG + "\n" + getExchangeString(entry) + "\n" + Resources.MESSAGE_END_TAG;
            String body = String.format(Resources.getString("PerspectiveSend.mailBody"),
                                        entry.getLeftValue(), // Name of participant
                                        getApp().getModel().state == StudyState.INITIAL_SENDING
                                                ? Resources.getString("PerspectiveSend.mailBodyParticipateStartFragement")
                                                : String.format(Resources.getString("PerspectiveSend.mailBodyParticapteProceedFragement"),
                                                                getApp().getModel().state == StudyState.SENDING_RESULT? 5: 3), // Step number
                                        exchangeString,
                                        getApp().getModel().participants[getApp().getModel().ownId].name);
            
            // Fill clipboard. Do this only if getExchangeString() was successful to avoid overwriting the users clipboard with nothing
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(body), null);
            
            // Send a dialog to confirm copying
            if (JOptionPane.showConfirmDialog(this.getPanel(), String.format(Resources.getString("PerspectiveSend.confirmSendCopyGeneric")), "", JOptionPane.OK_CANCEL_OPTION) == 0) {
                int index = Arrays.asList(this.panelParticipants.getComponents()).indexOf(entry);
                getApp().actionMarkMessageSent(index);
                
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
     * Sends an e-mail to the participant entry automatically
     * 
     * @param list
     */
    private void actionSendMailAutomatically(List<EntryParticipantSendMail> list) {
        
        // Deactivate buttons at start: Will be re-enabled if needed by the thread spawned below
        buttonSendAllAutomatically.setEnabled(false);
        buttonSendAllManually.setEnabled(false);
        for (Component c : this.panelParticipants.getComponents()) {
            ((EntryParticipantSendMail) c).setButtonEnabled(false);
        }
        
        // Spawn async task
        new SwingWorker<Void, Void>() {
            
            @Override
            protected Void doInBackground() throws Exception {

                // Create progress monitor
                ProgressMonitor monitor = new ProgressMonitor(Perspective2Send.this.getPanel(), 
                                                              Resources.getString("PerspectiveSend.ProgressTitle"),
                                                              Resources.getString("PerspectiveSend.ProgressNote"),
                                                              0, list.size());

                //  Disable cancel button
                // TODO: AccessibleContext ac = monitor.getAccessibleContext();
                // TODO: JDialog dialog = (JDialog)ac.getAccessibleParent();
                // TODO: java.util.List<JButton> components = getDescendantsOfType(JButton.class, dialog, true);
                // TODO: JButton button = components.get(0);
                // TODO: button.setVisible(false);
                // TODO: dialog.setModal(true);
                
                try {
                    
                    // Timing
                    monitor.setMillisToDecideToPopup(100);
                    monitor.setMillisToPopup(100);
                    monitor.setProgress(0);
                    
                    // Loop over messages
                    boolean messageSent = false;
                    int workDone = 0;
                    for (EntryParticipantSendMail entry : list) {
                        
                        // Send message
                        getApp().getModel().getBus().send(new org.bihealth.mi.easybus.Message(getExchangeString(entry)),
                                      new Scope(getApp().getModel().studyUID + getRoundIdentifier()),
                                      new org.bihealth.mi.easybus.Participant(entry.getLeftValue(), entry.getRightValue()));

                        
                        // Mark message sent
                        int index = Arrays.asList(panelParticipants.getComponents()).indexOf(entry);
                        getApp().actionMarkMessageSent(index);
                        messageSent = true;
                        monitor.setProgress(++workDone);
                    }
                    
                    // Persist changes
                    if (messageSent) {
                        getApp().actionSave();
                    }
                    
                } catch (BusException | IOException e) {
                    
                    // Error
                    monitor.setProgress(list.size());
                    JOptionPane.showMessageDialog(getPanel(),
                                                  Resources.getString("PerspectiveSend.sendAutomaticError"),
                                                  Resources.getString("PerspectiveSend.sendAutomaticErrorTitle"),
                                                  JOptionPane.ERROR_MESSAGE);
                }

                // Activate all buttons
                monitor.setProgress(list.size());
                stateChanged(new ChangeEvent(this));
                
                // Done
                return null;
            }
        }.execute();
    }

    /**
     * Sends an e-mail to the participant entry
     * 
     * @param list
     */
    private void actionSendMailManual(List<EntryParticipantSendMail> list) {
        
        try {
            
            // For each entry
            for (EntryParticipantSendMail entry : list) {
                
                // Prepare URI parts
                String subject = String.format(Resources.getString("PerspectiveSend.mailSubject"),
                                               getApp().getModel().name,
                                               getApp().getModel().state == StudyState.INITIAL_SENDING
                                                       ? 0
                                                       : getApp().getModel().state == StudyState.SENDING_RESULT
                                                               ? 2
                                                               : 1);
                
                String exchangeString = Resources.MESSAGE_START_TAG + "\n" + getExchangeString(entry) + "\n" + Resources.MESSAGE_END_TAG;
                exchangeString = exchangeString.replaceAll("(.{" + Resources.MESSAGE_LINE_WIDTH + "})", "$1\n");
                
                String body = String.format(Resources.getString("PerspectiveSend.mailBody"),
                                            entry.getLeftValue(), // Name of participant
                                            getApp().getModel().state == StudyState.INITIAL_SENDING
                                                    ? Resources.getString("PerspectiveSend.mailBodyParticipateStartFragement")
                                                    : String.format(Resources.getString("PerspectiveSend.mailBodyParticapteProceedFragement"),
                                                                    getApp().getModel().state == StudyState.SENDING_RESULT? 5: 3), // Step number
                                            exchangeString,
                                            getApp().getModel().participants[getApp().getModel().ownId].name);
                
                // Build URI
                URIBuilder builder = new URIBuilder().setScheme("mailto");
                builder.setPath(entry.getRightValue()).addParameter("subject", subject).addParameter("body", body);
                
                // Open email
                Desktop.getDesktop().mail(new URI(builder.toString().replace("+", "%20").replace(":/", ":")));
            }

            // Send a dialog to confirm mail sending
            if (JOptionPane.showConfirmDialog(this.getPanel(), String.format(Resources.getString("PerspectiveSend.confirmSendMailGeneric")), "", JOptionPane.OK_CANCEL_OPTION) == 0) {
                
                for (EntryParticipantSendMail entry : list) {
                    int index = Arrays.asList(this.panelParticipants.getComponents()).indexOf(entry);
                    getApp().actionMarkMessageSent(index);
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
     * Returns the exchange string for the given entry
     * 
     * @param entry
     * @throws IOException
     */
    private String getExchangeString(EntryParticipantSendMail entry) throws IOException {
        int index = Arrays.asList(panelParticipants.getComponents()).indexOf(entry);
        return Message.serializeMessage(getApp().getModel().getUnsentMessageFor(index));
    }
    
    /**
     * List all participants with unsent messages
     * 
     * @return list of participants
     */
    private List<EntryParticipantSendMail> getParticipantsWithUnsentMessages() {
        List<EntryParticipantSendMail> list = new ArrayList<>();
        for (Component c : panelParticipants.getComponents()) {
            if (!isOwnEntry(c) && isMessagesUnsent(c) ) {
                list.add((EntryParticipantSendMail)  c);
            }
        }
        return list;
    }
     
    /**
     * Indicates whether the automatic processing is displayed
     * 
     * @return enabled
     */
    private boolean isAutomaticProcessingDisplayed() {
        // It is not initial sending of study creator
        return !(getApp().getModelState() == StudyState.INITIAL_SENDING);
    }
    
     /**
     * Indicates whether the automatic processing enabled
     * 
     * @return enabled
     */
    private boolean isAutomaticProcessingEnabled() {
        // Return if automatic connection is enabled and it is not initial sending of study creator
        return getApp().getModel().connectionIMAPSettings != null &&
               !(getApp().getModelState() == StudyState.INITIAL_SENDING);
    }
    
    /**
     * Validates each send mail button whether it should be clickable
     */
    private boolean isMailButtonClickable(Component c) {
        int index = Arrays.asList(panelParticipants.getComponents()).indexOf(c);
        if (index == getApp().getModel().ownId ||
            getApp().getModel().getUnsentMessageFor(index) == null) {
            return false;
        }
        return true;
    }
    
    /**
     * Returns whether there are unsent message for the entry
     * @param entry
     * @return
     */
   private boolean isMessagesUnsent(Component entry) {
       return getApp().getModel().getUnsentMessageFor(Arrays.asList(panelParticipants.getComponents()).indexOf(entry)) != null;                     
   }

    /**
      * Returns whether this is the own entry
      * @param entry
      * @return
      */
    private boolean isOwnEntry(Component entry) {
        return Arrays.asList(panelParticipants.getComponents()).indexOf(entry) == getApp().getModel().ownId;
    }

    /**
     * Draws the buttons pane with or withaout the automatic resend button
     * 
     * @param showResend
     */
    private void updateButtonsPane(boolean showResend) {
        
        // Remove
        this.panelButtons.removeAll();
        
        // Create with two or three rows and add resend button of necessary
        if (showResend) {
            this.panelButtons.setLayout(new GridLayout(3, 1));
            this.panelButtons.add(this.buttonSendAllAutomatically, 0, 0);
            this.panelButtons.add(this.buttonSendAllManually, 0, 1);
            this.panelButtons.add(this.buttonProceed, 0, 2);
            
        } else {
            this.panelButtons.setLayout(new GridLayout(2, 1));
            this.panelButtons.add(this.buttonSendAllManually, 0, 0);
            this.panelButtons.add(this.buttonProceed, 0, 1);
        }
    }

    /**
     * Proceed action
     */
    protected void actionProceed() {
        getApp().actionFirstSendingDone();
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
        titlePanel.setLayout(new BorderLayout());
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
        
        
        // Send all e-mails automatically button
        panelButtons = new JPanel();
        buttonSendAllAutomatically = new JButton(Resources.getString("PerspectiveSend.sendAllEmailsButtonAutomatic"));
        buttonSendAllAutomatically.addActionListener(new ActionListener() {            
            @Override
            public void actionPerformed(ActionEvent e) {
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        actionSendMailAutomatically(getParticipantsWithUnsentMessages());
                    }
                  });
            }
        });
        
        // Send all e-mails button manually
        buttonSendAllManually = new JButton(Resources.getString("PerspectiveSend.sendAllEmailsButtonManual"));
        buttonSendAllManually.addActionListener(new ActionListener() {            
            @Override
            public void actionPerformed(ActionEvent e) {
                actionSendMailManual(getParticipantsWithUnsentMessages());
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
        panel.add(panelButtons, BorderLayout.SOUTH);
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
        
        this.fieldTitle.setText(getApp().getModel().name);
        this.panelParticipants.removeAll();
        
        int i = 0; // Index count for participants to access messages
        for (Participant currentParticipant : getApp().getModel().participants) {
            
            // Add participant
            EntryParticipantSendMail entry = new EntryParticipantSendMail(currentParticipant.name, currentParticipant.emailAddress, i != getApp().getModel().ownId);
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
                    actionSendMailAutomatically(Arrays.asList(entry));
                }
            });
            automaticSend.setVisible(isAutomaticProcessingDisplayed());
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
        updateButtonsPane(isAutomaticProcessingDisplayed());
        
        
        // Update state
        this.stateChanged(new ChangeEvent(this));
        
        // Update GUI
        getPanel().revalidate();
        getPanel().repaint();

        // Send e-mails automatically if enabled
        if (isAutomaticProcessingEnabled()) {
            actionSendMailAutomatically(getParticipantsWithUnsentMessages());
        }
    }
    
    @Override
    protected void uninitialize() {
        // Stop the bus for automatic processing if running
        if (getApp().getModel() != null) {
            getApp().getModel().stopBus();
        }
    }
}

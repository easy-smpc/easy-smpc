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
package org.bihealth.mi.easysmpc.components;

import java.awt.BorderLayout;
import java.net.MalformedURLException;
import java.net.URL;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.bihealth.mi.easybus.ConnectionSettings;
import org.bihealth.mi.easybus.implementations.http.easybackend.ConnectionSettingsEasyBackend;
import org.bihealth.mi.easybus.implementations.http.samplybeam.ConnectionSettingsSamplyBeam;
import org.bihealth.mi.easysmpc.resources.Resources;

import de.tu_darmstadt.cbs.emailsmpc.Participant;

/**
 * Entry for Samply.Beam config
 * 
 * @author Felix Wirth
 *
 */
public class EntryConnectionConfigSamplyBeam extends ComponentConnectionConfig implements ChangeListener {
    
    /** Entry for email address and app name*/
    private final ComponentEntryOne            emailAppEntry    = new ComponentEntryOne(Resources.getString("SamplyBeam.0"),
                                                                    null,
                                                                    true,
                                                                    new ComponentTextFieldValidator() {

                                                                                                                   @Override
                                                                                                                   public boolean
                                                                                                                          validate(String text) {
                                                                                                                       return Participant.validEmail(text);
                                                                                                                   }
                                                                                                               },
                                                                    false,
                                                                    false);
                                               
    /** Entry for proxy server URL and API key */
    private final ComponentEntry            urlKeyEntry    = new ComponentEntry(Resources.getString("SamplyBeam.2"),
                                                                    null,
                                                                    true,
                                                                    new ComponentTextFieldValidator() {

                                                                                                                   @Override
                                                                                                                   public boolean
                                                                                                                          validate(String text) {
                                                                                                                       if(text == null || text.isBlank()) {
                                                                                                                           return false;
                                                                                                                       }
                                                                                                                       
                                                                                                                       try {
                                                                                                                           ConnectionSettingsEasyBackend.checkURL(text);
                                                                                                                       } catch (Exception e) {
                                                                                                                           return false;
                                                                                                                       }
                                                                                                                       return true;
                                                                                                                   }
                                                                                                               },
                                                                    false,
                                                                    Resources.getString("SamplyBeam.3"),
                                                                    null,
                                                                    true,
                                                                    new ComponentTextFieldValidator() {

                                                                        @Override
                                                                        public boolean
                                                                               validate(String text) {
                                                                            return !text.trim().isEmpty();
                                                                        }
                                                                    },
                                                                    false,
                                                                    false,
                                                                    false) {

 

                                                   /** SVUID */
                                                   private static final long serialVersionUID = 7769043535065416551L;

                                                   @Override
                                                   protected JPanel createAdditionalControls() {
                                                       return null;
                                                   }
                                               };

    /** SVUID */
    private static final long    serialVersionUID = -5592016240952194634L;
    /** Tabbed pane */
    JTabbedPane                  tabbedPane       = new JTabbedPane();
    /** Entry for message size */
    private ComponentEntryOne    entryMessageSize;
    /** Entry for check interval */
    private ComponentEntryOne    entryCheckInterval;
    /** Entry for e-mail sending timeout */
    private ComponentEntryOne    entrySendTimeout;
    /** Central panel */
    private JPanel               centralBase;

    /**
     * Creates a new instance
     * @param parent 
     * @param settings
     * @param changeConfigAllowed
     */
    public EntryConnectionConfigSamplyBeam(JDialog parent, ConnectionSettingsSamplyBeam settings, boolean changeConfigAllowed){
        // Super
        super(parent, settings, changeConfigAllowed);
        
        // Base settings panes
        centralBase = new JPanel();
        JPanel top = new JPanel();
        JPanel mainBase = new JPanel();
        mainBase.setLayout(new BorderLayout());

        // Create central panel and its content
        centralBase.setLayout(new BoxLayout(centralBase, BoxLayout.Y_AXIS));
        centralBase.add(emailAppEntry);
        centralBase.add(urlKeyEntry);
        
        // Add base settings pane
        mainBase.add(top, BorderLayout.NORTH);
        mainBase.add(centralBase, BorderLayout.CENTER);

        // Create further settings pane
        JPanel mainFurther = new JPanel();
        JPanel centralFurther = new JPanel();
        centralFurther.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED),
                                                                  Resources.getString("EmailConfig.44"),
                                                                  TitledBorder.LEFT,
                                                                  TitledBorder.DEFAULT_POSITION));
        entryMessageSize = new ComponentEntryOne(Resources.getString("EmailConfig.45"),
                                                 null,
                                                 true,
                                                 new ComponentTextFieldValidator() {

            @Override
            public boolean validate(String text) {
                try {
                    Integer.parseInt(text);
                    return true;
                } catch (Exception e) {
                    return false;
                }
            }
        },
                                                 false,
                                                 false);
        entryCheckInterval = new ComponentEntryOne(Resources.getString("EmailConfig.46"),
                                                   null,
                                                   true,
                                                   new ComponentTextFieldValidator() {

            @Override
            public boolean validate(String text) {
                try {
                    Integer.parseInt(text);
                    return true;
                } catch (Exception e) {
                    return false;
                }
            }
        },
                                                   false,
                                                   false);
        entrySendTimeout = new ComponentEntryOne(Resources.getString("EmailConfig.47"),
                                                 null,
                                                 true,
                                                 new ComponentTextFieldValidator() {

            @Override
            public boolean validate(String text) {
                try {
                    Integer.parseInt(text);
                    return true;
                } catch (Exception e) {
                    return false;
                }
            }
        },
                                                 false,
                                                 false);

        // Set default values for further settings
        // TODO Adapt defaults
        entryMessageSize.setValue(String.valueOf(Resources.EMAIL_MAX_MESSAGE_SIZE_DEFAULT/ (1024 * 1024)));
        entryCheckInterval.setValue(String.valueOf(Resources.INTERVAL_CHECK_EASYBACKEND_DEFAULT / 1000));
        entrySendTimeout.setValue(String.valueOf(Resources.TIMEOUT_EASYBACKEND / 1000));

        // Add further settings pane
        centralFurther.setLayout(new BoxLayout(centralFurther, BoxLayout.Y_AXIS));
        mainFurther.setLayout(new BorderLayout());
        centralFurther.add(entryMessageSize);
        centralFurther.add(entryCheckInterval);
        centralFurther.add(entrySendTimeout);
        mainFurther.add(centralFurther, BorderLayout.CENTER);
        
        // Add tabbed panes
        tabbedPane.add(Resources.getString("EmailConfig.43"), mainBase);
        tabbedPane.add(Resources.getString("EmailConfig.44"), mainFurther);
        tabbedPane.addChangeListener(this);
        
        // Add
        this.add(tabbedPane, BorderLayout.CENTER);
        
        // Display dialog
        updateList();
        displaySettings(settings);
        
        // State changed
        stateChanged(new ChangeEvent(this));
    }

    /**
     * Creates a new instance
     * @param parent
     */
    public EntryConnectionConfigSamplyBeam(JDialog parent) {
        this(parent, null, true);
    }

    /**
     * Displays the settings after deciding for a complex or basic dialog
     * @param settings
     */
    @Override
    public void displaySettings(ConnectionSettings settingsGeneric) {
        // Check
        if(settingsGeneric != null && !(settingsGeneric instanceof ConnectionSettingsSamplyBeam)) {
            throw new IllegalStateException("Settings must be of type ConnectionSettingsSamplyBeam");
        }
        
        // Typecast
        ConnectionSettingsSamplyBeam settings = (ConnectionSettingsSamplyBeam) settingsGeneric;
        
        // Fill fields
        if (settings != null) {

            // Editing, not creating
            emailAppEntry.setFieldEnabled(false);
            
            // Set main fields
            emailAppEntry.setValue(settings.getIdentifier());
            urlKeyEntry.setLeftValue(settings.getProxyServer().toString());
            urlKeyEntry.setRightValue(settings.getApiKey());
            
            // Set fields for further options
            entryMessageSize.setValue(String.valueOf(settings.getMaxMessageSize() / (1024 * 1024)));
            entryCheckInterval.setValue(String.valueOf(settings.getCheckInterval() / 1000));
            entrySendTimeout.setValue(String.valueOf(settings.getSendTimeout() / 1000));
        } else {
            emailAppEntry.setFieldEnabled(true);
        }
    }

    @Override
    public boolean isProceedPossible() {
        return areValuesValid();
    }

    @Override
    public boolean isAddPossible() {
        return true;
    }

    @Override
    public ConnectionSettings getConnectionSettings() {
        
        // Read main fields
        ConnectionSettingsSamplyBeam result = new ConnectionSettingsSamplyBeam(emailAppEntry.getValue());
        try {
            result.setProxyServer(new URL(urlKeyEntry.getLeftValue()));
        } catch (MalformedURLException e) {
            return null;
        }
        result.setApiKey(urlKeyEntry.getRightValue());
        
        // Read additional options
        result.setMaxMessageSize(Integer.valueOf(entryMessageSize.getValue()) * (1024 * 1024));
        result.setCheckInterval(Integer.valueOf(entryCheckInterval.getValue()) * 1000);
        result.setSendTimeout(Integer.valueOf(entrySendTimeout.getValue()) * 1000);

        // Return
        return result;
    }

    /**
     * Checks for validity
     * 
     * @return
     */
    private boolean areValuesValid() {
        return emailAppEntry.isValueValid() && urlKeyEntry.areValuesValid();
    }

    @Override
    public Class<?> getSettingsClass() {
        return ConnectionSettingsSamplyBeam.class;
    }
}

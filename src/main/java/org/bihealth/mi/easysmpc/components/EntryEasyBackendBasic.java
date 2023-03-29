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
import javax.swing.JPanel;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeListener;

import org.bihealth.mi.easybus.PasswordStore;
import org.bihealth.mi.easybus.implementations.http.easybackend.ConnectionSettingsEasyBackend;
import org.bihealth.mi.easysmpc.AppPasswordProvider;
import org.bihealth.mi.easysmpc.resources.Resources;

/**
 *  Entry of basic details of an Easybackend connection
 * 
 * @author Felix Wirth
 */
public class EntryEasyBackendBasic extends JPanel {

    /** SVUID */
    private static final long        serialVersionUID = -4525263639314062052L;
    /** E-Mail and password entry */
    private final EntryEMailPassword entryEmailPassword;
    /** Server entry */
    private final ComponentEntryOne  entryServerURL;

    /**
     * Creates a new instance
     * 
     * @param settings
     * @param createMode
     */
    public EntryEasyBackendBasic(ConnectionSettingsEasyBackend settings, boolean createMode) {

        // General
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        // Panes
        JPanel loginPane = new JPanel();
        loginPane.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED),
                                                             Resources.getString("EmailConfig.36"),
                                                             TitledBorder.LEFT,
                                                             TitledBorder.DEFAULT_POSITION));
        loginPane.setLayout(new BorderLayout());
        JPanel loginInnerPane = new JPanel();
        loginInnerPane.setLayout(new BoxLayout(loginInnerPane, BoxLayout.Y_AXIS));

        JPanel serverPane = new JPanel();
        serverPane.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED),
                                                              Resources.getString("Easybackend.5"),
                                                              TitledBorder.LEFT,
                                                              TitledBorder.DEFAULT_POSITION));
        serverPane.setLayout(new BorderLayout());
        JPanel serverInnerPane = new JPanel();
        serverInnerPane.setLayout(new BoxLayout(serverInnerPane, BoxLayout.Y_AXIS));

        entryEmailPassword = new EntryEMailPassword(Resources.getString("EmailConfig.1"), Resources.getString("EmailConfig.2"));
        entryEmailPassword.setLefttEnabled(createMode);
        if (settings != null) {
            entryEmailPassword.setLeftValue(settings.getIdentifier());
            entryEmailPassword.setRightValue(settings.getPassword(false));
        }

        entryServerURL = new ComponentEntryOne(Resources.getString("Easybackend.1"),
                                               settings != null ? settings.getAPIServer().toString()
                                                       : null,
                                                       true,
                                                       new ComponentTextFieldValidator() {

            @Override
            public boolean validate(String text) {
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
                                                       false);

        // Add
        this.add(loginPane);
        loginPane.add(loginInnerPane, BorderLayout.CENTER);
        loginInnerPane.add(entryEmailPassword);
        this.add(serverPane);
        serverPane.add(serverInnerPane, BorderLayout.CENTER);
        serverInnerPane.add(entryServerURL);
        
        // Repaint
        this.revalidate();
        this.repaint();
    }

    /**
     * Sets a change listener
     * @param listener
     */
    public void setChangeListener(ChangeListener listener) {
        entryServerURL.setChangeListener(listener);
        entryEmailPassword.setChangeListener(listener);
    }

    /**
     *  Returns whether the settings are valid
     * 
     * @return
     */
    public boolean areValuesValid() {
        return entryServerURL.isValueValid() && entryEmailPassword.areValuesValid();
    }

    /**
     * Get connection settings
     * 
     * @return
     */
    public ConnectionSettingsEasyBackend getSettings() {
        // Prepare
        ConnectionSettingsEasyBackend result = null;
        try {
            result = new ConnectionSettingsEasyBackend(entryEmailPassword.getLeftValue(),
                                                       new AppPasswordProvider(Resources.getString("EmailConfig.33")))
                    .setAPIServer(new URL(entryServerURL.getValue()));
            result.setPasswordStore(new PasswordStore(entryEmailPassword.getRightValue() != null ? entryEmailPassword.getRightValue() : ""));

        } catch (MalformedURLException e) {
            return null;
        }

        // Return
        return result;
    }
}
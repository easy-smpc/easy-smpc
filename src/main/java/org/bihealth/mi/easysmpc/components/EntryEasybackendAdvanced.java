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

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeListener;

import org.apache.http.client.utils.URIBuilder;
import org.bihealth.mi.easybus.implementations.http.easybackend.ConnectionSettingsEasybackend;
import org.bihealth.mi.easysmpc.resources.Resources;

/**
 *  Entry of advanced details of an easybackend connection
 * 
 * @author Felix Wirth
 *
 */
 
public class EntryEasybackendAdvanced extends EntryEasybackendBasic {

    /** SVUID */
    private static final long         serialVersionUID = 8266372600895412625L;
    /** Auth server URL */
    private ComponentEntryOneCheckBox entryAuthServerURL;
    /** Proxy server URL */
    private ComponentEntryOneCheckBox entryProxyServerURL;
    /** Auth server realm */
    private ComponentEntryOneCheckBox entryRealm;
    /** Auth server client id */
    private ComponentEntryOneCheckBox entryClientId;
    /** Auth server client secret */
    private ComponentEntryOneCheckBox entryClientSecret;
    
    /**
     * Create new instance from settings object
     * 
     * @param settings
     * @param createMode 
     */
    public EntryEasybackendAdvanced(ConnectionSettingsEasybackend settings, boolean createMode) {
        super(settings, createMode);
        
        // General
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        
        // Panes
        JPanel authPane = new JPanel();
        authPane.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED),
                                                             Resources.getString("Easybackend.6"),
                                                             TitledBorder.LEFT,
                                                             TitledBorder.DEFAULT_POSITION));
        authPane.setLayout(new BoxLayout(authPane, BoxLayout.Y_AXIS));

        JPanel proxyPane = new JPanel();
        proxyPane.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED),
                                                              Resources.getString("Easybackend.7"),
                                                              TitledBorder.LEFT,
                                                              TitledBorder.DEFAULT_POSITION));
        proxyPane.setLayout(new BoxLayout(proxyPane, BoxLayout.Y_AXIS));
        
        // Entries
        this.entryAuthServerURL = new ComponentEntryOneCheckBox(Resources.getString("Easybackend.9"), new ComponentTextFieldValidator() {
            @Override
            public boolean validate(String text) {
                if(text == null || text.isBlank()) {
                    return false;
                }
                
                try {
                    ConnectionSettingsEasybackend.checkURL(text);
                } catch (Exception e) {
                    return false;
                }
                return true;
            }
        }, true, false);
        
        this.entryProxyServerURL = new ComponentEntryOneCheckBox(Resources.getString("Easybackend.8"), new ComponentTextFieldValidator() {
            @Override
            public boolean validate(String text) {
                if(text == null || text.isBlank()) {
                    return false;
                }
                
                try {
                    ConnectionSettingsEasybackend.checkURL(text);
                } catch (Exception e) {
                    return false;
                }
                return true;
            }
        }, true, false);
        
        this.entryRealm = new ComponentEntryOneCheckBox(Resources.getString("Easybackend.10"), new ComponentTextFieldValidator() {
            @Override
            public boolean validate(String text) {
                return text == null || text.isBlank() ? false : true;
            }
        }, true, false);
                
        this.entryClientId = new ComponentEntryOneCheckBox(Resources.getString("Easybackend.11"), new ComponentTextFieldValidator() {
            @Override
            public boolean validate(String text) {
                return text == null || text.isBlank() ? false : true;
            }
        }, true, false);
        
        this.entryClientSecret = new ComponentEntryOneCheckBox(Resources.getString("Easybackend.12"), new ComponentTextFieldValidator() {
            @Override
            public boolean validate(String text) {
                return text == null || text.isBlank() ? false : true;
            }
        }, true, false);
        
        // Add
        this.add(authPane);
        authPane.add(entryAuthServerURL);
        authPane.add(entryRealm);
        authPane.add(entryClientId);
        authPane.add(entryClientSecret);
        this.add(proxyPane);
        proxyPane.add(entryProxyServerURL);
        
        // Set values
        if(settings != null) {
            if (settings.getAPIServer() != null) {
                try {
                    entryAuthServerURL.setValue(new URIBuilder(settings.getAPIServer().toString()).setPort(9090).build().toURL().toString());
                } catch (MalformedURLException | URISyntaxException e) {
                    // Empty
                }
            }
            entryProxyServerURL.setValue(settings.getProxy()!= null ? settings.getProxy().toString() : null);
            entryRealm.setValue(settings.getRealm());
            entryClientId.setValue(settings.getClientId());
            entryClientSecret.setValue(settings.getClientSecret());
        }
    }
    
    /**
     * Sets a change listener
     * @param listener
     */
    public void setChangeListener(ChangeListener listener) {
        super.setChangeListener(listener);
        entryAuthServerURL.setChangeListener(listener);
        entryProxyServerURL.setChangeListener(listener);
        entryRealm.setChangeListener(listener);
        entryClientId.setChangeListener(listener);
        entryClientSecret.setChangeListener(listener);
        
    }
    
    /**
     *  Returns whether the settings are valid
     * 
     * @return
     */
    public boolean areValuesValid() {
        return super.areValuesValid() && entryAuthServerURL.isValueValid() &&
               entryProxyServerURL.isValueValid() && entryRealm.isValueValid() &&
               entryClientId.isValueValid() && entryClientSecret.isValueValid();
    }
    
    /**
     * Get entered auth server url
     * 
     * @return
     */
    private URL getAuthServerURL() {
        try {
            return new URL(entryAuthServerURL.getValue());
        } catch (MalformedURLException e) {
            throw new IllegalStateException("Can not understand auth server URL");
        }
    }
    
    /**
     * Get entered proxy server uri
     * 
     * @return
     */
    public URI getProxyServerURI() {
        if(entryProxyServerURL.getValue() == null || entryProxyServerURL.getValue().isBlank()) return null;
        
        try {
            return new URI(entryProxyServerURL.getValue());
        } catch (URISyntaxException e) {
            throw new IllegalStateException("Can not understand proxy server URI");
        }
    }
    
    /**
     * Get connection settings
     * @return
     */
    public ConnectionSettingsEasybackend getSettings() {
        // Prepare
        ConnectionSettingsEasybackend result = super.getSettings();
        
        result.setAuthServer(getAuthServerURL());
        result.setProxy(getProxyServerURI());
        result.setRealm(entryRealm.getValue());
        result.setClientId(entryClientId.getValue());
        result.setClientSecret(entryClientSecret.getValue() == null || entryClientSecret.getValue().isBlank() ? null : entryClientSecret.getValue());
        
        // Return
        return result;
    }
}
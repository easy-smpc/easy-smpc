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
package org.bihealth.mi.easybus.implementations.http.samplybeam;

import java.net.URL;

import org.bihealth.mi.easybus.ConnectionSettings;
import org.bihealth.mi.easysmpc.resources.Resources;

/**
 * Settings for Samply.Beam connections
 * 
 * @author Felix Wirth
 *
 */
public class ConnectionSettingsSamplyBeam extends ConnectionSettings {

    /** SVUID */
    private static final long serialVersionUID = -197213165506892522L;
    /** Send timeout */
    private int               sendTimeout;
    /** Maximal message size */
    private int               maxMessageSize;
    /** Check interval */
    private int               checkInterval;
    /** E-mail address */
    private final String      email;
    /** Name of API key */
    private String            apiKey;
    /** Proxy server URL */
    private URL               proxyServer;
    
    /**
     * Creates a new instance
     * @param appName
     */
    public ConnectionSettingsSamplyBeam(String email) {
        // Store
        this.email = email;
    }
    
    @Override
    public String getIdentifier() {
        // TODO Auto-generated method stub
        return email;
    }

    @Override
    public boolean isValid(boolean usePasswordProvider) {
        // TODO Auto-generated method stub
        return true;
    }

    @Override
    public ExchangeMode getExchangeMode() {
        return ExchangeMode.SAMPLYBEAM;
    }
    
    /**
     * @param sendTimeout the sendTimeout to set
     */
    public ConnectionSettingsSamplyBeam setSendTimeout(int sendTimeout) {
        this.sendTimeout = sendTimeout;
        return this;
    }

    /**
     * @param maxMessageSize the maxMessageSize to set
     * @return 
     */
    public ConnectionSettingsSamplyBeam setMaxMessageSize(int maxMessageSize) {
        this.maxMessageSize = maxMessageSize;
        return this;
    }

    /**
     * @param checkInterval the checkInterval to set
     */
    public ConnectionSettingsSamplyBeam setCheckInterval(int checkInterval) {
        this.checkInterval = checkInterval;
        return this;
    }
    
    /**
     * @return the sendTimeout
     */
    @Override
    public int getSendTimeout() {
        // TODO Adapt defaults?
        return sendTimeout > 0 ? sendTimeout : Resources.TIMEOUT_SEND_EMAILS_DEFAULT;
    }

    /**
     * @return the maxMessageSize
     */
    @Override
    public int getMaxMessageSize() {
        // TODO Adapt defaults?
        return maxMessageSize > 0 ? maxMessageSize : Resources.EMAIL_MAX_MESSAGE_SIZE_DEFAULT;
    }

    /**
     * @return the checkInterval
     */
    @Override
    public int getCheckInterval() {
        // TODO Adapt defaults?
        return checkInterval > 0 ? checkInterval : Resources.INTERVAL_CHECK_EASYBACKEND_DEFAULT;
    }

    /**
     * @return the apiKey
     */
    public String getApiKey() {
        return apiKey;
    }

    /**
     * @param apiKey the apiKey to set
     */
    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    /**
     * @return the proxyServer
     */
    public URL getProxyServer() {
        return proxyServer;
    }

    /**
     * @param proxyServer the proxyServer to set
     */
    public void setProxyServer(URL proxyServer) {
        this.proxyServer = proxyServer;
    }
 
    /**
     * @return the email
     */
    public String getEmail() {
        return email;
    }
}
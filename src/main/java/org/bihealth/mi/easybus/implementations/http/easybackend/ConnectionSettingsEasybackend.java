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
package org.bihealth.mi.easybus.implementations.http.easybackend;

import java.net.Proxy;
import java.net.URI;

import org.bihealth.mi.easybus.ConnectionSettings;
import org.bihealth.mi.easybus.Participant;
import org.bihealth.mi.easybus.PasswordStore;
import org.bihealth.mi.easybus.implementations.email.PasswordProvider;
import org.bihealth.mi.easysmpc.resources.Resources;

import jakarta.ws.rs.core.UriBuilder;

/**
 * Settings for Easybackend connections
 * 
 * @author Felix Wirth
 *
 */
public class ConnectionSettingsEasybackend  extends ConnectionSettings {

    /** SVUID */
    private static final long serialVersionUID = -944743683309534747L;
    /** Auth server URL */
    private URI               authServer;
    /** Easybackend server URL */
    private URI               apiServer;
    /** Keycloak realm */
    private String            realm            = "easybackend";
    /** Keycloak clien id */
    private String            clientId         = "easy-client";
    /** Keycloak client secret */
    private String            clientSecret;
    /** Self */
    private final Participant self;
    /** Proxy */
    private Proxy             proxy            = null;
    /** Send timeout */
    private int               sendTimeout;
    /** Maximal message size */
    private int               maxMessageSize;
    /** Check interval */
    private int               checkInterval;
    /** Password provider */
    private PasswordProvider  provider;


    /**
     * Creates a new instance
     * 
     * @param self - own participant
     * @param provider
     */
    public ConnectionSettingsEasybackend(Participant self, PasswordProvider provider) {
        // Checks
        checkNonNull(self);
        checkNonNull(provider);

        // Store
        this.self = self;
        this.provider = provider;
    }

    @Override
    public String getIdentifier() {
        return this.self.getEmailAddress();
    }

    @Override
    public boolean isValid() {
        // TODO Auto-generated method stub
        return false;
    }

    /**
     * @return the emailSendTimeout
     */
    @Override
    public int getSendTimeout() {
        return sendTimeout > 0 ? sendTimeout : Resources.TIMEOUT_SEND_EMAILS_DEFAULT;
    }

    /**
     * @return the maxMessageSize
     */
    @Override
    public int getMaxMessageSize() {
        return maxMessageSize > 0 ? maxMessageSize : Resources.EMAIL_MAX_MESSAGE_SIZE_DEFAULT;
    }

    /**
     * @return the checkInterval
     */
    @Override
    public int getCheckInterval() {
        return checkInterval > 0 ? checkInterval : Resources.INTERVAL_CHECK_MAILBOX_DEFAULT;
    }

    @Override
    public ConnectionTypes getConnectionType() {
        return ConnectionTypes.EASYBACKEND;
    }

    /**
     * @return the authServer
     */
    public URI getAuthServer() {
        return authServer;
    }

    /**
     * @param authServer the authServer to set
     */
    public ConnectionSettingsEasybackend setAuthServer(URI authServer) {
        this.authServer = authServer;
        return this;
    }

    /**
     * @return the apiServer
     */
    public URI getAPIServer() {
        return apiServer;
    }

    /**
     * @param apiServer the apiServer to set
     */
    public ConnectionSettingsEasybackend setAPIServer(URI apiServer) {
        this.apiServer = apiServer;
        // TODO
        authServer = UriBuilder.fromUri(apiServer).port(9090).build();
        return this;
    }

    /**
     * @return the realm
     */
    public String getRealm() {
        return realm;
    }

    /**
     * @param realm the realm to set
     */
    public ConnectionSettingsEasybackend setRealm(String realm) {
        this.realm = realm;
        return this;
    }

    /**
     * @return the clientId
     */
    public String getClientId() {
        return clientId;
    }

    /**
     * @param clientId the clientId to set
     */
    public ConnectionSettingsEasybackend setClientId(String clientId) {
        this.clientId = clientId;
        return this;
    }

    /**
     * @return the self
     */
    public Participant getSelf() {
        return self;
    }

    /**
     * Return config parameter
     * @return the IMAP password
     */
    public String getPassword() {
        return getPassword(true);
    }

    /**
     * Return the password
     * @param usePasswordProvider
     * @return
     */
    public String getPassword(boolean usePasswordProvider) {
        // Potentially ask for password
        if ((this.getPasswordStore() == null || this.getPasswordStore().getFirstPassword() == null) && this.provider != null && usePasswordProvider) {
            // Get passwords
            PasswordStore store = this.provider.getPassword();

            // Check
            if(store == null) {
                return null;
            }

            // Store
            this.setPasswordStore(store);

            // Check connection settings
            if (!this.isValid()) {
                setPasswordStore(null);
            }
        }

        // Return password
        return getPasswordStore() == null ? null : getPasswordStore().getFirstPassword();
    }

    /**
     * @return the proxy
     */
    public Proxy getProxy() {
        return proxy;
    }

    /**
     * @param proxy the proxy to set
     */
    public ConnectionSettingsEasybackend setProxy(Proxy proxy) {
        this.proxy = proxy;
        return this;
    }

    /**
     * @param sendTimeout the sendTimeout to set
     */
    public void setSendTimeout(int sendTimeout) {
        this.sendTimeout = sendTimeout;
    }

    /**
     * @param maxMessageSize the maxMessageSize to set
     */
    public void setMaxMessageSize(int maxMessageSize) {
        this.maxMessageSize = maxMessageSize;
    }

    /**
     * @param checkInterval the checkInterval to set
     */
    public void setCheckInterval(int checkInterval) {
        this.checkInterval = checkInterval;
    }

    /**
     * @return the clientSecret
     */
    protected String getClientSecret() {
        return clientSecret;
    }

    /**
     * @param clientSecret the clientSecret to set
     */
    protected ConnectionSettingsEasybackend setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
        return this;
    }

    /**
     * Check
     * @param object
     */
    private void checkNonNull(Object object) {
        if (object == null) {
            throw new IllegalArgumentException("Parameter must not be null");
        }
    }

    public boolean isValid(boolean b) {
        // TODO Necessary?
        return true;
    }
}
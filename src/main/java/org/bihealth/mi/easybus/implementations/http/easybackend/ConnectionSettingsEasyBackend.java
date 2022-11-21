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

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import org.apache.commons.validator.routines.UrlValidator;
import org.apache.http.client.utils.URIBuilder;
import org.bihealth.mi.easybus.ConnectionSettings;
import org.bihealth.mi.easybus.Participant;
import org.bihealth.mi.easybus.PasswordStore;
import org.bihealth.mi.easybus.PerformanceListener;
import org.bihealth.mi.easybus.implementations.PasswordProvider;
import org.bihealth.mi.easybus.implementations.http.HTTPAuthentication;
import org.bihealth.mi.easysmpc.resources.Resources;

/**
 * Settings for Easybackend connections
 * 
 * @author Felix Wirth
 */
public class ConnectionSettingsEasyBackend  extends ConnectionSettings {

    /** SVUID */
    private static final long             serialVersionUID = -944743683309534747L;
    /** Auth server URL */
    private URL                           authServer;
    /** Easybackend server URL */
    private URL                           apiServer;
    /** Keycloak realm */
    private String                        realm            = Resources.AUTH_REALM_DEFAULT;
    /** Keycloak clien id */
    private String                        clientId         = Resources.AUTH_CLIENTID_DEFAULT;
    /** Keycloak client secret */
    private String                        clientSecret;
    /** Self */
    private final Participant             self;
    /** Proxy */
    private URI                           proxy            = null;
    /** Send timeout */
    private int                           sendTimeout;
    /** Maximal message size */
    private int                           maxMessageSize;
    /** Check interval */
    private int                           checkInterval;
    /** Password provider */
    private PasswordProvider              provider;
    /** Performance listener */
    private transient PerformanceListener listener         = null;
    /** URL validator */
    private final static UrlValidator     URL_VALIDATOR    = new UrlValidator(new String[] { "https" }, UrlValidator.ALLOW_LOCAL_URLS);

    /**
     * Creates a new instance
     * 
     * @param self - own participant
     * @param provider
     */
    public ConnectionSettingsEasyBackend(Participant self, PasswordProvider provider) {
        
        // Checks
        checkNonNull(self);

        // Store
        this.self = self;
        this.provider = provider;
    }

    @Override
    public String getIdentifier() {
        return this.self.getEmailAddress();
    }

    /**
     * @return the sendTimeout
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
        return checkInterval > 0 ? checkInterval : Resources.INTERVAL_CHECK_EASYBACKEND_DEFAULT;
    }

    @Override
    public ExchangeMode getExchangeMode() {
        return ExchangeMode.EASYBACKEND;
    }

    /**
     * @return the authServer
     */
    public URL getAuthServer() {
        return authServer != null ? authServer : apiServer;
    }

    /**
     * @param authServer the authServer to set
     */
    public ConnectionSettingsEasyBackend setAuthServer(URL authServer) {
        this.authServer = authServer;
        return this;
    }

    /**
     * @return the apiServer
     */
    public URL getAPIServer() {
        return apiServer;
    }

    /**
     * @param apiServer the apiServer to set
     */
    public ConnectionSettingsEasyBackend setAPIServer(URL apiServer) {
        checkURL(apiServer.toString());
        
        URIBuilder uriBuilder;
        try {
            uriBuilder = new URIBuilder(apiServer.toString());
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("URL not understood");
        }
        
        // Set defaults
       if(uriBuilder.getPort() == -1 || uriBuilder.getPort() == 0) {
           uriBuilder.setPort(443);
       }        
        
        // Set
        try {
            this.apiServer = uriBuilder.build().toURL();
        } catch (MalformedURLException | URISyntaxException e) {
            throw new IllegalArgumentException("URL not understood");
        }
        
        // Return
        return this;
    }
    
    /**
     * Check if url is valid
     * 
     * @param url to check
     */
    public static void checkURL(String url) {
        if(!URL_VALIDATOR.isValid(url)) {
            throw new IllegalStateException("URL is not valid!");
        }
    }

    /**
     * @return the realm
     */
    public String getRealm() {
        return realm != null ? realm : Resources.AUTH_REALM_DEFAULT;
    }

    /**
     * @param realm the realm to set
     */
    public ConnectionSettingsEasyBackend setRealm(String realm) {
        this.realm = realm;
        return this;
    }

    /**
     * @return the clientId
     */
    public String getClientId() {
        return clientId != null ? clientId : Resources.AUTH_CLIENTID_DEFAULT;
    }

    /**
     * @param clientId the clientId to set
     */
    public ConnectionSettingsEasyBackend setClientId(String clientId) {
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
            if (!this.isValid(false)) {
                setPasswordStore(null);
            }
        }

        // Return password
        return getPasswordStore() == null ? null : getPasswordStore().getFirstPassword();
    }

    /**
     * @return the proxy
     */
    public URI getProxy() {
        return proxy;
    }

    /**
     * @param proxy the proxy to set
     */
    public ConnectionSettingsEasyBackend setProxy(URI proxy) {
        this.proxy = proxy;
        return this;
    }

    /**
     * @param sendTimeout the sendTimeout to set
     */
    public ConnectionSettingsEasyBackend setSendTimeout(int sendTimeout) {
        this.sendTimeout = sendTimeout;
        return this;
    }

    /**
     * @param maxMessageSize the maxMessageSize to set
     * @return 
     */
    public ConnectionSettingsEasyBackend setMaxMessageSize(int maxMessageSize) {
        this.maxMessageSize = maxMessageSize;
        return this;
    }

    /**
     * @param checkInterval the checkInterval to set
     */
    public ConnectionSettingsEasyBackend setCheckInterval(int checkInterval) {
        this.checkInterval = checkInterval;
        return this;
    }

    /**
     * @return the clientSecret
     */
    public String getClientSecret() {
        return clientSecret;
    }

    /**
     * @param clientSecret the clientSecret to set
     */
    public ConnectionSettingsEasyBackend setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
        return this;
    }
    
    /**
     * @return the listener
     */
    protected PerformanceListener getListener() {
        return listener;
    }

    /**
     * @param listener the listener to set
     */
    protected ConnectionSettingsEasyBackend setListener(PerformanceListener listener) {
        this.listener = listener;
        return this;
    }

    /**
     * Check
     * @param object
     */
    public static void checkNonNull(Object object) {
        if (object == null) {
            throw new IllegalArgumentException("Parameter must not be null");
        }
    }
    
    @Override
    public boolean isValid(boolean usePasswordProvider) {
        
        if ((this.getPasswordStore() == null || this.getPasswordStore().getFirstPassword() == null) && !usePasswordProvider) {
            return false;
        }

        try {
            // Add check to API server
            new HTTPAuthentication(this).authenticate();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public boolean isPlainPossible() {
        return false;
    }
}
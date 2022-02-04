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
package org.bihealth.mi.easybus.implementations.email;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.ProxySelector;
import java.net.URL;
import java.util.Objects;
import java.util.Properties;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilderFactory;

import org.bihealth.mi.easybus.Participant;
import org.bihealth.mi.easybus.PerformanceListener;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.github.markusbernhardt.proxy.ProxySearch;

/**
 * Settings for IMAP connections
 * 
 * @author Felix Wirth
 * @author Fabian Prasser
 */
public class ConnectionIMAPSettings implements Serializable {

    /** SVUID */
    private static final long    serialVersionUID            = 3880443185633907293L;
    /** End-point for Mozilla auto-configuration service */
    public static final String   MOZILLA_AUTOCONF            = "https://autoconfig.thunderbird.net/v1.1/";
    /** Standard port for IMAP */
    public static final int      DEFAULT_PORT_IMAP           = 993;
    /** Standard port for SMTP */
    public static final int      DEFAULT_PORT_SMTP           = 465;
    /** Regex to check dns validity */
    private static final Pattern regexDNS                    = Pattern.compile("^([a-zA-Z0-9]|[a-zA-Z0-9][a-zA-Z0-9\\-]{0,61}[a-zA-Z0-9])(\\.([a-zA-Z0-9]|[a-zA-Z0-9][a-zA-Z0-9\\-]{0,61}[a-zA-Z0-9]))*$");
    /** Key */
    private static final String  EMAIL_ADDRESS_KEY           = "email_address";
    /** Key */
    private static final String  PASSWORD_KEY                = "password";
    /** Key */
    private static final String  IMAP_SERVER_KEY             = "imap_server";
    /** Key */
    private static final String  IMAP_PORT_KEY               = "imap_port";
    /** Key */
    private static final String  IMAP_ENCRYPTION_TYPE        = "imap_encryption";
    /** Key */
    private static final String  SMTP_SERVER_KEY             = "smtp_server";
    /** Key */
    private static final String  SMTP_PORT_KEY               = "smtp_port";
    /** Key */
    private static final String  SMTP_ENCRYPTION_TYPE        = "smtp_encryption";
    /** Key */
    private static final String  ACCEPT_SELF_SIGNED_CERT_KEY = "accept_self_signed_cert";
    /** Key */
    private static final String  USE_PROXY_KEY               = "use_poxy";
    /** Prefix for system properties */
    private static final String  PREFIX_SYSTEM_PROPERTIES    = "org.bihealth.mi.easybus.";

    /**
     * Check server name
     * 
     * @param text
     */
    public static void checkDNSName(String text) {
        if (!regexDNS.matcher(text).matches()) {
            throw new IllegalArgumentException("DNS name invalid");
        }
    }

    /**
     * Check
     * 
     * @param object
     */
    public static void checkPort(int object) {
        if (object < 1 || object > 65535) {
            throw new IllegalArgumentException("Port must not be between 1 and 65535");
        }
    }

    /** E-mail address */
    private String                        emailAddress;
    /** Password */
    private transient String              password;
    /** IMAP server dns */
    private String                        imapServer;
    /** Port of IMAP server */
    private int                           imapPort             = DEFAULT_PORT_IMAP;
    /** SMTP server dns */
    private String                        smtpServer;
    /** Port of SMTP server */
    private int                           smtpPort             = DEFAULT_PORT_SMTP;
    /** Accept self signed certificates */
    private boolean                       acceptSelfSignedCert = false;
    /** Search for proxy */
    private boolean                       searchForProxy       = false;
    /** Performance listener */
    private transient PerformanceListener listener             = null;
    /** Use ssl/tls (=true) or starttls (=false) for IMAP connection */
    private boolean                       ssltlsIMAP           = true;
    /** Use ssl/tls (=true) or starttls (=false) for SMTP connection */
    private boolean                       ssltlsSMTP           = true;
    /** Password accessor */
    private PasswordProvider              provider;

    /**
     * Creates a new instance
     * 
     * @param emailAddress
     * @param provider
     */
    public ConnectionIMAPSettings(String emailAddress, PasswordProvider provider) {
        
        // Checks
        checkNonNull(emailAddress);
        if (!Participant.isEmailValid(emailAddress)) {
            throw new IllegalArgumentException("Invalid e-mail address");
        }
        
        // Store
        this.emailAddress = emailAddress;
        this.provider = provider;
    }
    
    /**
     * Checks whether fields are empty
     * 
     * @return has null fields
     */
    public void check() {
        if (this.emailAddress == null || this.imapServer == null || this.smtpServer == null) {
            throw new IllegalArgumentException("Connection parameters must not be null");
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        ConnectionIMAPSettings other = (ConnectionIMAPSettings) obj;
        return acceptSelfSignedCert == other.acceptSelfSignedCert &&
               Objects.equals(emailAddress, other.emailAddress) && imapPort == other.imapPort &&
               Objects.equals(imapServer, other.imapServer) &&
               Objects.equals(password, other.password) && searchForProxy == other.searchForProxy &&
               smtpPort == other.smtpPort && Objects.equals(smtpServer, other.smtpServer) &&
               ssltlsIMAP == other.ssltlsIMAP && ssltlsSMTP == other.ssltlsSMTP;
    }

    /**
     * Return config parameter
     * @return the emailAddress
     */
    public String getEmailAddress() {
        return emailAddress;
    }

    /**
     * Return config parameter
     * @return the imapPort
     */
    public int getIMAPPort() {
        return imapPort;
    }

    /**
     * Return config parameter
     * @return the imapServer
     */
    public String getIMAPServer() {
        return imapServer;
    }
    
    /**
     * Return config parameter
     * @return the password
     */
    public String getPassword() {
        return getPassword(true);
    }

    /**
     * Return config parameter
     * @param usePasswordProvider
     * @return the password
     */
    public String getPassword(boolean usePasswordProvider) {
        
        // Potentially ask for password
        if (this.password == null && this.provider != null && usePasswordProvider) {
            this.password = this.provider.getPassword();
            
            // Check connection settings
            if (!this.isValid()) {
                this.password = null;
            }
        }
        
        // Return password
        return this.password;
    }

    /**
     * Returns performance listener
     * @return
     */
	public PerformanceListener getPerformanceListener() {
		return listener;
	}

    /**
     * Return config parameter
     * @return the smtpPort
     */
    public int getSMTPPort() {
        return smtpPort;
    }
    
    /**
     * Return config parameter
     * @return the smtpServer
     */
    public String getSMTPServer() {
        return smtpServer;
    }
    
    /**
     * Tries to guess the connection settings from the email address provider
     * @param Whether settings could be guessed successfully
     */
    public boolean guess() {
        // Auto discovery for proxy connections
        ProxySelector.setDefault(ProxySearch.getDefaultProxySearch().getProxySelector());
        
        // Initialize
        String mozillaConfEndpoint = MOZILLA_AUTOCONF + emailAddress.substring(emailAddress.indexOf("@") + 1, emailAddress.length());

        try {
            
            // Request and serialize XML document
            HttpURLConnection connection = (HttpURLConnection) new URL(mozillaConfEndpoint).openConnection();
            connection.setRequestMethod("GET");
            Document doc = DocumentBuilderFactory.newInstance()
                                        .newDocumentBuilder()
                                        .parse(connection.getInputStream());

            // Set IMAP
            NodeList list = doc.getDocumentElement().getElementsByTagName("incomingServer");
            for (int i = 0; i < list.getLength(); i++) {
                Element element = (Element) list.item(i);
                if (element.getAttributes()
                           .getNamedItem("type")
                           .getNodeValue()
                           .equalsIgnoreCase("imap")) {
                    this.imapServer = element.getElementsByTagName("hostname")
                                             .item(0)
                                             .getChildNodes()
                                             .item(0)
                                             .getNodeValue();
                    this.imapPort = Integer.parseInt(element.getElementsByTagName("port")
                                                            .item(0)
                                                            .getChildNodes()
                                                            .item(0)
                                                            .getNodeValue());
                    this.ssltlsIMAP = element.getElementsByTagName("socketType")
                                             .item(0)
                                             .getChildNodes()
                                             .item(0)
                                             .getNodeValue()
                                             .equals("STARTTLS") ? false : true;
                }
            }

            // Set SMTP
            Element element = (Element) doc.getDocumentElement()
                                   .getElementsByTagName("outgoingServer")
                                   .item(0);
            smtpServer = element.getElementsByTagName("hostname")
                                .item(0)
                                .getChildNodes()
                                .item(0)
                                .getNodeValue();
            this.smtpPort = Integer.parseInt(element.getElementsByTagName("port")
                                                    .item(0)
                                                    .getChildNodes()
                                                    .item(0)
                                                    .getNodeValue());
            this.ssltlsSMTP = element.getElementsByTagName("socketType")
                    .item(0)
                    .getChildNodes()
                    .item(0)
                    .getNodeValue()
                    .equals("STARTTLS") ? false : true;
            
            // Success
            return true;
            
        } catch (Exception e) {
            // No success
            return false;
        }
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(acceptSelfSignedCert,
                            emailAddress,
                            imapPort,
                            imapServer,
                            password,
                            searchForProxy,
                            smtpPort,
                            smtpServer,
                            ssltlsIMAP,
                            ssltlsSMTP);
    }
      
    /**
     * Returns whether self-signed certificates are accepted
     * @return
     */
    public boolean isAcceptSelfSignedCertificates() {
    	return acceptSelfSignedCert;
    }

    /**
     * Search for proxy
     * @return
     */
	public boolean isSearchForProxy() {
		return this.searchForProxy;
	}

    /**
     * Is ssl/tls or startls used for IMAP connection?
     * @return the ssltlsIMAP
     */
    public boolean isSSLTLSIMAP() {
        return ssltlsIMAP;
    }

    /**
     * Is ssl/tls or startls used for SMTP connection?     
     * @return the ssltlsSMTP
     */
    public boolean isSSLTLSSMTP() {
        return ssltlsSMTP;
    }
    
    /**
     * Returns whether this connection is valid
     * 
     * @return
     */
    public boolean isValid() {
        return isValid(false);
    }

    /**
     * Returns whether this connection is valid
     * 
     * @param usePasswordProvider
     * @return
     */
    public boolean isValid(boolean usePasswordProvider) {
        
        if (this.password == null && !usePasswordProvider) {
            return false;
        }

        if (this.password == null && getPassword() == null) {
            return false;
        }       

        try {
            return new ConnectionIMAP(this, false).checkConnection();
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Accept self-signed certificates
     * @param accept
     */
    public ConnectionIMAPSettings setAcceptSelfSignedCertificates(boolean accept) {
    	this.acceptSelfSignedCert = accept;
    	return this;
    }
    
    /**
     * Set config parameter
     * @param imapPort the IMAP port to set
     */
    public ConnectionIMAPSettings setIMAPPort(int imapPort) {

        // Check
        checkNonNull(imapPort);
        checkPort(imapPort);
        
        // Set
        this.imapPort = imapPort;

        // Done
        return this;
    }
    
    /**
     * Set config parameter
     * @param imapServer the IMAP server to set
     */
    public ConnectionIMAPSettings setIMAPServer(String imapServer) {
        
        // Check
        checkNonNull(imapServer);
        checkDNSName(imapServer);
        
        // Set
        this.imapServer = imapServer;

        // Done
        return this;
    }
    
    /**
     * Set config parameter
     * @param password the password to set
     */
    public ConnectionIMAPSettings setPassword(String password) {
        this.password = password;
        return this;        
    }
    
    /**
     * Sets performance listener
     * @param listener
     * @return
     */
    public ConnectionIMAPSettings setPerformanceListener(PerformanceListener listener) {
    	this.listener = listener;
    	return this;
    }
    
    /**
     * Search for proxy
     * @param search
     * @return
     */
    public ConnectionIMAPSettings setSearchForProxy(boolean search) {
    	this.searchForProxy = search;
    	return this;
    }
    
    /**
     * Set config parameter
     * @param smtpPort the SMTP port to set
     */
    public ConnectionIMAPSettings setSMTPPort(int smtpPort) {

        // Check
        checkNonNull(smtpPort);
        checkPort(smtpPort);
        
        // Set
        this.smtpPort = smtpPort;

        // Done
        return this;
    }

    /**
     * Set config parameter
     * @param smtpServer the SMTP server to set
     */
    public ConnectionIMAPSettings setSMTPServer(String smtpServer) {

        // Check
        checkNonNull(smtpServer);
        checkDNSName(smtpServer);
        
        // Set
        this.smtpServer = smtpServer;

        // Done
        return this;
    }
    
    /**
     * @param ssltlsIMAP the ssltlsIMAP to set
     */
    public ConnectionIMAPSettings setSSLTLSIMAP(boolean ssltlsIMAP) {
        // Set
        this.ssltlsIMAP = ssltlsIMAP;
        
        // Done
        return this;
    }
    /**
     * @param ssltlsSMTP the ssltlsSMTP to set
     */
    public ConnectionIMAPSettings setSSLTLSSMTP(boolean ssltlsSMTP) {
        // Set
        this.ssltlsSMTP = ssltlsSMTP;
        
        // Done
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
    
    /**
     * Reads all IMAP connection settings from file but the password. The password will be provided with the passwordProvider parameter
     * 
     * @param file
     * @param passwordProvider
     * @return
     */
    public static ConnectionIMAPSettings getConnectionIMAPSettingsFromFile(File file, PasswordProvider passwordProvider) {
        // Check
        if (file == null) { return null; }
        
        // Prepare
        Properties prop = new Properties();
        try {
            prop.load(new FileInputStream(file));
        } catch (IOException e) {          
            throw new IllegalStateException("Unable to load file to read ConnectionIMAPSettings", e);
        }
        
        // Check parameters
        if (prop.getProperty(EMAIL_ADDRESS_KEY) == null ||prop.getProperty(IMAP_SERVER_KEY) == null || prop.getProperty(IMAP_PORT_KEY) == null || prop.getProperty(IMAP_ENCRYPTION_TYPE) == null
            || prop.getProperty(SMTP_SERVER_KEY) == null || prop.getProperty(SMTP_PORT_KEY) == null || prop.getProperty(SMTP_ENCRYPTION_TYPE) == null) {            
            throw new IllegalStateException("Properties file does not contain all necessary fields!");
        }
        
        // Return
        return new ConnectionIMAPSettings(prop.getProperty(EMAIL_ADDRESS_KEY),
                                          passwordProvider).setIMAPServer(prop.getProperty(IMAP_SERVER_KEY))
                                                           .setIMAPPort(Integer.valueOf(prop.getProperty(IMAP_PORT_KEY)))
                                                           .setSMTPServer(prop.getProperty(SMTP_SERVER_KEY))
                                                           .setSMTPPort(Integer.valueOf(prop.getProperty(SMTP_PORT_KEY)))
                                                           .setSSLTLSIMAP(Boolean.valueOf(prop.getProperty(IMAP_ENCRYPTION_TYPE)))
                                                           .setSSLTLSSMTP(Boolean.valueOf(prop.getProperty(SMTP_ENCRYPTION_TYPE)))
                                                           .setAcceptSelfSignedCertificates(prop.getProperty(ACCEPT_SELF_SIGNED_CERT_KEY) != null
                                                                   ? Boolean.valueOf(prop.getProperty(ACCEPT_SELF_SIGNED_CERT_KEY))
                                                                   : false)
                                                           .setSearchForProxy(prop.getProperty(USE_PROXY_KEY) != null
                                                                   ? Boolean.valueOf(prop.getProperty(USE_PROXY_KEY))
                                                                   : false);
    }
    
    /**
     * Reads IMAP connection settings from system properties including the password
     * 
     * @param file
     * @return IMAP connection settings
     */
    public static ConnectionIMAPSettings getConnectionIMAPSettingsFromSystemProperties() {        

        // Check parameters
        if (System.getProperties().getProperty(PREFIX_SYSTEM_PROPERTIES + EMAIL_ADDRESS_KEY) == null ||
                System.getProperties().getProperty(PREFIX_SYSTEM_PROPERTIES + PASSWORD_KEY) == null ||
                System.getProperties().getProperty(PREFIX_SYSTEM_PROPERTIES + IMAP_SERVER_KEY) == null ||
                System.getProperties().getProperty(PREFIX_SYSTEM_PROPERTIES + IMAP_PORT_KEY) == null ||
                System.getProperties().getProperty(PREFIX_SYSTEM_PROPERTIES + IMAP_ENCRYPTION_TYPE) == null ||
                System.getProperties().getProperty(PREFIX_SYSTEM_PROPERTIES + SMTP_SERVER_KEY) == null ||
                System.getProperties().getProperty(PREFIX_SYSTEM_PROPERTIES + SMTP_PORT_KEY) == null ||
                System.getProperties().getProperty(PREFIX_SYSTEM_PROPERTIES + SMTP_ENCRYPTION_TYPE) == null) {
            throw new IllegalStateException("Properties file does not contain all necessary fields!");
        }
        
        // Return
        return new ConnectionIMAPSettings(System.getProperties().getProperty(PREFIX_SYSTEM_PROPERTIES + EMAIL_ADDRESS_KEY), null)
                .setPassword(System.getProperties().getProperty(PREFIX_SYSTEM_PROPERTIES + PASSWORD_KEY))
                .setIMAPServer(System.getProperties().getProperty(PREFIX_SYSTEM_PROPERTIES + IMAP_SERVER_KEY))
                .setIMAPPort(Integer.valueOf(System.getProperties().getProperty(PREFIX_SYSTEM_PROPERTIES + IMAP_PORT_KEY)))
                .setSMTPServer(System.getProperties().getProperty(PREFIX_SYSTEM_PROPERTIES + SMTP_SERVER_KEY))
                .setSMTPPort(Integer.valueOf(System.getProperties().getProperty(PREFIX_SYSTEM_PROPERTIES + SMTP_PORT_KEY)))
                .setSSLTLSIMAP(Boolean.valueOf(System.getProperties().getProperty(PREFIX_SYSTEM_PROPERTIES + IMAP_ENCRYPTION_TYPE)))
                .setSSLTLSSMTP(Boolean.valueOf(System.getProperties().getProperty(PREFIX_SYSTEM_PROPERTIES + SMTP_ENCRYPTION_TYPE)))
                .setAcceptSelfSignedCertificates(System.getProperties().getProperty(PREFIX_SYSTEM_PROPERTIES + ACCEPT_SELF_SIGNED_CERT_KEY) != null ? Boolean.valueOf(System.getProperties().getProperty(PREFIX_SYSTEM_PROPERTIES + ACCEPT_SELF_SIGNED_CERT_KEY)) : false)
                .setSearchForProxy(System.getProperties().getProperty(PREFIX_SYSTEM_PROPERTIES + USE_PROXY_KEY) != null ? Boolean.valueOf(System.getProperties().getProperty(PREFIX_SYSTEM_PROPERTIES + USE_PROXY_KEY)) : false);
    }
    
    @Override
    public String toString() {
        return String.format("IMAP connections for e-mail address %s", this.emailAddress);
    }
}
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

import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.ProxySelector;
import java.net.URL;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilderFactory;

import org.bihealth.mi.easybus.Participant;
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
    private static final long serialVersionUID = 3880443185633907293L;
    
    /** End-point for Mozilla auto-configuration service */
    public static final String   MOZILLA_AUTOCONF = "https://autoconfig.thunderbird.net/v1.1/";
    /** Standard port for IMAP */
    public static final int      DEFAULT_PORT_IMAP        = 993;
    /** Standard port for SMTP */
    public static final int      DEFAULT_PORT_SMTP        = 465;
    /** Regex to check dns validity */
    private static final Pattern regexDNS = Pattern.compile("^([a-zA-Z0-9]|[a-zA-Z0-9][a-zA-Z0-9\\-]{0,61}[a-zA-Z0-9])(\\.([a-zA-Z0-9]|[a-zA-Z0-9][a-zA-Z0-9\\-]{0,61}[a-zA-Z0-9]))*$");


    /**
     * Check server name
     * @param text
     */
    public static void checkDNSName(String text) {
        if (!regexDNS.matcher(text).matches()) {
            throw new IllegalArgumentException("DNS name invalid");
        }        
    }
    /**
     * Check
     * @param object
     */
    public static void checkPort(int object) {
        if (object < 1 || object > 65535) {
            throw new IllegalArgumentException("Port must not be between 1 and 65535");
        }
    }
    /** E-mail address */
    private String               emailAddress;
    /** Password */
    private String               password;
    /** IMAP server dns */
    private String               imapServer;
    /** Port of IMAP server */
    private int                  imapPort                 = DEFAULT_PORT_IMAP;

    /** SMTP server dns */
    private String               smtpServer;
        
    /** Port of SMTP server */
    private int                  smtpPort                 = DEFAULT_PORT_SMTP;

    /**
     * Creates a new instance
     * @param emailAddress Email address to use
     */
    public ConnectionIMAPSettings(String emailAddress) {
        
        // Checks
        checkNonNull(emailAddress);
        if (!Participant.isEmailValid(emailAddress)) {
            throw new IllegalArgumentException("Invalid e-mail address");
        }
        
        // Store
        this.emailAddress = emailAddress;
    }

    /**
     * Checks whether fields are empty
     * 
     * @return has null fields
     */
    public void check() {
        if (this.emailAddress == null || this.password == null || this.imapServer == null || this.smtpServer == null) {
            throw new IllegalArgumentException("Connection parameters must not be null");
        }
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

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        ConnectionIMAPSettings other = (ConnectionIMAPSettings) obj;
        if (emailAddress == null) {
            if (other.emailAddress != null) return false;
        } else if (!emailAddress.equals(other.emailAddress)) return false;
        if (imapPort != other.imapPort) return false;
        if (imapServer == null) {
            if (other.imapServer != null) return false;
        } else if (!imapServer.equals(other.imapServer)) return false;
        if (password == null) {
            if (other.password != null) return false;
        } else if (!password.equals(other.password)) return false;
        if (smtpPort != other.smtpPort) return false;
        if (smtpServer == null) {
            if (other.smtpServer != null) return false;
        } else if (!smtpServer.equals(other.smtpServer)) return false;
        return true;
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
        return password;
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
            
            // Success
            return true;
            
        } catch (Exception e) {
            // No success
            return false;
        }
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((emailAddress == null) ? 0 : emailAddress.hashCode());
        result = prime * result + imapPort;
        result = prime * result + ((imapServer == null) ? 0 : imapServer.hashCode());
        result = prime * result + ((password == null) ? 0 : password.hashCode());
        result = prime * result + smtpPort;
        result = prime * result + ((smtpServer == null) ? 0 : smtpServer.hashCode());
        return result;
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

        // Check
        checkNonNull(password); // TODO should null be possible for values?
        
        // Set
        this.password = password;

        // Done
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
}
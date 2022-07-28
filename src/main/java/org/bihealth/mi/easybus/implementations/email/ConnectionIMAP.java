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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.regex.Pattern;
import java.util.zip.GZIPOutputStream;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.math3.util.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bihealth.mi.easybus.BusException;
import org.bihealth.mi.easybus.MessageFilter;
import org.bihealth.mi.easybus.PerformanceListener;
import org.bihealth.mi.easysmpc.resources.Resources;

import jakarta.activation.DataHandler;
import jakarta.mail.Folder;
import jakarta.mail.Message;
import jakarta.mail.Message.RecipientType;
import jakarta.mail.MessagingException;
import jakarta.mail.Multipart;
import jakarta.mail.Session;
import jakarta.mail.Store;
import jakarta.mail.Transport;
import jakarta.mail.UIDFolder;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import jakarta.mail.util.ByteArrayDataSource;

/**
 * Defines the connection using IMAP to receive and SMTP to send e-mails
 * It is assumed that user credentials are the same to receive and to send e-mails
 * 
 * @author Felix Wirth
 * @author Fabian Prasser
 */
public class ConnectionIMAP extends ConnectionEmail {

    /** File name of the attached message */
    private static final String           FILENAME_MESSAGE = "message";
    /** Regex to check whether start of contains the e-mail subject prefix */
    private static Pattern                START_CONTAIN_PREFIX_PATTERN = Pattern.compile(".*" + EMAIL_SUBJECT_PREFIX.replace("[", "\\[") .replace("]", "\\]") + ".*");
    /** Logger */
    private static final Logger           LOGGER           = LogManager.getLogger(ConnectionIMAP.class);
    /** Properties t o receive */
    private final Properties              propertiesReceiving;
    /** Properties to send */
    private final Properties              propertiesSending;
    /** Store object to access e-mail server */
    private Store                         store;
    /** Folder receiving */
    private Folder                        folder;
    /** Session to send e-mails */
    private Session                       sessionSending;
    /** Session to receive e-mails */
    private Session                       sessionReceiving;
    /** Password of the user */
    private String                        receivingPassword;
    /** Performance listener */
    private transient PerformanceListener listener;
    /** Password of the sending user */
    private String                        sendingPassword;
   
    /**
     * Create a new instance
     * 
     * @param settings of mailbox
     * @param sharedMailbox - use shared mailbox or separated mailboxes
     * @param listener
     * @throws BusException
     */
    public ConnectionIMAP(ConnectionIMAPSettings settings,
                          boolean sharedMailbox) throws BusException {

        // Super
        super(sharedMailbox,
              settings.getIMAPEmailAddress(),
              settings.getSMTPEmailAddress(),
              settings.getIMAPUserName() != null ? settings.getIMAPUserName() : settings.getIMAPEmailAddress(),
              settings.getSMTPUserName() != null ? settings.getSMTPUserName() : settings.getSMTPEmailAddress(),
              settings.getPerformanceListener());
        
        // Check
        settings.check();
        if(settings.getIMAPPassword() == null || settings.getSMTPPassword() == null) {
            throw new IllegalArgumentException("Passwords cannot be null");
        }
        
        // Store
        this.receivingPassword = settings.getIMAPPassword();
        this.sendingPassword = settings.getSMTPPassword();
        this.listener = settings.getPerformanceListener();
        
        // Search for proxy
        Pair<String, Integer> proxy = null;
        if (settings.isSearchForProxy()) {
        	proxy = ConnectionIMAPProxy.getProxy(settings);
        }
        
        // Create properties of receiving connection
        this.propertiesReceiving = new Properties();
        this.propertiesReceiving.put("mail.store.protocol", "imap");
        this.propertiesReceiving.put("mail.user", getReceivingUserName());
        this.propertiesReceiving.put("mail.from", getReceivingEmailAddress());
        this.propertiesReceiving.put("mail.imap.host", settings.getIMAPServer());
        this.propertiesReceiving.put("mail.imap.port", String.valueOf(settings.getIMAPPort()));        
        this.propertiesReceiving.put("mail.imap.partialfetch", "false");
        this.propertiesReceiving.put("mail.imap.fetchsize", Resources.FETCH_SIZE_IMAP);
        this.propertiesReceiving.put(settings.isSSLTLSIMAP() ? "mail.imap.ssl.enable" : "mail.imap.starttls.enable", "true");        
        if (settings.isAcceptSelfSignedCertificates()) {
            this.propertiesReceiving.put("mail.imap.ssl.trust", "*");
        }
        
        // Set IMAP auth mechanisms if present in settings
        if (settings.getIMAPAuthMechanisms() != null) {
            this.propertiesReceiving.put("mail.imap.auth.mechanisms", settings.getIMAPAuthMechanisms());
        }        
        
        // Set proxy
        if (proxy != null) {
            this.propertiesReceiving.setProperty("mail.imap.proxy.host", proxy.getFirst());
            this.propertiesReceiving.setProperty("mail.imap.proxy.port", String.valueOf(proxy.getSecond()));
        }
        
        // Create properties of sending connection
        this.propertiesSending = new Properties();
        this.propertiesSending.put("mail.transport.protocol", "smtp");
        this.propertiesSending.put("mail.user", getSendingUserName());
        this.propertiesSending.put("mail.from", getSendingEmailAddress());        
        this.propertiesSending.put("mail.smtp.host", settings.getSMTPServer());
        this.propertiesSending.put("mail.smtp.port", String.valueOf(settings.getSMTPPort()));
        this.propertiesSending.put("mail.smtp.auth", "true");
        this.propertiesSending.put(settings.isSSLTLSSMTP() ? "mail.smtp.ssl.enable" : "mail.smtp.starttls.enable", "true");
        if(settings.isAcceptSelfSignedCertificates()) {
        	this.propertiesSending.put("mail.smtp.ssl.trust", "*");
        }

        // Set proxy
        if (proxy != null) {
            this.propertiesSending.setProperty("mail.smtp.proxy.host", proxy.getFirst());
            this.propertiesSending.setProperty("mail.smtp.proxy.port", String.valueOf(proxy.getSecond()));
        }
        
        // Set SMTP auth mechanisms if present in settings
        if (settings.getSMTPAuthMechanisms() != null) {
            this.propertiesSending.put("mail.smtp.auth.mechanisms", settings.getSMTPAuthMechanisms());
        }
    }

    /**
     * Checks if connections are working
     */
    public boolean checkConnection() {
                
        // Check receiving
        return isReceivingConnected();
    }

    /**
     * Transforms an object into a byte stream array
     * 
     * @param object
     * @return byte stream array
     */
    private byte[] getByteArrayOutputStream(Object object) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream ous = new ObjectOutputStream(new GZIPOutputStream(bos));
        ous.writeObject(object);
        ous.close();
        return bos.toByteArray();
    }

    @Override
    protected void close() {
        try {
            if (folder != null && folder.isOpen()) {
                folder.close(false);
            }
            
            if (store != null && store.isConnected()) {
                store.close();
            }                        
        } catch (MessagingException e) {
            // Ignore
            LOGGER.debug("Closing connection failed logged", new Date(), "Closing connection failed ", ExceptionUtils.getStackTrace(e));
        }
    }

    /**
     * Is there a working connection to receive
     * 
     * @return
     */
    protected boolean isReceivingConnected() {
        synchronized (propertiesReceiving) {
            // Make sure we are ready to go
            try {

                // Make sure we are ready to go
                Folder folder = null;
                if (sessionReceiving == null) {
                    sessionReceiving = Session.getInstance(propertiesReceiving, null);
                }

                // Create store
                Store store = sessionReceiving.getStore();

                // Connect store
                store.connect(getReceivingUserName(), receivingPassword);

                // Create new folder for every call to get latest state
                folder = store.getFolder("INBOX");
                if (!folder.exists()) {
                    folder.close(false);
                    store.close();
                    return false;
                }

                // Open folder
                folder.open(Folder.READ_WRITE);

                // Close
                folder.close(false);
                store.close();

                // Done
                return true;

            } catch (Exception e) {
                return false;
            }
        }
    }

    @Override
    protected List<ConnectionEmailMessage> list(MessageFilter filter) throws BusException, InterruptedException {
        
        synchronized (propertiesReceiving) {
            // Make sure we are ready to go

            try {
        
                // Create store
                Session sessionReceiving = Session.getInstance(propertiesReceiving);
                store = sessionReceiving.getStore();
                
                // Connect store
                store.connect(getReceivingUserName(), receivingPassword);
                
                // Create folder new for every call to get latest state
                folder = store.getFolder("INBOX");
                if (!folder.exists()) {
                    throw new BusException("Unable to identify inbox folder of mail box");
                }
                
                // Open folder
                folder.open(Folder.READ_WRITE);
    
            } catch (MessagingException e) {
                throw new BusException("Error establishing or keeping alive connection to mail server", e);
            }
            
            // Init
            List<ConnectionEmailMessage> result = new ArrayList<>();
            
            try {
                
                // Load messages
                for (Message message : folder.getMessages()) {
                    String subject = message.getSubject();
                    long uid = ((UIDFolder)  folder).getUID(message);
                    LOGGER.debug("Message considered logged", new Date(), "Message considered", uid, subject);
                    // Check for interrupt
                    if (Thread.interrupted()) { 
                        throw new InterruptedException();
                    }

                    // Select relevant messages
                    try {
                        if (START_CONTAIN_PREFIX_PATTERN.matcher(subject).matches() &&
                                (filter == null || filter.accepts(subject))) {                                
                                LOGGER.debug("Message received logged", new Date(), "Message received", uid, subject);
                                result.add(new ConnectionEmailMessage(message, folder));
                        }
                    } catch (Exception e) {
                        // Ignore, as this may be a result of non-transactional properties of the IMAP protocol
                        LOGGER.debug("message.getSubject() failed logged", new Date(), "message.getSubject() failed", ExceptionUtils.getStackTrace(e));
                    }
                }
                
            } catch (MessagingException e) {
                throw new BusException("Cannot read messages", e);
            }
            
            // Done
            return result;
        }
    }
    
    @Override
    protected void send(String recipient, String subject, String body, Object attachment) throws BusException {

        synchronized(propertiesSending) {
    
            // Make sure we are ready to go
            if (sessionSending == null) {
                sessionSending = Session.getInstance(propertiesSending, null);
            }
    
            try {
                
                // Create message
                MimeMessage email = new MimeMessage(sessionSending);
               
                // Add sender and recipient
                email.setRecipient(RecipientType.TO, new InternetAddress(recipient));
                email.setSender(new InternetAddress(getSendingEmailAddress()));
                email.setFrom(new InternetAddress(getSendingEmailAddress()));
                email.setSubject(subject);
                
                // Add body
                MimeBodyPart mimeBodyPart = new MimeBodyPart();
                mimeBodyPart.setDisposition(MimeBodyPart.INLINE);
                mimeBodyPart.setContent(body, "text/plain");
                Multipart multipart = new MimeMultipart();
                multipart.addBodyPart(mimeBodyPart);
    
                // Add attachment
                long attachmentSize = 0;
                if (attachment != null) {
                    mimeBodyPart = new MimeBodyPart();
                    mimeBodyPart.setDisposition(MimeBodyPart.ATTACHMENT);
                    byte[] attachmentBytes = getByteArrayOutputStream(attachment);
                    mimeBodyPart.setDataHandler(new DataHandler(new ByteArrayDataSource(attachmentBytes, "application/octet-stream")));
                    mimeBodyPart.setFileName(FILENAME_MESSAGE);
                    multipart.addBodyPart(mimeBodyPart);
                    attachmentSize = attachmentBytes.length;
                }
                
                // Compose message
                email.setContent(multipart);
    
                // Send
                Transport.send(email, getSendingUserName(), sendingPassword);
                if (listener != null) {
                    listener.messageSent(attachmentSize);
                }
                LOGGER.debug("Message sent logged", new Date(), "Message sent", subject);
            } catch (Exception e) {
                throw new BusException("Unable to send message", e);
            }
        }
    }
}
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
import java.util.zip.GZIPOutputStream;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.math3.util.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bihealth.mi.easybus.Bus;
import org.bihealth.mi.easybus.BusException;
import org.bihealth.mi.easybus.MessageFilter;
import org.bihealth.mi.easysmpc.resources.Resources;

import jakarta.activation.DataHandler;
import jakarta.mail.Authenticator;
import jakarta.mail.Folder;
import jakarta.mail.Message;
import jakarta.mail.Message.RecipientType;
import jakarta.mail.MessagingException;
import jakarta.mail.Multipart;
import jakarta.mail.PasswordAuthentication;
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
    private static final String FILENAME_MESSAGE = "message";
    /** Properties */
    private final Properties    propertiesReceiving;
    /** Properties */
    private final Properties    propertiesSending;
    /** Store object to access e-mail server */
    private Store               store;
    /** Folde receiving*/
    private Folder folder;
    /** Session to send e-mails */
    private Session             session;
    /** Password of the user */
    private String              password;
    /** Logger */
    private static final Logger logger = LogManager.getLogger(ConnectionIMAP.class);
    
    /**
     * Create a new instance
     * 
     * @param settings
     * @param sharedMailbox
     * @throws BusException
     */
    public ConnectionIMAP(ConnectionIMAPSettings settings, boolean sharedMailbox) throws BusException {
        
        // Super
        super(sharedMailbox, settings.getEmailAddress());
        
        // Check
        settings.check();
        
        // Store
        this.password = settings.getPassword();
        
        // Search for proxy
        Pair<String, Integer> proxy = ConnectionIMAPProxy.getProxy(settings);
        
        // Create properties of receiving connection
        this.propertiesReceiving = new Properties();
        this.propertiesReceiving.put("mail.store.protocol", "imap");
        this.propertiesReceiving.put("mail.user", getEmailAddress());
        this.propertiesReceiving.put("mail.from", getEmailAddress());
        this.propertiesReceiving.put("mail.imap.host", settings.getIMAPServer());
        this.propertiesReceiving.put("mail.imap.port", String.valueOf(settings.getIMAPPort()));        
        this.propertiesReceiving.put("mail.imap.partialfetch", "false");
        this.propertiesReceiving.put("mail.imap.fetchsize", Resources.FETCH_SIZE_IMAP);
      //this.propertiesReceiving.put("mail.imap.ssl.enable", "true");
        
        // Set proxy
        if (proxy != null) {
            this.propertiesReceiving.setProperty("mail.imap.proxy.host", proxy.getFirst());
            this.propertiesReceiving.setProperty("mail.imap.proxy.port", String.valueOf(proxy.getSecond()));
        }
        
        // Create properties of sending connection
        this.propertiesSending = new Properties();
        this.propertiesSending.put("mail.user", getEmailAddress());
        this.propertiesSending.put("mail.from", getEmailAddress());
        this.propertiesSending.put("mail.transport.protocol", "stmp");
        this.propertiesSending.put("mail.smtp.host", settings.getSMTPServer());
        this.propertiesSending.put("mail.smtp.port", String.valueOf(settings.getSMTPPort()));
        this.propertiesSending.put("mail.smtp.auth", "true");
        //this.propertiesSending.put("mail.smtp.ssl.enable", "true");

        // Set proxy
        if (proxy != null) {
            this.propertiesSending.setProperty("mail.smtp.proxy.host", proxy.getFirst());
            this.propertiesSending.setProperty("mail.smtp.proxy.port", String.valueOf(proxy.getSecond()));
        }
    }

    /**
     * Checks if connections are working
     */
    public boolean checkConnection() {
        
        // Check sending
        if (!isSendingConnected()) {
            return false;
        }
        
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
        } catch (Exception e) {
            // Ignore
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
                store.connect(getEmailAddress(), password);
                
                // Create folder new for every call to get latest state
                folder = store.getFolder("INBOX");
                if (!folder.exists()) {
                    throw new BusException("Unable to identify inbox folder of mail box");
                }
                
                // Open folder
                folder.open(Folder.READ_WRITE);
    
            } catch (Exception e) {
                throw new BusException("Error establishing or keeping alive connection to mail server", e);
            }
            
            // Init
            List<ConnectionEmailMessage> result = new ArrayList<>();
            
            try {
                
                // Load messages
                for (Message message : folder.getMessages()) {
                    String subject = message.getSubject();
                    long uid = ((UIDFolder)  folder).getUID(message);
                    logger.debug("Message considered logged", new Date(), "Message considered", uid, subject);
                    // Check for interrupt
                    if (Thread.interrupted()) { 
                        throw new InterruptedException();
                    }

                    // Select relevant messages
                    try {
                        if (subject.startsWith(EMAIL_SUBJECT_PREFIX) &&
                                (filter == null || filter.accepts(subject))) {                                
                                logger.debug("Message received logged", new Date(), "Message received", uid, subject);
                                result.add(new ConnectionEmailMessage(message, folder, store));
                        }
                    } catch (Exception e) {
                        // Ignore, as this may be a result of non-transactional properties of the IMAP protocol
                        logger.debug("message.getSubject() failed logged", new Date(), "message.getSubject() failed", ExceptionUtils.getStackTrace(e));
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
    protected synchronized void send(String recipient, String subject, String body, Object attachment) throws BusException {

        synchronized(propertiesSending) {
    
            // Make sure we are ready to go
            try {
                if (session == null) {
                    session = Session.getInstance(propertiesSending);
                }
            } catch (Exception e) {
                throw new BusException("Error establishing or keeping alive connection to mail server", e);
            }
    
            try {
                
                // Create message
                MimeMessage email = new MimeMessage(session);
               
                // Add sender and recipient
                email.setRecipient(RecipientType.TO, new InternetAddress(recipient));
                email.setSender(new InternetAddress(getEmailAddress()));
                email.setFrom(new InternetAddress(getEmailAddress()));
                email.setSubject(subject);
                
                // Add body
                MimeBodyPart mimeBodyPart = new MimeBodyPart();
                mimeBodyPart.setDisposition(MimeBodyPart.INLINE);
                mimeBodyPart.setContent(body, "text/plain");
                Multipart multipart = new MimeMultipart();
                multipart.addBodyPart(mimeBodyPart);
    
                // Add attachment
                if (attachment != null) {
                    mimeBodyPart = new MimeBodyPart();
                    mimeBodyPart.setDisposition(MimeBodyPart.ATTACHMENT);
                    byte[] attachmentBytes = getByteArrayOutputStream(attachment);
                    mimeBodyPart.setDataHandler(new DataHandler(new ByteArrayDataSource(attachmentBytes, "application/octet-stream")));
                    mimeBodyPart.setFileName(FILENAME_MESSAGE);
                    multipart.addBodyPart(mimeBodyPart);
                    
                    // Add statistics
                    Bus.numberMessagesSent.incrementAndGet();
                    Bus.totalSizeMessagesSent.addAndGet(attachmentBytes.length);
                }
                
                // Compose message
                email.setContent(multipart);
    
                // Send
                Transport.send(email, getEmailAddress(), password);                
                logger.debug("Message sent logged", new Date(), "Message sent logged", subject);
            } catch (Exception e) {
                throw new BusException("Unable to send message", e);
            }
        }
    }

    @Override
    protected synchronized boolean isReceivingConnected() {
        try {

            // Prepare
            Folder folder = null;

            // Create store
            Session sessionReceiving = Session.getInstance(propertiesReceiving);
            Store store = sessionReceiving.getStore();

            // Connect store
            store.connect(getEmailAddress(), password);

            // Create folder new for every call to get latest state
            folder = store.getFolder("INBOX");
            if (!folder.exists()) { return false; }

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
    
    /**
     * Is sending connected?
     * @return
     */
    protected synchronized boolean isSendingConnected() {
        try {
            // TODO Check and remove Authenticator
            // Check sending e-mails
            Session.getInstance(propertiesSending, new Authenticator() {
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(getEmailAddress(), password);
                }
            });

            // Done
            return true;

        } catch (Exception e) {
            return false;
        }
    }
}
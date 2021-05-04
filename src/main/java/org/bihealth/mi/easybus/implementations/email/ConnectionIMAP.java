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
import java.util.List;
import java.util.Properties;
import java.util.zip.GZIPOutputStream;

import javax.activation.DataHandler;
import javax.mail.Authenticator;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMessage.RecipientType;
import javax.mail.internet.MimeMultipart;
import javax.mail.util.ByteArrayDataSource;

import org.apache.commons.math3.util.Pair;
import org.bihealth.mi.easybus.Bus;
import org.bihealth.mi.easybus.BusException;
import org.bihealth.mi.easysmpc.resources.Resources;

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
    /** Session to send e-mails */
    private Session             session;
    /** Password of the user */
    private String              password;

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
        this.propertiesReceiving.put("mail.imap.host", settings.getIMAPServer());
        this.propertiesReceiving.put("mail.imap.port", String.valueOf(settings.getIMAPPort()));
        this.propertiesReceiving.put("mail.imap.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        this.propertiesReceiving.put("mail.imap.socketFactory.fallback", "false");
        this.propertiesReceiving.put("mail.imap.socketFactory.port", String.valueOf(settings.getIMAPPort()));
        this.propertiesReceiving.put("mail.imap.partialfetch", "false");
        this.propertiesReceiving.put("mail.imap.fetchsize", Resources.FETCH_SIZE_IMAP);
        
        // Set proxy
        if (proxy != null) {
            this.propertiesReceiving.setProperty("mail.imap.proxy.host", proxy.getFirst());
            this.propertiesReceiving.setProperty("mail.imap.proxy.port", String.valueOf(proxy.getSecond()));
        }
        
        // Create properties of sending connection
        this.propertiesSending = new Properties();
        this.propertiesSending.put("mail.smtp.host", settings.getSMTPServer());
        this.propertiesSending.put("mail.smtp.port", String.valueOf(settings.getSMTPPort()));
        this.propertiesSending.put("mail.smtp.socketFactory.port", String.valueOf(settings.getSMTPPort()));
        this.propertiesSending.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        this.propertiesSending.put("mail.smtp.socketFactory.fallback", "false");
        this.propertiesSending.put("mail.smtp.auth", "true");

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
            if (store != null && store.isConnected()) {
                store.close();
            }
        } catch (Exception e) {
            // Ignore
        }
    }
    
    @Override
    protected List<ConnectionEmailMessage> list() throws BusException, InterruptedException {
        
        synchronized (propertiesReceiving) {
                
            // Make sure we are ready to go
            Folder folder = null;
            try {
        
                // Create store
                if (store == null) {
                    Session sessionReceiving = Session.getInstance(propertiesReceiving);
                    store = sessionReceiving.getStore("imap");
                }
                
                // Connect store
                if (!store.isConnected()) {
                    store.connect(getEmailAddress(), password);
                }
                
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

                    // Check for interrupt
                    if (Thread.interrupted()) { 
                        throw new InterruptedException();
                    }

                    // Select relevant messages
                    try {
                        if (message.getSubject().startsWith(EMAIL_SUBJECT_PREFIX)) {
                            result.add(new ConnectionEmailMessage(message, folder));   
                        }
                    } catch (Exception e) {
                        // Ignore, as this may be a result of non-transactional properties of the IMAP protocol
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
                    session = Session.getInstance(propertiesSending, new Authenticator() {
                        protected PasswordAuthentication getPasswordAuthentication() {
                            return new PasswordAuthentication(getEmailAddress(), password);
                        }
                    });
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
                Transport.send(email);
                
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
            Store store = sessionReceiving.getStore("imap");

            // Connect store
            store.connect(getEmailAddress(), password);

            // Create folder new for every call to get latest state
            folder = store.getFolder("INBOX");
            if (!folder.exists()) { return false; }

            // Open folder
            folder.open(Folder.READ_WRITE);

            // Close
            store.close();

            // Done
            return true;

        } catch (Exception e) {
            return false;
        }
    }
    
    protected synchronized boolean isSendingConnected() {
        try {
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
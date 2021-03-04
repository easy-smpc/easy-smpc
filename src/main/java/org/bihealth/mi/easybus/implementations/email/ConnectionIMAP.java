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

import org.bihealth.mi.easybus.BusException;

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
        
        this.password = settings.getPassword();
        
        // Create properties of receiving connection
        this.propertiesReceiving = new Properties();
        propertiesReceiving.put("mail.imap.host", settings.getIMAPServer());
        propertiesReceiving.put("mail.imap.port", String.valueOf(settings.getIMAPPort()));
        propertiesReceiving.put("mail.imap.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        propertiesReceiving.put("mail.imap.socketFactory.fallback", "false");
        propertiesReceiving.put("mail.imap.socketFactory.port", String.valueOf(settings.getIMAPPort()));
        
        // Create properties of sending connection
        this.propertiesSending = new Properties();
        propertiesSending.put("mail.smtp.host", settings.getSMTPServer());
        propertiesSending.put("mail.smtp.port", String.valueOf(settings.getSMTPPort()));
        propertiesSending.put("mail.smtp.socketFactory.port", String.valueOf(settings.getSMTPPort()));
        propertiesSending.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        propertiesSending.put("mail.smtp.socketFactory.fallback", "false");
        propertiesSending.put("mail.smtp.auth", "true");
    }

    /**
     * Checks if connections are working
     * @throws BusException
     */
    public void checkConnection() throws BusException {
        
        try {
            
            // Check receiving e-mails 
            Folder folder = null;

            // Create store
            Session sessionReceiving = Session.getInstance(propertiesReceiving);
            store = sessionReceiving.getStore("imap");

            // Connect store
            store.connect(getEmailAddress(), password);

            // Create folder new for every call to get latest state
            folder = store.getFolder("INBOX");
            if (!folder.exists()) {
                throw new BusException("Unable to identify inbox folder of mail box");
            }

            // Open folder
            folder.open(Folder.READ_WRITE);
            
            
            // Check sending e-mails 
            session = Session.getInstance(propertiesSending, new Authenticator() {
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(getEmailAddress(), password);
                }
            });
            // TODO check sending
        } catch (MessagingException | BusException e) {
            throw new BusException("Check for connections was not successful");
        }
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
    protected List<ConnectionEmailMessage> list() throws BusException {
        
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
                mimeBodyPart = new MimeBodyPart();
                mimeBodyPart.setDisposition(MimeBodyPart.ATTACHMENT);
                mimeBodyPart.setDataHandler(new DataHandler(new ByteArrayDataSource(getByteArrayOutputStream(attachment),"application/octet-stream")));
                mimeBodyPart.setFileName(FILENAME_MESSAGE);
                multipart.addBodyPart(mimeBodyPart);
                
                // Compose message
                email.setContent(multipart);
    
                // Send
                Transport.send(email);
                
            } catch (Exception e) {
                throw new BusException("Unable to send message", e);
            }
        }
    }
}
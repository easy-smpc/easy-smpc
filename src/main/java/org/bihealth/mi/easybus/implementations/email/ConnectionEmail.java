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

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.zip.GZIPInputStream;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bihealth.mi.easybus.BusException;
import org.bihealth.mi.easybus.BusMessage;
import org.bihealth.mi.easybus.BusMessageFragment;
import org.bihealth.mi.easybus.MessageFilter;
import org.bihealth.mi.easybus.Participant;
import org.bihealth.mi.easybus.PerformanceListener;
import org.bihealth.mi.easybus.Scope;

import jakarta.mail.BodyPart;
import jakarta.mail.Flags.Flag;
import jakarta.mail.MessagingException;
import jakarta.mail.Multipart;
import jakarta.mail.internet.MimeBodyPart;

/**
 * Abstract class for e-mail connections
 * 
 * @author Fabian Prasser
 * @author Felix Wirth
 */
public abstract class ConnectionEmail {

    /**
     * Internal message used by email connections
     * 
     * @author Fabian Prasser
     */
    protected class ConnectionEmailMessage {

        /** Message */
        private final jakarta.mail.Message message;

        /** Text */
        private String                     text       = null;

        /** Attachment */
        private Object                     attachment = null;
    
        /**
         * Creates a new instance
         * @param message
         * @param folder
         */
        public ConnectionEmailMessage(jakarta.mail.Message message) {

            // Store
            this.message = message;
            long size = 0;
    
            try {
                
                // Extract parts
                text = message.getSubject();
                Multipart multipart = (Multipart) message.getContent();
                if (multipart.getCount() == 2) {
                    
                    // Obtain body and attachment
                    for (int i = 0; i < 2; i++) {
                        BodyPart part = multipart.getBodyPart(i);
                        if (part != null && part.getDisposition() != null && part.getDisposition().equalsIgnoreCase(MimeBodyPart.ATTACHMENT)) {
                          attachment = getObject(((MimeBodyPart)part).getInputStream());
                          size += ((MimeBodyPart)part).getSize();
                      }                        
                    }
                }
            } catch (Exception e) {
                // Ignore, as this may be a result of non-transactional properties of the IMAP protocol
                LOGGER.debug("Load message failed logged", new Date(), "load message failed", ExceptionUtils.getStackTrace(e));
            }
            
            // Pass to listener
            if (listener != null) {
                listener.messageReceived(size);
            }
        }
    
        /**
         * Create object from byte stream
         * 
         * @param inputStream
         * @return message
         * @throws IOException 
         * @throws ClassNotFoundException 
         */
        private Object getObject(InputStream inputStream) throws IOException, ClassNotFoundException {
            BufferedInputStream bufferedis = new BufferedInputStream(inputStream);
            ByteArrayInputStream bis = new ByteArrayInputStream(bufferedis.readAllBytes());
            ObjectInputStream ois = new ObjectInputStream(new GZIPInputStream(bis));
            Object result = ois.readObject();
            ois.close();
            return result;
        }
        
        /** 
         * Deletes the message on the server
         */
        protected void delete() {
            try {
                message.setFlag(Flag.DELETED, true);
            } catch (MessagingException e) {
                LOGGER.debug("Delete failed logged", new Date(), "delete failed", ExceptionUtils.getStackTrace(e));
                // Ignore, as this may be a result of non-transactional properties of the IMAP protocol
            }
        }    
        /** 
         * Expunges all deleted messages on the server
         */
        protected void expunge() {
            try {
                if (message.getFolder() != null && message.getFolder().isOpen()) {
                    message.getFolder().expunge();
                }
            } catch (MessagingException e) {
                LOGGER.debug("Expunge failed logged", new Date(), "expunge failed", ExceptionUtils.getStackTrace(e));
                // Ignore, as this may be a result of non-transactional properties of the IMAP protocol
            }
        }        
    
        /**
         * Returns the attachment
         * @return the attachment
         */
        protected Object getAttachment() {
            return attachment;
        }
    
        /**
         * Returns the text
         * @return
         */
        protected String getText() {
            return text;
        }
    }

    /** E-mail subject category prefix */
    public static final String EMAIL_SUBJECT_PREFIX        = "[EasySMPC]";
    /** String to format the e-mail subject */
    public static final String EMAIL_SUBJECT_RECEIVER      = EMAIL_SUBJECT_PREFIX + "Message in scope \"%s\" for recipient \"%s\"";
    /** String to format the e-mail for a scope */
    public static final String EMAIL_SUBJECT_SCOPE         = EMAIL_SUBJECT_PREFIX + "Message in scope \"%s\"";
    /** String indicating start of scope */
    public static final String SCOPE_NAME_START_TAG        = "BEGIN_NAME_SCOPE";
    /** String indicating end of scope */
    public static final String SCOPE_NAME_END_TAG          = "END_NAME_SCOPE";
    /** String indicating start of participant name */
    public static final String PARTICIPANT_NAME_START_TAG  = "BEGIN_NAME_PARTICIPANT";
    /** String indicating end of participant name */
    public static final String PARTICIPANT_NAME_END_TAG    = "END_NAME_PARTICIPANT";
    /** String indicating start of participant e-mail address */
    public static final String PARTICIPANT_EMAIL_START_TAG = "BEGIN_EMAIL_PARTICIPANT";
    /** String indicating end of participant address */
    public static final String PARTICIPANT_EMAIL_END_TAG   = "END_EMAIL_PARTICIPANT";
    /** Logger */
    private static final Logger LOGGER = LogManager.getLogger(ConnectionEmail.class);

    /**
     * Generates the subject line
     * 
     * @param scope
     * @param receiver
     * @return
     */
    public static String createSubject(Scope scope, Participant receiver) {
        return EMAIL_SUBJECT_PREFIX + SCOPE_NAME_START_TAG + scope.getName() + SCOPE_NAME_END_TAG +
               " " + PARTICIPANT_NAME_START_TAG + receiver.getName() + PARTICIPANT_NAME_END_TAG +
               " " + PARTICIPANT_EMAIL_START_TAG + receiver.getEmailAddress() +
               PARTICIPANT_EMAIL_END_TAG;
    }

    /**
     * Create participant from body
     * 
     * @param body
     * @return participant 
     */
    public static Participant getParticipant(String body) {

        // Check
        if(!body.contains(PARTICIPANT_NAME_END_TAG) || !body.contains(PARTICIPANT_NAME_END_TAG) ||
           !body.contains(PARTICIPANT_EMAIL_END_TAG) || !body.contains(PARTICIPANT_EMAIL_END_TAG)) {
           return null;
        }
        
        // Extract
        String name = body.substring(body.indexOf(PARTICIPANT_NAME_START_TAG) + PARTICIPANT_NAME_START_TAG.length(), body.indexOf(PARTICIPANT_NAME_END_TAG));
        String email = body.substring(body.indexOf(PARTICIPANT_EMAIL_START_TAG) + PARTICIPANT_EMAIL_START_TAG.length(), body.indexOf(PARTICIPANT_EMAIL_END_TAG));
        
        // Check
        if (name.isEmpty() || email.isEmpty() || !Participant.isEmailValid(email)) {
            return null;
        } else {
            try {
                return new Participant(name, email);
            } catch (BusException e) {
                return null;
            }
        }
    }

    /**
     * Create scope from body
     * 
     * @param body
     * @return scope
     */
    public static Scope getScope(String body) {

        // Check
        if (!body.contains(SCOPE_NAME_START_TAG) || !body.contains(SCOPE_NAME_END_TAG)) {
            return null;
        }

        // Extract
        String scope = body.substring(body.indexOf(SCOPE_NAME_START_TAG) +
                                      SCOPE_NAME_START_TAG.length(),
                                      body.indexOf(SCOPE_NAME_END_TAG));

        // Check
        if (scope.length() == 0) {
            return null;
        } else {
            return new Scope(scope);
        }
    }

    /** Use several or exactly one mail box for the bus */
    private boolean             sharedMailbox;
    /** Mail address of receiving user */
    private String              receivingEmailAddress;
    /** Performance listener */
    private PerformanceListener listener;
    /** Mail address of sending user */
    private String              sendingEmailAddress;
    /** Receiving user name */
    private String              receivingUserName;
    /** Sending user name */
    private String              sendingUserName;
    
    /**
     * Creates a new instance with same mail address to receive and to send
     * 
     * @param sharedMailBox
     * @param emailAddress
     * @throws BusException
     */
    protected ConnectionEmail(boolean sharedMailBox, String emailAddress) {
        this(sharedMailBox, emailAddress, null);
    }
    
    /**
     * Creates a new instance with same mail address and user name to receive and to send and a performance listener
     * 
     * @param sharedMailBox
     * @param emailAddress
     * @param listener
     * @throws BusException
     */
    protected ConnectionEmail(boolean sharedMailBox, String emailAddress, PerformanceListener listener) {
        this(sharedMailBox, emailAddress, emailAddress, listener);
    }
    
    /**
     * Creates a new instance
     * 
     * @param sharedMailBox
     * @param receivingEmailAddress
     * @param sendingEmailAddress
     * @param listener
     * @throws BusException
     */
    protected ConnectionEmail(boolean sharedMailBox, String receivingEmailAddress, String sendingEmailAddress, PerformanceListener listener) {
        this(sharedMailBox, receivingEmailAddress, sendingEmailAddress, null, null, listener);
    }
    
    /**
     * Creates a new instance
     * 
     * @param sharedMailBox
     * @param receivingEmailAddress
     * @param sendingEmailAddress
     * @param receivingUserName - only necessary if deviates from receivingEmailAddress
     * @param sendingUserName - only necessary if deviates from sendingEmailAddress
     * @param listener
     */
    protected ConnectionEmail(boolean sharedMailBox,
                              String receivingEmailAddress,
                              String sendingEmailAddress,
                              String receivingUserName,
                              String sendingUserName,
                              PerformanceListener listener) {
        // Check
        if (receivingEmailAddress == null) {
            throw new NullPointerException("Email address must not be null");
        }
        if (sendingEmailAddress == null) {
            throw new NullPointerException("Email address must not be null");
        }
        
        // Store
        this.sharedMailbox = sharedMailBox;
        this.receivingEmailAddress = receivingEmailAddress;
        this.sendingEmailAddress = sendingEmailAddress;
        this.receivingUserName = receivingUserName;
        this.sendingUserName = sendingUserName;
        this.listener = listener;
    }
    
    /** 
     * Close connection
     */
    protected abstract void close();

    /**
     * Returns the associated email address for sending
     * @return
     */
    protected String getReceivingEmailAddress() {
        return this.receivingEmailAddress;
    }
    
    /**
     * Returns the user name for receiving
     * 
     * @return
     */
    protected String getReceivingUserName() {
        return this.receivingUserName != null ? this.receivingUserName : this.receivingEmailAddress;
    }
    
    /**
     * Returns the associated email address for receiving
     * @return
     */
    protected String getSendingEmailAddress() {
        return this.sendingEmailAddress;
    }
    
    /**
     * Returns the user name for sending
     * 
     * @return
     */
    protected String getSendingUserName() {
        return sendingUserName;
    }
    
    /**
     * Lists all relevant e-mails
     * @param filter
     * @return relevant e-mails
     * @throws BusException 
     * @throws InterruptedException 
     */
    protected abstract List<ConnectionEmailMessage> list(MessageFilter filter) throws BusException, InterruptedException;
  
    /**
     * Receives a list of relevant messages
     * @param filter 
     * @return
     * @throws InterruptedException 
     */
    protected List<BusMessage> receive(MessageFilter filter) throws BusException, InterruptedException {
        
        // Prepare
        List<BusMessage> result = new ArrayList<>();
        
        try {
            
            // Receive messages
            for (ConnectionEmailMessage message : list(filter)) {

                // Check for interrupt
                if (Thread.interrupted()) {
                    throw new InterruptedException();
                }

                // Get content
                String text = message.getText();

                // Check for interrupt
                if (Thread.interrupted()) {
                    throw new InterruptedException();
                }

                Object attachment = message.getAttachment();

                if (text == null || attachment == null) {
                    LOGGER.debug("Malformated message skipped logged", new Date(), "Malformated message skipped");
                    continue;
                }
                
                // Extract scope and participant
                Scope scope = getScope(text);

                // Check for interrupt
                if (Thread.interrupted()) {
                    throw new InterruptedException();
                }

                Participant participant = getParticipant(text);

                // Check for interrupt
                if (Thread.interrupted()) {
                    throw new InterruptedException();
                }                        

                if (scope == null || participant == null) {
                    LOGGER.debug("Malformated message skipped logged", new Date(), "Malformated message skipped");
                    continue;
                }
                
                // Pass on
                final ConnectionEmailMessage _message = message;

                if (attachment instanceof BusMessageFragment) {
                    result.add(new BusMessageFragment((BusMessageFragment)attachment) {
                        
                        /** SVUID */
                        private static final long serialVersionUID = 2663872683179080953L;
                        
                        @Override
                        public void delete() throws BusException {
                            _message.delete();
                        }
                        @Override
                        public void expunge() throws BusException {
                            _message.expunge();
                        }
                    });
                } else {
                    result.add(new BusMessage((BusMessage)attachment) {
                        /** SVUID */
                        private static final long serialVersionUID = -2294147052332533758L;
                        @Override
                        public void delete() throws BusException {
                            _message.delete();
                        }
                        @Override
                        public void expunge() throws BusException {
                            _message.expunge();
                        }
                    });
                }
            }

        } catch (BusException e) {
            throw new BusException("Error receiving message", e);
        }

        // Done
        return result;
    }
    
    
    /**
     * Send message to participant
     * @param message
     * @throws BusException
     */
    protected void send(BusMessage message) throws BusException {
        
        // Prepare
        Participant receiver = message.getReceiver();
        Scope scope = message.getScope();
        
        // Recipient
        String recipient = sharedMailbox ? getReceivingEmailAddress() : receiver.getEmailAddress();
        
        // Subject
        String subject = createSubject(scope, receiver);       
  
        // Body
        String body = SCOPE_NAME_START_TAG + scope.getName() + SCOPE_NAME_END_TAG + "\n" + 
                      PARTICIPANT_NAME_START_TAG + receiver.getName() + PARTICIPANT_NAME_END_TAG + "\n" + 
                      PARTICIPANT_EMAIL_START_TAG + receiver.getEmailAddress() + PARTICIPANT_EMAIL_END_TAG;
        
        // Send
        this.send(recipient, subject, body, message);
    }
    
    /** 
     * Send email
     * @param recipient
     * @param subject
     * @param body
     * @param attachment
     * @throws BusException
     */
    protected abstract void send(String recipient, String subject, String body, Object attachment) throws BusException;
}
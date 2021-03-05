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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;

import javax.mail.BodyPart;
import javax.mail.Flags.Flag;
import javax.mail.Folder;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.internet.MimeBodyPart;

import org.bihealth.mi.easybus.BusException;
import org.bihealth.mi.easybus.Message;
import org.bihealth.mi.easybus.Participant;
import org.bihealth.mi.easybus.Scope;

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
    protected static class ConnectionEmailMessage {

        /** Message */
        private final javax.mail.Message message;

        /** Folder */
        private final Folder             folder;

        /** Text */
        private String                   text       = null;

        /** Attachment */
        private Object                   attachment = null;
    
        /**
         * Creates a new instance
         * @param message
         * @param folder
         */
        public ConnectionEmailMessage(javax.mail.Message message, Folder folder) {
            
            // Store
            this.message = message;
            this.folder = folder;
    
            try {
    
                // Extract parts
                Multipart multipart = (Multipart) message.getContent();
                if (multipart.getCount() == 2) {
                    
                    // Obtain body and attachment
                    for (int i = 0; i < 2; i++) {
                        BodyPart part = multipart.getBodyPart(i);
                        if (part != null && part.getDisposition().equalsIgnoreCase(MimeBodyPart.INLINE)) {
                            text = (String)((MimeBodyPart)part).getContent();
                        } else if (part != null && part.getDisposition().equalsIgnoreCase(MimeBodyPart.ATTACHMENT)) {
                            attachment = getObject(((MimeBodyPart)part).getInputStream());
                        }
                    }
                }
            } catch (Exception e) {
                // Ignore, as this may be a result of non-transactional properties of the IMAP protocol
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
            ByteArrayInputStream bos = new ByteArrayInputStream(inputStream.readAllBytes());
            ObjectInputStream ois = new ObjectInputStream(new GZIPInputStream(bos));
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
                // Ignore, as this may be a result of non-transactional properties of the IMAP protocol
            }
        }
    
        /** 
         * Expunges all deleted messages on the server
         */
        protected void expunge() {
    
            try {
                if (folder != null && folder.isOpen()) {
                    folder.close(true);
                }
            } catch (MessagingException e) {
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

    /** Use several or exactly one mail box for the bus */
    private boolean sharedMailbox;
    
    /** Mail address of the user */
    private String  emailAddress;

    /**
     * Creates a new instance
     * @param sharedMailBox
     * @param emailAddress
     * @throws BusException
     */
    protected ConnectionEmail(boolean sharedMailBox, String emailAddress) {
        // Check
        if (emailAddress == null) {
            throw new NullPointerException("Email address must not be null");
        }
        this.sharedMailbox = sharedMailBox;
        this.emailAddress = emailAddress;
    }
    
    /**
     * Create participant from body
     * 
     * @param body
     * @return participant
     * @throws BusException 
     */
    private Participant getParticipant(String body) throws BusException {

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
            return new Participant(name, email);
        }
    }
    
    /**
     * Create scope from body
     * 
     * @param body
     * @return scope
     * @throws BusException 
     */
    private Scope getScope(String body) throws BusException {

        // Check
        if(!body.contains(SCOPE_NAME_START_TAG) || !body.contains(SCOPE_NAME_END_TAG)){
            return null;
        }
        
        // Extract
        String scope = body.substring(body.indexOf(SCOPE_NAME_START_TAG) + SCOPE_NAME_START_TAG.length(), body.indexOf(SCOPE_NAME_END_TAG));
        
        // Check
        if (scope.length() == 0) {
            return null;
        } else {
            return new Scope(scope);
        }
    }
    
    /**
     * Returns the associated email address
     * @return
     */
    protected String getEmailAddress() {
        return this.emailAddress;
    }

    /**
     * Lists all relevant e-mails
     * 
     * @return relevant e-mails
     * @throws BusException 
     * @throws InterruptedException 
     */
    protected abstract List<ConnectionEmailMessage> list() throws BusException, InterruptedException;

    /**
     * Receives a list of potentially relevant messages
     * @return
     * @throws InterruptedException 
     */
    protected List<BusEmail.BusEmailMessage> receive() throws BusException, InterruptedException {
        
        // Prepare
        List<BusEmail.BusEmailMessage> result = new ArrayList<>();
        
        try {
            
            // Receive messages
            for (ConnectionEmailMessage message : list()) {

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
                
                // TODO: Is this a good idea? Delete malformed messages
                if (text == null || attachment == null) {
                    message.delete();
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
    
                // TODO: Is this a good idea? Delete malformed messages
                if (scope == null || participant == null) {
                    message.delete();
                    continue;
                }
                        
                // Pass on
                ConnectionEmailMessage _message = message;
                result.add(new BusEmail.BusEmailMessage(participant, scope, (Message) attachment) {
                    @Override
                    protected void delete() throws BusException {
                        _message.delete();
                    }
                    @Override
                    protected void expunge() throws BusException {
                        _message.expunge();
                    }
                });
            }

        } catch (InterruptedException e) {
            throw e;
        } catch (Exception e) {
            throw new BusException("Error receiving message", e);
        }

        // Done
        return result;
    }
    
    /**
     * Send message to participant
     * @param message
     * @param scope
     * @param participant
     * @throws BusException 
     */
    protected void send(Message message, Scope scope, Participant participant) throws BusException {
        
        // Recipient
        String recipient = sharedMailbox ? getEmailAddress() : participant.getEmailAddress();
        
        // Subject
        String subject = String.format(EMAIL_SUBJECT_RECEIVER, scope.getName(), participant.getName());
        
        // Body
        String body = SCOPE_NAME_START_TAG + scope.getName() + SCOPE_NAME_END_TAG + "\n" + 
                      PARTICIPANT_NAME_START_TAG + participant.getName() + PARTICIPANT_NAME_END_TAG + "\n" + 
                      PARTICIPANT_EMAIL_START_TAG + participant.getEmailAddress() + PARTICIPANT_EMAIL_END_TAG;
        
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
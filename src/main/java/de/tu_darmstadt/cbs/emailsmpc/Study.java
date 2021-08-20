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
package de.tu_darmstadt.cbs.emailsmpc;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.stream.IntStream;

import org.bihealth.mi.easybus.BusException;
import org.bihealth.mi.easybus.implementations.email.BusEmail;
import org.bihealth.mi.easybus.implementations.email.ConnectionIMAP;
import org.bihealth.mi.easybus.implementations.email.ConnectionIMAPSettings;
import org.bihealth.mi.easysmpc.resources.Resources;

/**
 * Main class of the API
 * @author Tobias Kussel
 * @author Felix Wirth
 * @author Fabian Prasser
 */
public class Study implements Serializable, Cloneable {
    
    /**
     * Enum for the app state
     * @author Tobias Kussel
     */
    public enum StudyState {
         /** The none. */
         NONE, 
         /** The starting. */
         STARTING, 
         /** The participating. */
         PARTICIPATING, 
         /** The entering values. */
         ENTERING_VALUES, 
         /** The initial sending. */
         INITIAL_SENDING, 
         /** The sending share. */
         SENDING_SHARE, 
         /** The recieving share. */
         RECIEVING_SHARE, 
         /** The sending result. */
         SENDING_RESULT, 
         /** The recieving result. */
         RECIEVING_RESULT, 
         /** The finished. */
         FINISHED
    }
    
    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 67394185932574354L;
    
    /**
     * Load model.
     *
     * @param filename the filename
     * @return the app model
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws ClassNotFoundException the class not found exception
     * @throws IllegalArgumentException the illegal argument exception
     */
    public static Study loadModel(File filename) throws IOException, ClassNotFoundException, IllegalArgumentException {
        ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filename));
        Object o = ois.readObject();
        ois.close();
        if (!(o instanceof Study))
            throw new IllegalArgumentException("Invalid project file");
        Study model = (Study) o;
        model.setFilename(filename);
        return model;
    }

    /** The study UID. */
    private String                 studyUID;

    /** The number of participants. */
    private int                    numParticipants;

    /** The own id. */
    private int                    ownId;

    /** The state. */
    private StudyState             state;

    /** The bins. */
    private Bin[]                  bins;

    /** The participants. */
    private Participant[]          participants;

    /** The name. */
    private String                 name;

    /** The unsent messages. */
    private Message[]             unsentMessages;

    /** The filename. */
    private transient File         filename;

    /** The e-mail connection details */
    private ConnectionIMAPSettings connectionIMAPSettings;

    /** Bus for automatic e-mail processing */
    private transient BusEmail    bus;
    
    /** Store whether messages have been retrieved */
    private boolean[] retrievedMessages;

    /**
     * Instantiates a new app model.
     */
    public Study() {
        setStudyUID(UIDGenerator.generateShortUID(8));
        setName(null);
        setNumParticipants(0);
        setOwnId(0);
        setState(StudyState.NONE);
        setBins(null);
        setParticipants(null);
        unsentMessages = null;
        setFilename(null);
    }

    /**
     * Clear bins.
     */
    public synchronized void clearBins() {
        for (Bin b : this.getBins()) {
            b.clearShares();
        }
    }

    /**
     * Clone.
     *
     * @return the object
     */
    @Override
    public synchronized Object clone() {
        Study newModel = null;
        try {
            newModel = (Study) super.clone();
        } catch (CloneNotSupportedException e) {
            newModel = new Study();
        }
        newModel.setName(this.getName());
        newModel.setNumParticipants(this.getNumParticipants());
        newModel.setOwnId(this.getOwnId());
        newModel.setStudyUID(this.getStudyUID());
        newModel.setState(this.getState());
        newModel.setFilename(this.getFilename());     
        if (this.getBins() != null) {
            newModel.setBins(new Bin[this.getBins().length]);
            for (int i = 0; i < newModel.getBins().length; i++) {
                newModel.getBins()[i] = (Bin) this.getBins()[i].clone();
            }
        }
      
        if (this.getParticipants() != null) {
            newModel.setParticipants(new Participant[this.getParticipants().length]);
            for (int i = 0; i < newModel.getParticipants().length; i++) {
                newModel.getParticipants()[i] = (Participant) this.getParticipants()[i].clone();
            }
        }
      
        if (this.unsentMessages != null) {
            newModel.unsentMessages = new Message[this.unsentMessages.length];
            for (int i = 0; i < newModel.unsentMessages.length; i++) {
                if (this.unsentMessages[i] != null) newModel.unsentMessages[i] = (Message) this.unsentMessages[i].clone();
            }
        }
     
      return newModel;
    }

    /**
     * Equals.
     *
     * @param o the o
     * @return true, if successful
     */
    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (!(o instanceof Study))
            return false;
        Study m = (Study) o;
        boolean result = (m.getNumParticipants() == getNumParticipants());
        result = result && (m.getStudyUID().equals(getStudyUID()));
        result = result && (m.getOwnId() == getOwnId());
        result = result && (m.getState().equals(getState()));
        result = result && (m.getName().equals(getName()));
        if (m.getFilename() != null)
          result = result && (m.getFilename().equals(getFilename()));
        else
          result = result && (getFilename() == null);
        result = result && (m.getBins().length == getBins().length);
        result = result && (m.getParticipants().length == getParticipants().length);
        result = result && (m.unsentMessages.length == unsentMessages.length);
        for (int i = 0; i < getBins().length; i++) {
            if (m.getBins()[i] != null)
                result = result && m.getBins()[i].equals(getBins()[i]);
            else
                result = result && (getBins()[i] == null);
        }
        for (int i = 0; i < getParticipants().length; i++) {
            if (m.getParticipants()[i] != null)
                result = result && m.getParticipants()[i].equals(getParticipants()[i]);
            else
                result = result && (getParticipants()[i] == null);
        }
        for (int i = 0; i < unsentMessages.length; i++) {
            if (m.unsentMessages[i] != null)
                result = result && m.unsentMessages[i].equals(unsentMessages[i]);
            else
                result = result && (unsentMessages[i] == null);
        }
        return result;
    }

    /**
     * Gets the all results.
     *
     * @return the all results
     * @throws IllegalStateException the illegal state exception
     */
    public synchronized BinResult[] getAllResults() throws IllegalStateException {
        if (getState() != StudyState.FINISHED)
            throw new IllegalStateException("Forbidden action (getBinResult) at current state " + getState());
        BinResult[] result = new BinResult[getBins().length];
        for (int i = 0; i < getBins().length; i++) {
            result[i] = getBinResult(i);
        }
        return result;
    }

    /**
     * Gets the bin result.
     *
     * @param binId the bin id
     * @return the bin result
     * @throws IllegalStateException the illegal state exception
     */
    public synchronized BinResult getBinResult(int binId) throws IllegalStateException {
        if (getState() != StudyState.FINISHED)
            throw new IllegalStateException("Forbidden action (getBinResult) at current state " + getState());
        return new BinResult(getBins()[binId].name, getBins()[binId].reconstructBin());
    }
    
    /**
     * Returns the bus with standard interval length and shared-mailbox mode
     * 
     * @return the bus
     * @throws BusException 
     */
    public synchronized BusEmail getBus() throws BusException {
        return getBus(Resources.INTERVAL_CHECK_MAILBOX_MILLISECONDS);
    }
    
    /**
     * Gets the bus with a shared mailbox
     * 
     * @param millis milliseconds interval to check for new mails
     * @return
     * @throws BusException
     */
    public synchronized BusEmail getBus(int millis) throws BusException {
        return getBus(millis, true);
    }
    
    /**
     * Returns the bus
     * 
     * @param millis milliseconds interval to check for new mails. If zero a send only bus is returned
     * @return the bus
     * @throws BusException 
     */
    public synchronized BusEmail getBus(int millis, boolean isSharedMailbox) throws BusException {

        if ((this.bus == null || !this.bus.isAlive()) && this.getConnectionIMAPSettings() != null) {
            this.bus = new BusEmail(new ConnectionIMAP(this.getConnectionIMAPSettings(), isSharedMailbox), millis);
        }
        return this.bus;
    }
    
    /**
     * Is the e-mail bus thread alive?
     * 
     * @return
     */
    public synchronized boolean isBusAlive() {
        if (this.bus != null) {
            return this.bus.isAlive();
        }
        
        return false;
    }
    
    /**
     * Is the e-mail bus connected to receive e-mails?
     * 
     * @return
     */
    public synchronized boolean isBusConectedReceiving() {
        if (this.bus != null) {
            return this.bus.isReceivingConnected();
        }
        
        return false;
    }

    /**
     * Gets the participant from id.
     *
     * @param p the p
     * @return the participant from id
     * @throws IllegalArgumentException the illegal argument exception
     */
    public synchronized Participant getParticipantFromId(int p) throws IllegalArgumentException {
        if (p < 0 || p > (getParticipants().length - 1))
            throw new IllegalArgumentException("Unknown participant " + p);
        return getParticipants()[p];
    }

    /**
     * Gets the participant id.
     *
     * @param p the p
     * @return the participant id
     * @throws IllegalArgumentException the illegal argument exception
     */
    public synchronized int getParticipantId(Participant p) throws IllegalArgumentException {
        for (int i = 0; i < getParticipants().length; i++) {
            if (getParticipants()[i].equals(p))
                return i;
        }
        throw new IllegalArgumentException("Unknown participant " + p);
    }

    /**
     * Gets the unsent message for.
     *
     * @param recipientId the recipient id
     * @return the unsent message for
     */
    public synchronized Message getUnsentMessageFor(int recipientId) {
        return unsentMessages[recipientId];
    }

    /**
     * Hash code.
     *
     * @return the int
     */
    @Override
    public int hashCode() {
        int result = getNumParticipants();
        result = 31 * result + getOwnId();
        result = 31 * result + getStudyUID().hashCode();
        result = 31 * result + getState().hashCode();
        result = 31 * result + getName().hashCode();
        if (getFilename() != null)
            result = 31 * result + getFilename().hashCode();
        for (Bin b : getBins()) {
            if (b != null)
                result = 31 * result + b.hashCode();
            else
                result = 31 * result;
        }
        for (Participant p : getParticipants()) {
            if (p != null)
                result = 31 * result + p.hashCode();
            else
                result = 31 * result;
        }
        for (Message m : unsentMessages) {
            if (m != null)
                result = 31 * result + m.hashCode();
            else
                result = 31 * result;
        }
        return result;
    }

    /**
     * Initialize study.
     *
     * @param name the name
     * @param participants the participants
     * @param bins the bins
     * @param connectionIMAPSettings 
     * @throws IllegalStateException the illegal state exception
     */
    public synchronized void initializeStudy(String name, Participant[] participants, Bin[] bins, ConnectionIMAPSettings connectionIMAPSettings) throws IllegalStateException {
        if (!(getState() == StudyState.NONE || getState() == StudyState.STARTING))
            throw new IllegalStateException("Unable to initialize study at state" + getState());
        this.setName(name);
        this.setConnectionIMAPSettings(connectionIMAPSettings);
        setNumParticipants(participants.length);
        unsentMessages = new Message[getNumParticipants()];
        retrievedMessages = new boolean[getNumParticipants()];       
        for (Bin bin : bins) {
            if (!(bin.isInitialized())) {
                bin.initialize(getNumParticipants());
            }
        }
        this.setBins(bins);
        this.setOwnId(0); // unneeded but for verbosity...
        retrievedMessages[getOwnId()] = true;
        this.setParticipants(participants);
        if (getState() == StudyState.NONE)
            setState(StudyState.STARTING);
    }

    /**
     * Check whether the message is for the correct recipient
     * @param msg
     * @return
     */
    public synchronized boolean isCorrectRecipient(Message msg) {
        return (msg.recipientName.equals(getParticipantFromId(getOwnId()).name) && msg.recipientEmailAddress.equals(getParticipantFromId(getOwnId()).emailAddress));
    }

    /**
     * Validates a given message to set a share or result.
     *
     * @param msg the msg
     * @return true, if is message share result valid
     */
    public synchronized boolean isMessageShareResultValid(Message msg) {
        try {
            if(!isCorrectRecipient(msg)){
                return false;
            }            
            Participant sender = getParticipantFromId(msg.senderID);
            Message.validateData(getParticipantId(sender), getParticipants()[getOwnId()], msg.data);
            switch (getState()){
            case RECIEVING_SHARE:
                MessageShare.decodeAndVerify(Message.getMessageData(msg), sender, this);
                break;
            case RECIEVING_RESULT:
                MessageResult.decodeAndVerify(Message.getMessageData(msg), sender, this);
                break;
            default:
                return false;
            }            
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Checks if is result computable.
     *
     * @return true, if is result computable
     */
    public synchronized boolean isResultComputable() {
        boolean ready = true;
        for (Bin b : getBins()) {
            ready &= b.isComplete();
        }
        return ready;
    }

    /**
     * Mark message sent.
     *
     * @param recipientId the recipient id
     * @throws IllegalArgumentException the illegal argument exception
     */
    public synchronized void markMessageSent(int recipientId) throws IllegalArgumentException {
        if (unsentMessages[recipientId] == null)
            throw new IllegalArgumentException("Message " + recipientId + " nonexistent");
        unsentMessages[recipientId] = null;
    }
    
    /**
     * Mark a message as retrieved (one time or more).
     *
     * @param recipientId the recipient id
     */
    public synchronized void markMessageRetrieved(int recipientId) {
        retrievedMessages[recipientId] = true;
    }
    
    /**
     * Was a message already retrieved (one time or more).
     *
     * @param recipientId the recipient id
     * @return message retrieved
     */
    public synchronized boolean wasMessageRetrieved(int recipientId) {
        return retrievedMessages[recipientId];
    }
    
    /**
     * Have all messages been retrieved
     *
     * @return messages retrieved
     */
    public synchronized boolean areAllMessagesRetrieved() {
        for(int index = 0; index < retrievedMessages.length; index++) {
            if (!retrievedMessages[index]) return false;
        }
        
        return true;
    }

    /**
     * Messages unsent.
     *
     * @return true, if successful
     */
    public synchronized boolean messagesUnsent() {
        for (Message m : unsentMessages) {
            if (m != null)
                return true;
        }
        return false;
    }

    /**
     * Populate initial messages.
     *
     * @throws IllegalStateException the illegal state exception
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public synchronized void populateInitialMessages() throws IllegalStateException, IOException {
      if (getState() != StudyState.INITIAL_SENDING)
        throw new IllegalStateException("Forbidden action (getInitialMessage) at current state " + getState());
        for (int i = 0; i < getNumParticipants(); i++) {
          if (i != getOwnId())
            unsentMessages[i] = getInitialMessage(i);
          else {
            for (Bin b : getBins()) {
              b.transferSharesOutIn(getOwnId());
            }
          }
        }
        for (Bin b : getBins()) {
          b.clearOutSharesExceptId(getOwnId());
        }
    }
    
    /**
     * Populate result messages.
     *
     * @throws IllegalStateException the illegal state exception
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public synchronized void populateResultMessages() throws IllegalStateException, IOException {
        if (getState() != StudyState.SENDING_RESULT)
            throw new IllegalStateException("Forbidden action (populateResultMessage) at current state " + getState());
      MessageResult data = new MessageResult(this);
      for (int i = 0; i < getNumParticipants(); i++) {
        if (i != getOwnId()) {
          Participant recipient = this.getParticipants()[i];
          unsentMessages[i] = new Message(getOwnId(), recipient, data.getMessage());
          // Reset the retrieved messages array
          retrievedMessages[i] = false;
          retrievedMessages[getOwnId()] = true;
        } else {
          for (Bin b : getBins()) {
            b.setInShare(b.getSumShare(), getOwnId());
          }
        }
      }
      for (Bin b : getBins()) {
        b.clearInSharesExceptId(getOwnId());
      }
    }

    /**
     * Populate share messages.
     *
     * @throws IllegalStateException the illegal state exception
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public synchronized void populateShareMessages() throws IllegalStateException, IOException {
        if (getState() != StudyState.SENDING_SHARE)
            throw new IllegalStateException("Forbidden action (populateShareMessage) at current state " + getState());
          for (int i = 0; i < getNumParticipants(); i++) {
            if (i != getOwnId()) {
              unsentMessages[i] = getShareMessage(i);
            } else {
              for (Bin b : getBins()) {
                b.transferSharesOutIn(getOwnId());
              }
            }
          }
          for (Bin b : getBins()) {
            b.clearOutSharesExceptId(getOwnId());
          }
    }

    /**
     * Save program.
     *
     * @throws IllegalStateException the illegal state exception
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public synchronized void saveProgram() throws IllegalStateException, IOException {
        if (getFilename() == null) {
            throw new IllegalStateException("No filename defined");
        } else {
            ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(getFilename()));
            oos.writeObject(this);
            oos.close();
        }
    }

    /**
     * Sets the model from message.
     *
     * @param initialMsg the new model from message
     * @throws IllegalStateException the illegal state exception
     * @throws IllegalArgumentException the illegal argument exception
     * @throws ClassNotFoundException the class not found exception
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public synchronized void setModelFromMessage(String initialMsg)
            throws IllegalStateException, IllegalArgumentException, ClassNotFoundException, IOException {
        if (getState() != StudyState.PARTICIPATING)
            throw new IllegalStateException("Setting the Model from a Message is not allowed at state " + getState());
        Study model = MessageInitial.getAppModel(MessageInitial.decodeMessage(Message.getMessageData(initialMsg)));
        model.setState(StudyState.PARTICIPATING);
        update(model);
    }

    /**
     * Sets the share from message.
     *
     * @param msg the new share from message
     * @throws IllegalStateException the illegal state exception
     * @throws IllegalArgumentException the illegal argument exception
     * @throws NoSuchAlgorithmException the no such algorithm exception
     * @throws ClassNotFoundException the class not found exception
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public synchronized void setShareFromMessage(Message msg) throws IllegalStateException, IllegalArgumentException, NoSuchAlgorithmException, ClassNotFoundException, IOException {
        Participant sender = getParticipantFromId(msg.senderID);
        if (!(getState() == StudyState.RECIEVING_SHARE || getState() == StudyState.RECIEVING_RESULT)) {
            throw new IllegalStateException("Setting a share from a message is not allowed at state " + getState());
        }
        if (!isCorrectRecipient(msg)) {
            throw new IllegalArgumentException("Message recipient does not match the current participant");
        }
        if (Message.validateData(getParticipantId(sender), getParticipants()[getOwnId()], msg.data)) {
            if (getState() == StudyState.RECIEVING_SHARE) {
                MessageShare sm = MessageShare.decodeAndVerify(Message.getMessageData(msg), sender, this);
                int senderId = getParticipantId(sender);
                for (int i = 0; i < getBins().length; i++) {
                    getBins()[i].setInShare(sm.bins[i].share, senderId);
                }
            } else {
                MessageResult rm = MessageResult.decodeAndVerify(Message.getMessageData(msg), sender, this);
                int senderId = getParticipantId(sender);
                for (int i = 0; i < getBins().length; i++) {
                    getBins()[i].setInShare(rm.bins[i].share, senderId);
                }
            }
        } else {
            throw new IllegalArgumentException("Message invalid");
        }
    }

    /**
     * Stops the bus
     */
    public synchronized void stopBus() {
        if (this.bus != null) {
            this.bus.stop();
        } 
    }

    /**
     * To entering values.
     *
     * @param initialMessage the initial message
     * @throws IllegalStateException the illegal state exception
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws IllegalArgumentException the illegal argument exception
     * @throws ClassNotFoundException the class not found exception
     */
    public synchronized void toEnteringValues(String initialMessage) throws IllegalStateException, IOException, IllegalArgumentException, ClassNotFoundException{
        setModelFromMessage(initialMessage);
        unsentMessages = new Message[getNumParticipants()];
        retrievedMessages = new boolean[getNumParticipants()];
        retrievedMessages[getOwnId()] = true;
        advanceState(StudyState.ENTERING_VALUES);
    }

    /**
     * To finished.
     *
     * @throws IllegalStateException the illegal state exception
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public synchronized void toFinished() throws IllegalStateException, IOException{
        advanceState(StudyState.FINISHED);
    }

    /**
     * To initial sending.
     *
     * @param name the name
     * @param participants the participants
     * @param bins the bins
     * @param connectionIMAPSettings 
     * @throws IllegalStateException the illegal state exception
     * @throws IOException Signals that an I/O exception has occurred.
     */
    // Note, that bins need to be initialized and have shared values
    public synchronized void toInitialSending(String name, Participant[] participants, Bin[] bins, ConnectionIMAPSettings connectionIMAPSettings) throws IllegalStateException, IOException {
        initializeStudy(name, participants, bins, connectionIMAPSettings);
        advanceState(StudyState.INITIAL_SENDING);
    }

    /**
     * To participating.
     *
     * @throws IllegalStateException the illegal state exception
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public synchronized void toParticipating() throws IllegalStateException, IOException{
        advanceState(StudyState.PARTICIPATING);
    }

    /**
     * To recieving result.
     *
     * @throws IllegalStateException the illegal state exception
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public synchronized void toRecievingResult() throws IllegalStateException, IOException{
        advanceState(StudyState.RECIEVING_RESULT);
    }

    /**
     * To recieving shares.
     *
     * @throws IllegalStateException the illegal state exception
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public synchronized void toRecievingShares() throws IllegalStateException, IOException{
        advanceState(StudyState.RECIEVING_SHARE);
    }

    /**
     * To sending result.
     *
     * @throws IllegalStateException the illegal state exception
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public synchronized void toSendingResult() throws IllegalStateException, IOException{
        advanceState(StudyState.SENDING_RESULT);
    }

    /**
     * To sending shares.
     *
     * @param values the values
     * @throws IllegalArgumentException the illegal argument exception
     * @throws IllegalStateException the illegal state exception
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public synchronized void toSendingShares(BigInteger[] values) throws IllegalArgumentException, IllegalStateException, IOException {
        if (values.length != getBins().length)
            throw new IllegalArgumentException("Number of values not equal number of bins");
        for (int i = 0; i < getBins().length; i++) {
            getBins()[i].shareValue(values[i]);
        }
        advanceState(StudyState.SENDING_SHARE);
    }

    /**
     * Legal State Transitions:
     * +-----------------+     +------------------+
     * |    Starting     | <-- |       None       |
     * +-----------------+     +------------------+
     *   |                       |
     *   |                       |
     *   v                       v
     * +-----------------+     +------------------+
     * | Initial_Sending |     |  Participating   |
     * +-----------------+     +------------------+
     *   |                       |
     *   |                       |
     *   |                       v
     *   |                     +------------------+
     *   |                     |  Entering_Values |
     *   |                     +------------------+
     *   |                       |
     *   |                       |
     *   |                       v
     *   |                     +------------------+
     *   |                     |  Sending_Share   |
     *   |                     +------------------+
     *   |                       |
     *   |                       |
     *   |                       v
     *   |                     +------------------+
     *   +-------------------> | Recieving_Share  |
     *                         +------------------+
     *                           |
     *                           |
     *                           v
     *                         +------------------+
     *                         |  Sending_Result  |
     *                         +------------------+
     *                           |
     *                           |
     *                           v
     *                         +------------------+
     *                         | Recieving_Result |
     *                         +------------------+
     *                           |
     *                           |
     *                           v
     *                         +------------------+
     *                         |     Finished     |
     *                         +------------------+.
     *
     * @throws IllegalStateException the illegal state exception
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public synchronized void toStarting() throws IllegalStateException, IOException{
        advanceState(StudyState.STARTING);
    }

    /**
     * To string.
     *
     * @return the string
     */
    @Override
    public synchronized String toString() {
        return "AppModel [StudyUID=" + getStudyUID() +", numParticipants=" + getNumParticipants() + ", ownId=" + getOwnId() + ", state=" + getState() + ", bins="
                + Arrays.toString(getBins()) + ", participants=" + Arrays.toString(getParticipants()) + ", name=" + getName()
                + ", unsentMessages=" + Arrays.toString(unsentMessages) + ", filename=" + getFilename() + "]";
    }

    /**
     * Update.
     *
     * @param model the model
     */
    public synchronized void update(Study model) {
        setStudyUID(model.getStudyUID());
        setNumParticipants(model.getNumParticipants());
        setOwnId(model.getOwnId());
        setBins(model.getBins());
        setParticipants(model.getParticipants());
        setName(model.getName());
        setState(model.getState());
    }

    /**
     * Advance state.
     *
     * @param newState the new state
     * @throws IllegalStateException the illegal state exception
     * @throws IOException Signals that an I/O exception has occurred.
     */
    private void advanceState(StudyState newState) throws IllegalStateException, IOException {
        switch (getState()) {
        case NONE:
            if (!(newState == StudyState.STARTING || newState == StudyState.PARTICIPATING))
                throw new IllegalStateException("Illegal state transition from " + getState() + " to " + newState);
            // Change GUI Window
            setState(newState);
            break;
        case STARTING:
            if (!(newState == StudyState.INITIAL_SENDING))
              throw new IllegalStateException("Illegal state transition from " + getState() + " to " + newState);
            setState(newState);
            populateInitialMessages();
            // Change GUI Window
            break;
        case PARTICIPATING:
            if (newState != StudyState.ENTERING_VALUES)
                throw new IllegalStateException("Illegal state transition from " + getState() + " to " + newState);
            setState(newState);
            // Change GUI Window
            break;
        case ENTERING_VALUES:
            if (newState != StudyState.SENDING_SHARE)
                throw new IllegalStateException("Illegal state transition from " + getState() + " to " + newState);
            setState(newState);
            populateShareMessages();
            // Change GUI Window
            break;
        case INITIAL_SENDING:
            if (newState != StudyState.RECIEVING_SHARE)
                throw new IllegalStateException("Illegal state transition from " + getState() + " to " + newState);
            if (messagesUnsent())
                throw new IllegalStateException("Still unsent messages left");
            // Only one InShare set (at ownId, no OutShare set
            for (Bin b : getBins()) {
                int[] filledInShareIndices = b.getFilledInShareIndices();
                int[] filledOutShareIndices = b.getFilledOutShareIndices();
                if (!(filledInShareIndices.length == 1 && filledInShareIndices[0] == getOwnId()))
                    throw new IllegalStateException("InShares in bin " + b.name + " messed up");
                if (filledOutShareIndices.length != 0)
                    throw new IllegalStateException("OutShares in bin " + b.name + " not empty");
            }
            setState(newState);
            // Change GUI Window
            break;
        case SENDING_SHARE:
            // Forbid two parties
            if (newState != StudyState.RECIEVING_SHARE)
                throw new IllegalStateException("Illegal state transition from " + getState() + " to " + newState);
            if (messagesUnsent())
                throw new IllegalStateException("Still unsent messages left");
            // Two inShares (one from initial msg, one from self), no OutShares
            for (Bin b : getBins()) {
                int[] filledInShareIndices = b.getFilledInShareIndices();
                int[] filledOutShareIndices = b.getFilledOutShareIndices();
                if (!(filledInShareIndices.length == 2
                        && IntStream.of(filledInShareIndices).anyMatch(x -> (x == getOwnId() || x == 0))))
                    throw new IllegalStateException("InShares in bin " + b.name + " messed up");
                if (filledOutShareIndices.length != 0)
                    throw new IllegalStateException("OutShares in bin " + b.name + " not empty");
            }
            setState(newState);
            // Change GUI Window
            break;
        case RECIEVING_SHARE:
            if (newState != StudyState.SENDING_RESULT)
                throw new IllegalStateException("Illegal state transition from " + getState() + " to " + newState);
            if (!isResultComputable())
                throw new IllegalStateException("Not all shares collected");
            setState(newState);
            populateResultMessages();
            // Change GUI Window
            break;
        case SENDING_RESULT:
            if (newState != StudyState.RECIEVING_RESULT)
                throw new IllegalStateException("Illegal state transition from " + getState() + " to " + newState);
            if (messagesUnsent())
                throw new IllegalStateException("Still unsent messages left");
            // Sanity Check: Only one inShare (ownId), no OutShares
            for (Bin b : getBins()) {
                int[] filledInShareIndices = b.getFilledInShareIndices();
                int[] filledOutShareIndices = b.getFilledOutShareIndices();
                if (!(filledInShareIndices.length == 1 && filledInShareIndices[0] == getOwnId()))
                    throw new IllegalStateException("InShares in bin " + b.name + " messed up");
                if (filledOutShareIndices.length != 0)
                    throw new IllegalStateException("OutShares in bin " + b.name + " not empty");
            }
            setState(newState);
            // Change GUI Window
            break;
        case RECIEVING_RESULT:
            if (newState != StudyState.FINISHED)
                throw new IllegalStateException("Illegal state transition from " + getState() + " to " + newState);
            if (!isResultComputable())
                throw new IllegalStateException("Not all shares collected");
            setState(newState);
            // Change GUI WIndow and display result
            break;
        case FINISHED:
            throw new IllegalStateException("Illegal state transition: Already finished");
        }

    }
    
    /**
     * Gets the initial message.
     *
     * @param recipientId the recipient id
     * @return the initial message
     * @throws IOException Signals that an I/O exception has occurred.
     */
    private Message getInitialMessage(int recipientId) throws IOException {
        MessageInitial data = new MessageInitial(this, recipientId);
        Participant recipient = this.getParticipants()[recipientId];
        return new Message(getOwnId(), recipient, data.getMessage());
    }
    
    /**
     * Gets the share message.
     *
     * @param recipientId the recipient id
     * @return the share message
     * @throws IOException Signals that an I/O exception has occurred.
     */
    private Message getShareMessage(int recipientId) throws IOException {
        MessageShare data = new MessageShare(this, recipientId);
        Participant recipient = this.getParticipants()[recipientId];
        return new Message(getOwnId(), recipient, data.getMessage());
    }

    /**
     * @return the studyUID
     */
    public synchronized String getStudyUID() {
        return studyUID;
    }

    /**
     * @param studyUID the studyUID to set
     */
    public synchronized void setStudyUID(String studyUID) {
        this.studyUID = studyUID;
    }

    /**
     * @return the numParticipants
     */
    public synchronized int getNumParticipants() {
        return numParticipants;
    }

    /**
     * @param numParticipants the numParticipants to set
     */
    public synchronized void setNumParticipants(int numParticipants) {
        this.numParticipants = numParticipants;
    }

    /**
     * @return the ownId
     */
    public synchronized int getOwnId() {
        return ownId;
    }

    /**
     * @param ownId the ownId to set
     */
    public synchronized void setOwnId(int ownId) {
        this.ownId = ownId;
    }

    /**
     * @return the state
     */
    public synchronized StudyState getState() {
        return state;
    }

    /**
     * @param state the state to set
     */
    public synchronized void setState(StudyState state) {
        this.state = state;
    }

    /**
     * @return the bins
     */
    public synchronized Bin[] getBins() {
        return bins;
    }

    /**
     * @param bins the bins to set
     */
    public synchronized void setBins(Bin[] bins) {
        this.bins = bins;
    }

    /**
     * @return the participants
     */
    public synchronized Participant[] getParticipants() {
        return participants;
    }

    /**
     * @param participants the participants to set
     */
    public synchronized void setParticipants(Participant[] participants) {
        this.participants = participants;
    }

    /**
     * @return the name
     */
    public synchronized String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public synchronized void setName(String name) {
        this.name = name;
    }

    /**
     * @return the filename
     */
    public synchronized File getFilename() {
        return filename;
    }

    /**
     * @param filename the filename to set
     */
    public synchronized void setFilename(File filename) {
        this.filename = filename;
    }

    /**
     * @return the connectionIMAPSettings
     */
    public synchronized ConnectionIMAPSettings getConnectionIMAPSettings() {
        return connectionIMAPSettings;
    }

    /**
     * @param connectionIMAPSettings the connectionIMAPSettings to set
     */
    public synchronized void setConnectionIMAPSettings(ConnectionIMAPSettings connectionIMAPSettings) {
        this.connectionIMAPSettings = connectionIMAPSettings;
    }
}

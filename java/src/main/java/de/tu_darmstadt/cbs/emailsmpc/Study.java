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
    public static Study loadModel(File filename)
            throws IOException, ClassNotFoundException, IllegalArgumentException {
        ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filename));
        Object o = ois.readObject();
        ois.close();
        if (!(o instanceof Study))
            throw new IllegalArgumentException("Invalid Save file");
        return (Study) o;
    }
    
    /** The study UID. */
    public String studyUID = UIDGenerator.generateShortUID(8);
    
    /** The number of participants. */
    public int numParticipants;
    
    /** The own id. */
    public int ownId;
    
    /** The state. */
    public StudyState state;
    
    /** The bins. */
    public Bin[] bins;
    
    /** The participants. */
    public Participant[] participants;
    
    /** The name. */
    public String name;
    
    /** The unsent messages. */
    private Message[] unsentMessages;

    /** The filename. */
    public File filename;

    /**
     * Instantiates a new app model.
     */
    public Study() {
        name = null;
        numParticipants = 0;
        ownId = 0;
        state = StudyState.NONE;
        bins = null;
        participants = null;
        unsentMessages = null;
        filename = null;
    }

    /**
     * Clear bins.
     */
    public void clearBins() {
        for (Bin b : this.bins) {
            b.clearShares();
        }
    }

    /**
     * Clone.
     *
     * @return the object
     */
    @Override
    public Object clone() {
        Study newModel = null;
        try {
            newModel = (Study) super.clone();
        } catch (CloneNotSupportedException e) {
            newModel = new Study();
        }
        newModel.name = this.name;
        newModel.numParticipants = this.numParticipants;
        newModel.ownId = this.ownId;
        newModel.studyUID = this.studyUID;
        newModel.state = this.state;
        newModel.filename = this.filename;     
        if (this.bins != null) {
            newModel.bins = new Bin[this.bins.length];
            for (int i = 0; i < newModel.bins.length; i++) {
                newModel.bins[i] = (Bin) this.bins[i].clone();
            }
        }
      
        if (this.participants != null) {
            newModel.participants = new Participant[this.participants.length];
            for (int i = 0; i < newModel.participants.length; i++) {
                newModel.participants[i] = (Participant) this.participants[i].clone();
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
        boolean result = (m.numParticipants == numParticipants);
        result = result && (m.studyUID.equals(studyUID));
        result = result && (m.ownId == ownId);
        result = result && (m.state.equals(state));
        result = result && (m.name.equals(name));
        if (m.filename != null)
          result = result && (m.filename.equals(filename));
        else
          result = result && (filename == null);
        result = result && (m.bins.length == bins.length);
        result = result && (m.participants.length == participants.length);
        result = result && (m.unsentMessages.length == unsentMessages.length);
        for (int i = 0; i < bins.length; i++) {
            if (m.bins[i] != null)
                result = result && m.bins[i].equals(bins[i]);
            else
                result = result && (bins[i] == null);
        }
        for (int i = 0; i < participants.length; i++) {
            if (m.participants[i] != null)
                result = result && m.participants[i].equals(participants[i]);
            else
                result = result && (participants[i] == null);
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
    public BinResult[] getAllResults() throws IllegalStateException {
        if (state != StudyState.FINISHED)
            throw new IllegalStateException("Forbidden action (getBinResult) at current state " + state);
        BinResult[] result = new BinResult[bins.length];
        for (int i = 0; i < bins.length; i++) {
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
    public BinResult getBinResult(int binId) throws IllegalStateException {
        if (state != StudyState.FINISHED)
            throw new IllegalStateException("Forbidden action (getBinResult) at current state " + state);
        return new BinResult(bins[binId].name, bins[binId].reconstructBin());
    }

    /**
     * Gets the participant from id.
     *
     * @param p the p
     * @return the participant from id
     * @throws IllegalArgumentException the illegal argument exception
     */
    public Participant getParticipantFromId(int p) throws IllegalArgumentException {
        if (p < 0 || p > (participants.length - 1))
            throw new IllegalArgumentException("Unknown participant " + p);
        return participants[p];
    }

    /**
     * Gets the participant id.
     *
     * @param p the p
     * @return the participant id
     * @throws IllegalArgumentException the illegal argument exception
     */
    public int getParticipantId(Participant p) throws IllegalArgumentException {
        for (int i = 0; i < participants.length; i++) {
            if (participants[i].equals(p))
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
    public Message getUnsentMessageFor(int recipientId) {
        return unsentMessages[recipientId];
    }

    /**
     * Hash code.
     *
     * @return the int
     */
    @Override
    public int hashCode() {
        int result = numParticipants;
        result = 31 * result + ownId;
        result = 31 * result + studyUID.hashCode();
        result = 31 * result + state.hashCode();
        result = 31 * result + name.hashCode();
        if (filename != null)
            result = 31 * result + filename.hashCode();
        for (Bin b : bins) {
            if (b != null)
                result = 31 * result + b.hashCode();
            else
                result = 31 * result;
        }
        for (Participant p : participants) {
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
     * @throws IllegalStateException the illegal state exception
     */
    public void initializeStudy(String name, Participant[] participants, Bin[] bins) throws IllegalStateException {
        if (!(state == StudyState.NONE || state == StudyState.STARTING))
            throw new IllegalStateException("Unable to initialize study at state" + state);
        this.name = name;
        numParticipants = participants.length;
        unsentMessages = new Message[numParticipants];
        for (Bin bin : bins) {
            if (!(bin.isInitialized())) {
                // throw new IllegalStateException("Unable to initialize bin " + bin.name);
                bin.initialize(numParticipants);
            }
        }
        this.bins = bins;
        this.ownId = 0; // unneeded but for verbosity...
        this.participants = participants;
        if (state == StudyState.NONE)
            state = StudyState.STARTING;
    }

    /**
     * Check whether the message is for the correct recipient
     * @param msg
     * @return
     */
    public boolean isCorrectRecipient(Message msg) {
        return (msg.recipientName.equals(getParticipantFromId(ownId).name) && msg.recipientEmailAddress.equals(getParticipantFromId(ownId).emailAddress));
    }

    /**
     * Validates a given message to set a share or result.
     *
     * @param msg the msg
     * @return true, if is message share result valid
     */
    public boolean isMessageShareResultValid(Message msg) {
        try {
            if(isCorrectRecipient(msg)){
                return false;
            }            
            Participant sender = getParticipantFromId(msg.senderID);
            Message.validateData(getParticipantId(sender), participants[ownId], msg.data);
            switch (state){
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
    public boolean isResultComputable() {
        boolean ready = true;
        for (Bin b : bins) {
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
    public void markMessageSent(int recipientId) throws IllegalArgumentException {
        if (unsentMessages[recipientId] == null)
            throw new IllegalArgumentException("Message " + recipientId + " nonexistent");
        unsentMessages[recipientId] = null;
    }

    /**
     * Messages unsent.
     *
     * @return true, if successful
     */
    public boolean messagesUnsent() {
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
    public void populateInitialMessages() throws IllegalStateException, IOException {
      if (state != StudyState.INITIAL_SENDING)
        throw new IllegalStateException("Forbidden action (getInitialMessage) at current state " + state);
        for (int i = 0; i < numParticipants; i++) {
          if (i != ownId)
            unsentMessages[i] = getInitialMessage(i);
          else {
            for (Bin b : bins) {
              b.transferSharesOutIn(ownId);
            }
          }
        }
        for (Bin b : bins) {
          b.clearOutSharesExceptId(ownId);
        }
    }

    /**
     * Populate result messages.
     *
     * @throws IllegalStateException the illegal state exception
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public void populateResultMessages() throws IllegalStateException, IOException {
        if (state != StudyState.SENDING_RESULT)
            throw new IllegalStateException("Forbidden action (populateResultMessage) at current state " + state);
      MessageResult data = new MessageResult(this);
      for (int i = 0; i < numParticipants; i++) {
        if (i != ownId) {
          Participant recipient = this.participants[i];
          unsentMessages[i] = new Message(ownId, recipient, data.getMessage());
        } else {
          for (Bin b : bins) {
            b.setInShare(b.getSumShare(), ownId);
          }
        }
      }
      for (Bin b : bins) {
        b.clearInSharesExceptId(ownId);
      }
    }
    
    /**
     * Populate share messages.
     *
     * @throws IllegalStateException the illegal state exception
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public void populateShareMessages() throws IllegalStateException, IOException {
        if (state != StudyState.SENDING_SHARE)
            throw new IllegalStateException("Forbidden action (populateShareMessage) at current state " + state);
          for (int i = 0; i < numParticipants; i++) {
            if (i != ownId) {
              unsentMessages[i] = getShareMessage(i);
            } else {
              for (Bin b : bins) {
                b.transferSharesOutIn(ownId);
              }
            }
          }
          for (Bin b : bins) {
            b.clearOutSharesExceptId(ownId);
          }
    }

    /**
     * Save program.
     *
     * @throws IllegalStateException the illegal state exception
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public void saveProgram() throws IllegalStateException, IOException {
        if (filename == null) {
            throw new IllegalStateException("No filename defined");
        } else {
            ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filename));
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
    public void setModelFromMessage(String initialMsg)
            throws IllegalStateException, IllegalArgumentException, ClassNotFoundException, IOException {
        if (state != StudyState.PARTICIPATING)
            throw new IllegalStateException("Setting the Model from a Message is not allowed at state " + state);
        Study model = MessageInitial.getAppModel(MessageInitial.decodeMessage(Message.getMessageData(initialMsg)));
        model.state = StudyState.PARTICIPATING;
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
    public void setShareFromMessage(Message msg) throws IllegalStateException, IllegalArgumentException, NoSuchAlgorithmException, ClassNotFoundException, IOException {
        Participant sender = getParticipantFromId(msg.senderID);
        if (!(state == StudyState.RECIEVING_SHARE || state == StudyState.RECIEVING_RESULT)) {
            throw new IllegalStateException("Setting a share from a message is not allowed at state " + state);
        }
        if (!isCorrectRecipient(msg)) {
            throw new IllegalArgumentException("Message recipient does not match the current participant");
        }
        if (Message.validateData(getParticipantId(sender), participants[ownId], msg.data)) {
            if (state == StudyState.RECIEVING_SHARE) {
                MessageShare sm = MessageShare.decodeAndVerify(Message.getMessageData(msg), sender, this);
                int senderId = getParticipantId(sender);
                for (int i = 0; i < bins.length; i++) {
                    bins[i].setInShare(sm.bins[i].share, senderId);
                }
            } else {
                MessageResult rm = MessageResult.decodeAndVerify(Message.getMessageData(msg), sender, this);
                int senderId = getParticipantId(sender);
                for (int i = 0; i < bins.length; i++) {
                    bins[i].setInShare(rm.bins[i].share, senderId);
                }
            }
        } else {
            throw new IllegalArgumentException("Message invalid");
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
    public void toEnteringValues(String initialMessage) throws IllegalStateException, IOException, IllegalArgumentException, ClassNotFoundException{
        setModelFromMessage(initialMessage);
        unsentMessages = new Message[numParticipants];
      advanceState(StudyState.ENTERING_VALUES);
    }

    /**
     * To finished.
     *
     * @throws IllegalStateException the illegal state exception
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public void toFinished() throws IllegalStateException, IOException{
        advanceState(StudyState.FINISHED);
    }

    /**
     * To initial sending.
     *
     * @param name the name
     * @param participants the participants
     * @param bins the bins
     * @throws IllegalStateException the illegal state exception
     * @throws IOException Signals that an I/O exception has occurred.
     */
    // Note, that bins need to be initialized and have shared values
    public void toInitialSending(String name, Participant[] participants, Bin[] bins) throws IllegalStateException, IOException {
        initializeStudy(name, participants, bins);
        advanceState(StudyState.INITIAL_SENDING);
    }

    /**
     * To participating.
     *
     * @throws IllegalStateException the illegal state exception
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public void toParticipating() throws IllegalStateException, IOException{
        advanceState(StudyState.PARTICIPATING);
    }

    /**
     * To recieving result.
     *
     * @throws IllegalStateException the illegal state exception
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public void toRecievingResult() throws IllegalStateException, IOException{
        advanceState(StudyState.RECIEVING_RESULT);
    }

    /**
     * To recieving shares.
     *
     * @throws IllegalStateException the illegal state exception
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public void toRecievingShares() throws IllegalStateException, IOException{
        advanceState(StudyState.RECIEVING_SHARE);
    }

    /**
     * To sending result.
     *
     * @throws IllegalStateException the illegal state exception
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public void toSendingResult() throws IllegalStateException, IOException{
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
    public void toSendingShares(BigInteger[] values) throws IllegalArgumentException, IllegalStateException, IOException {
        if (values.length != bins.length)
            throw new IllegalArgumentException("Number of values not equal number of bins");
        for (int i = 0; i < bins.length; i++) {
            bins[i].shareValue(values[i]);
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
    public void toStarting() throws IllegalStateException, IOException{
        advanceState(StudyState.STARTING);
    }

    /**
     * To string.
     *
     * @return the string
     */
    @Override
    public String toString() {
        return "AppModel [StudyUID=" + studyUID +", numParticipants=" + numParticipants + ", ownId=" + ownId + ", state=" + state + ", bins="
                + Arrays.toString(bins) + ", participants=" + Arrays.toString(participants) + ", name=" + name
                + ", unsentMessages=" + Arrays.toString(unsentMessages) + ", filename=" + filename + "]";
    }

    /**
     * Update.
     *
     * @param model the model
     */
    public void update(Study model) {
        studyUID = model.studyUID;
        numParticipants = model.numParticipants;
        ownId = model.ownId;
        bins = model.bins;
        participants = model.participants;
        name = model.name;
        state = model.state;
    }

    /**
     * Advance state.
     *
     * @param newState the new state
     * @throws IllegalStateException the illegal state exception
     * @throws IOException Signals that an I/O exception has occurred.
     */
    private void advanceState(StudyState newState) throws IllegalStateException, IOException {
        switch (state) {
        case NONE:
            if (!(newState == StudyState.STARTING || newState == StudyState.PARTICIPATING))
                throw new IllegalStateException("Illegal state transition from " + state + " to " + newState);
            // Change GUI Window
            state = newState;
            break;
        case STARTING:
            if (!(newState == StudyState.INITIAL_SENDING))
              throw new IllegalStateException("Illegal state transition from " + state + " to " + newState);
            state = newState;
            populateInitialMessages();
            // Change GUI Window
            break;
        case PARTICIPATING:
            if (newState != StudyState.ENTERING_VALUES)
                throw new IllegalStateException("Illegal state transition from " + state + " to " + newState);
            state = newState;
            // Change GUI Window
            break;
        case ENTERING_VALUES:
            if (newState != StudyState.SENDING_SHARE)
                throw new IllegalStateException("Illegal state transition from " + state + " to " + newState);
            state = newState;
            populateShareMessages();
            // Change GUI Window
            break;
        case INITIAL_SENDING:
            if (newState != StudyState.RECIEVING_SHARE)
                throw new IllegalStateException("Illegal state transition from " + state + " to " + newState);
            if (messagesUnsent())
                throw new IllegalStateException("Still unsent messages left");
            // Only one InShare set (at ownId, no OutShare set
            for (Bin b : bins) {
                int[] filledInShareIndices = b.getFilledInShareIndices();
                int[] filledOutShareIndices = b.getFilledOutShareIndices();
                if (!(filledInShareIndices.length == 1 && filledInShareIndices[0] == ownId))
                    throw new IllegalStateException("InShares in bin " + b.name + " messed up");
                if (filledOutShareIndices.length != 0)
                    throw new IllegalStateException("OutShares in bin " + b.name + " not empty");
            }
            state = newState;
            // Change GUI Window
            break;
        case SENDING_SHARE:
            // Forbid two parties
            if (newState != StudyState.RECIEVING_SHARE)
                throw new IllegalStateException("Illegal state transition from " + state + " to " + newState);
            if (messagesUnsent())
                throw new IllegalStateException("Still unsent messages left");
            // Two inShares (one from initial msg, one from self), no OutShares
            for (Bin b : bins) {
                int[] filledInShareIndices = b.getFilledInShareIndices();
                int[] filledOutShareIndices = b.getFilledOutShareIndices();
                if (!(filledInShareIndices.length == 2
                        && IntStream.of(filledInShareIndices).anyMatch(x -> (x == ownId || x == 0))))
                    throw new IllegalStateException("InShares in bin " + b.name + " messed up");
                if (filledOutShareIndices.length != 0)
                    throw new IllegalStateException("OutShares in bin " + b.name + " not empty");
            }
            state = newState;
            // Change GUI Window
            break;
        case RECIEVING_SHARE:
            if (newState != StudyState.SENDING_RESULT)
                throw new IllegalStateException("Illegal state transition from " + state + " to " + newState);
            if (!isResultComputable())
                throw new IllegalStateException("Not all shares collected");
            state = newState;
            populateResultMessages();
            // Change GUI Window
            break;
        case SENDING_RESULT:
            if (newState != StudyState.RECIEVING_RESULT)
                throw new IllegalStateException("Illegal state transition from " + state + " to " + newState);
            if (messagesUnsent())
                throw new IllegalStateException("Still unsent messages left");
            // Sanity Check: Only one inShare (ownId), no OutShares
            for (Bin b : bins) {
                int[] filledInShareIndices = b.getFilledInShareIndices();
                int[] filledOutShareIndices = b.getFilledOutShareIndices();
                if (!(filledInShareIndices.length == 1 && filledInShareIndices[0] == ownId))
                    throw new IllegalStateException("InShares in bin " + b.name + " messed up");
                if (filledOutShareIndices.length != 0)
                    throw new IllegalStateException("OutShares in bin " + b.name + " not empty");
            }
            state = newState;
            // Change GUI Window
            break;
        case RECIEVING_RESULT:
            if (newState != StudyState.FINISHED)
                throw new IllegalStateException("Illegal state transition from " + state + " to " + newState);
            if (!isResultComputable())
                throw new IllegalStateException("Not all shares collected");
            state = newState;
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
        Participant recipient = this.participants[recipientId];
        return new Message(ownId, recipient, data.getMessage());
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
        Participant recipient = this.participants[recipientId];
        return new Message(ownId, recipient, data.getMessage());
    }
}

package de.tu_darmstadt.cbs.emailsmpc;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.stream.IntStream;

public class AppModel implements Serializable, Cloneable {
    public String studyUID = UIDGenerator.generateShortUID(8);
    public int numParticipants;
    public int ownId;
    public AppState state;
    public Bin[] bins;
    public Participant[] participants;
    public String name;
    private Message[] unsentMessages;
    public File filename;
    private static final long serialVersionUID = 67394185932574354L;

    public AppModel() {
        name = null;
        numParticipants = 0;
        ownId = 0;
        state = AppState.NONE;
        bins = null;
        participants = null;
        unsentMessages = null;
        filename = null;
    }

    public void initializeStudy(String name, Participant[] participants, Bin[] bins) throws IllegalStateException {
        if (!(state == AppState.NONE || state == AppState.STARTING))
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
        if (state == AppState.NONE)
            state = AppState.STARTING;
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
     *                         +------------------+
     */

    public void toStarting() {
        advanceState(AppState.STARTING);
    }

    public void toParticipating() {
        advanceState(AppState.PARTICIPATING);
    }

    // Note, that bins need to be initialized and have shared values
    public void toInitialSending(String name, Participant[] participants, Bin[] bins) {
        initializeStudy(name, participants, bins);
        advanceState(AppState.INITIAL_SENDING);
    }

    public void toEnteringValues(String initialMessage) {
        try {
            setModelFromMessage(initialMessage);
            unsentMessages = new Message[numParticipants];
        } catch (ClassNotFoundException e) {
            System.out.println("Failed to set model from message: " + e);
        } catch (IOException e) {
            System.out.println("Failed to set model from message: " + e);
        }
        advanceState(AppState.ENTERING_VALUES);
    }

    public void toSendingShares(BigInteger[] values) throws IllegalArgumentException {
        if (values.length != bins.length)
            throw new IllegalArgumentException("Number of values not equal number of bins");
        for (int i = 0; i < bins.length; i++) {
            bins[i].shareValue(values[i]);
        }
        advanceState(AppState.SENDING_SHARE);
    }

    public void toRecievingShares() {
        advanceState(AppState.RECIEVING_SHARE);
    }

    public void toSendingResult() {
        advanceState(AppState.SENDING_RESULT);
    }

    public void toRecievingResult() {
        advanceState(AppState.RECIEVING_RESULT);
    }

    public void toFinished() {
        advanceState(AppState.FINISHED);
    }

    private void advanceState(AppState newState) throws IllegalStateException {
        switch (state) {
        case NONE:
            if (!(newState == AppState.STARTING || newState == AppState.PARTICIPATING))
                throw new IllegalStateException("Illegal state transition from " + state + " to " + newState);
            // Change GUI Window
            state = newState;
            break;
        case STARTING:
            if (!(newState == AppState.INITIAL_SENDING))
                throw new IllegalStateException("Illegal state transition from " + state + " to " + newState);
            try {
                state = newState;
                populateInitialMessages();
                // Change GUI Window
            } catch (Exception e) {
                System.out.println("Something went wrong during creation of study: " + e);
            }
            break;
        case PARTICIPATING:
            if (newState != AppState.ENTERING_VALUES)
                throw new IllegalStateException("Illegal state transition from " + state + " to " + newState);
            state = newState;
            // Change GUI Window
            break;
        case ENTERING_VALUES:
            if (newState != AppState.SENDING_SHARE)
                throw new IllegalStateException("Illegal state transition from " + state + " to " + newState);
            state = newState;
            try {
                populateShareMessages();
                // Change GUI Window
            } catch (IOException e) {
                System.out.println("Something went wrong during creation of secret shares: " + e);
            }
            break;
        case INITIAL_SENDING:
            if (newState != AppState.RECIEVING_SHARE)
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
            if (newState != AppState.RECIEVING_SHARE)
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
            if (newState != AppState.SENDING_RESULT)
                throw new IllegalStateException("Illegal state transition from " + state + " to " + newState);
            if (!isResultComputable())
                throw new IllegalStateException("Not all shares collected");
            state = newState;
            try {
                populateResultMessages();
                // Change GUI Window
            } catch (Exception e) {
                System.out.println("Something went wrong during creation of Result Message: " + e);
            }
            break;
        case SENDING_RESULT:
            if (newState != AppState.RECIEVING_RESULT)
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
            if (newState != AppState.FINISHED)
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

    private Message getInitialMessage(int recipientId) throws IOException {
        InitialMessage data = new InitialMessage(this, recipientId);
        Participant recipient = this.participants[recipientId];
        return new Message(ownId, recipient, data.getMessage());
    }

    public void populateInitialMessages() throws IOException, IllegalStateException {
        if (state != AppState.INITIAL_SENDING)
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

    private Message getShareMessage(int recipientId) throws IOException {
        ShareMessage data = new ShareMessage(this, recipientId);
        Participant recipient = this.participants[recipientId];
        return new Message(ownId, recipient, data.getMessage());
    }

    public void populateShareMessages() throws IOException, IllegalStateException {
        if (state != AppState.SENDING_SHARE)
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

    public int getParticipantId(Participant p) throws IllegalArgumentException {
        for (int i = 0; i < participants.length; i++) {
            if (participants[i].equals(p))
                return i;
        }
        throw new IllegalArgumentException("Unknown participant " + p);
    }

    public Participant getParticipantFromId(int p) throws IllegalArgumentException {
        if (p < 0 || p > (participants.length - 1))
            throw new IllegalArgumentException("Unknown participant " + p);
        return participants[p];
    }

    public void setShareFromMessage(Message msg, Participant sender)
            throws IllegalStateException, IllegalArgumentException, ClassNotFoundException, IOException {
        if (!(state == AppState.RECIEVING_SHARE || state == AppState.RECIEVING_RESULT))
            throw new IllegalStateException("Setting a share from a Message is not allowed at state " + state);
        if (Message.validateData(getParticipantId(sender), participants[ownId], msg.data)) {
            if (state == AppState.RECIEVING_SHARE) {
                ShareMessage sm = ShareMessage.decodeAndVerify(Message.getMessageData(msg), sender, this);
                int senderId = getParticipantId(sender);
                for (int i = 0; i < bins.length; i++) {
                    bins[i].setInShare(sm.bins[i].share, senderId);
                }
            } else {
                ResultMessage rm = ResultMessage.decodeAndVerify(Message.getMessageData(msg), sender, this);
                int senderId = getParticipantId(sender);
                for (int i = 0; i < bins.length; i++) {
                    bins[i].setInShare(rm.bins[i].share, senderId);
                }
            }
        } else
            throw new IllegalArgumentException("Message invalid");

    }
    
    /**
     * Validates a given message to set a share or result
     * @param message
     * @return
     */
    public boolean isMessageShareResultValid(Message msg, Participant sender) {
        try {
            Message.validateData(getParticipantId(sender), participants[ownId], msg.data);
            switch (state){
            case RECIEVING_SHARE:
                ShareMessage.decodeAndVerify(Message.getMessageData(msg), sender, this);
                break;
            case RECIEVING_RESULT:
                ResultMessage.decodeAndVerify(Message.getMessageData(msg), sender, this);
                break;
            default:
                return false;
            }            
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public void setModelFromMessage(String initialMsg)
            throws IllegalStateException, IllegalArgumentException, ClassNotFoundException, IOException {
        if (state != AppState.PARTICIPATING)
            throw new IllegalStateException("Setting the Model from a Message is not allowed at state " + state);
        AppModel model = InitialMessage.getAppModel(InitialMessage.decodeMessage(Message.getMessageData(initialMsg)));
        model.state = AppState.PARTICIPATING;
        setModel(model);
    }

    private void setModel(AppModel model) {
        numParticipants = model.numParticipants;
        ownId = model.ownId;
        bins = model.bins;
        participants = model.participants;
        name = model.name;
        state = model.state;
    }

    public void markMessageSent(int recipientId) throws IllegalArgumentException {
        if (unsentMessages[recipientId] == null)
            throw new IllegalArgumentException("Message " + recipientId + " nonexistent");
        unsentMessages[recipientId] = null;
    }

    public void populateResultMessages() throws IOException, IllegalStateException {
        if (state != AppState.SENDING_RESULT)
            throw new IllegalStateException("Forbidden action (populateResultMessage) at current state " + state);

        ResultMessage data = new ResultMessage(this);
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

    public void clearBins() {
        for (Bin b : this.bins) {
            b.clearShares();
        }
    }

    public BinResult getBinResult(int binId) throws IllegalStateException {
        if (state != AppState.FINISHED)
            throw new IllegalStateException("Forbidden action (getBinResult) at current state " + state);
        return new BinResult(bins[binId].name, bins[binId].reconstructBin());
    }

    public BinResult[] getAllResults() throws IllegalStateException {
        if (state != AppState.FINISHED)
            throw new IllegalStateException("Forbidden action (getBinResult) at current state " + state);
        BinResult[] result = new BinResult[bins.length];
        for (int i = 0; i < bins.length; i++) {
            result[i] = getBinResult(i);
        }
        return result;
    }

    public boolean messagesUnsent() {
        for (Message m : unsentMessages) {
            if (m != null)
                return true;
        }
        return false;
    }

    public Message getUnsentMessageFor(int recipientId) {
        return unsentMessages[recipientId];
    }

    public boolean isResultComputable() {
        boolean ready = true;
        for (Bin b : bins) {
            ready &= b.isComplete();
        }
        return ready;
    }

    public void saveProgramAs() throws IOException {
        // Get Filename fom GUI Filepicker
        File fn = new File("filename.tmp");
        filename = fn;
        saveModel(filename);
    }

    public void saveProgram() throws IOException {
        if (filename == null) {
            saveProgramAs();
        } else {
            saveModel(filename);
        }
    }

    private void saveModel(File filename) throws IOException {
        ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filename));
        oos.writeObject(this);
        oos.close();
    }

    public static AppModel loadModel(File filename)
            throws IOException, ClassNotFoundException, IllegalArgumentException {
        ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filename));
        Object o = ois.readObject();
        ois.close();
        if (!(o instanceof AppModel))
            throw new IllegalArgumentException("Invalid Save file");
        return (AppModel) o;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (!(o instanceof AppModel))
            return false;
        AppModel m = (AppModel) o;
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

    @Override
    public int hashCode() {
        int result = numParticipants;
        result = 31 * result + ownId;
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

    @Override
    public String toString() {
        return "AppModel [numParticipants=" + numParticipants + ", ownId=" + ownId + ", state=" + state + ", bins="
                + Arrays.toString(bins) + ", participants=" + Arrays.toString(participants) + ", name=" + name
                + ", unsentMessages=" + Arrays.toString(unsentMessages) + ", filename=" + filename + "]";
    }

    @Override
    public Object clone() {
      AppModel newModel = null;
      try {
        newModel = (AppModel) super.clone();
      } catch (CloneNotSupportedException e) {
        newModel = new AppModel();
      }
      newModel.name = this.name;
      newModel.numParticipants = this.numParticipants;
      newModel.ownId = this.ownId;
      newModel.studyUID = this.studyUID;
      newModel.state = this.state;
      newModel.bins = new Bin[this.bins.length];
      newModel.participants = new Participant[this.participants.length];
      newModel.unsentMessages = new Message[this.unsentMessages.length];
      newModel.filename = this.filename;
      for (int i = 0; i < newModel.bins.length; i++) {
        newModel.bins[i] = (Bin) this.bins[i].clone();
      }
      for (int i = 0; i < newModel.participants.length; i++) {
        newModel.participants[i] = (Participant) this.participants[i].clone();
      }
      for (int i = 0; i < newModel.unsentMessages.length; i++) {
        if(this.unsentMessages[i] != null)
          newModel.unsentMessages[i] = (Message) this.unsentMessages[i].clone();
      }
      return newModel;
    }
}

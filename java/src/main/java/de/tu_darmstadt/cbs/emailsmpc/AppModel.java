package de.tu_darmstadt.cbs.emailsmpc;

import java.io.Serializable;
import java.io.IOException;
import java.math.BigInteger;

public class AppModel implements Serializable {
    public int numParticipants;
    public int ownId;
    public AppState state;
    public Bin[] bins;
    public Participant[] participants;
    public String name;
    public Message[] unsentMessages;
    private static final long serialVersionUID = 67394185932574354L;

    public AppModel() {
        name = null;
        numParticipants = 0;
        ownId = 0;
        state = AppState.NONE;
        bins = null;
        participants = null;
        unsentMessages = null;
    }

    public void initializeStudy(String name, Participant[] participants, Bin[] bins) throws IllegalStateException {
        if (state != AppState.NONE || state != AppState.STARTING)
            throw new IllegalStateException("Unable to initialize study at state" + state);
        this.name = name;
        numParticipants = participants.length;
        unsentMessages = new Message[numParticipants];
        for (Bin bin : bins) {
            if (!(bin.isInitialized()))
                throw new IllegalStateException("Unable to initialize bin " + bin.name);
            bin.initialize(numParticipants);
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

    public void advanceState(AppState newState) throws IllegalStateException {
        switch (state) {
        case NONE:
            if (!(newState == AppState.STARTING || newState == AppState.PARTICIPATING))
                throw new IllegalStateException("Illegal state transition from " + state + " to " + newState);
            break;
        case STARTING:
            if (!(newState == AppState.INITIAL_SENDING))
                throw new IllegalStateException("Illegal state transition from " + state + " to " + newState);
            try {
                setModel(createInitialStudy());
                populateShareMessages();
            } catch (Exception e) {
                System.out.println("Something went wrong during creation of study: " + e);
            }
            break;
        case PARTICIPATING:
            if (newState != AppState.SENDING_SHARE)
                throw new IllegalStateException("Illegal state transition from " + state + " to " + newState);
            break;
        case INITIAL_SENDING:
            if (newState != AppState.RECIEVING_SHARE)
                throw new IllegalStateException("Illegal state transition from " + state + " to " + newState);
            break;
        case SENDING_SHARE:
            if (newState != AppState.RECIEVING_SHARE)
                throw new IllegalStateException("Illegal state transition from " + state + " to " + newState);
            break;
        case RECIEVING_SHARE:
            if (newState != AppState.SENDING_RESULT)
                throw new IllegalStateException("Illegal state transition from " + state + " to " + newState);
            break;
        case SENDING_RESULT:
            if (newState != AppState.RECIEVING_RESULT)
                throw new IllegalStateException("Illegal state transition from " + state + " to " + newState);
            break;
        case RECIEVING_RESULT:
            if (newState != AppState.FINISHED)
                throw new IllegalStateException("Illegal state transition from " + state + " to " + newState);
            break;
        case FINISHED:
            throw new IllegalStateException("Illegal state transition: Already finished");
        }

    }

    private Message getInitialMessage(int recipientId) throws IOException {
        InitialMessage data = new InitialMessage(this, recipientId);
        Participant recipient = this.participants[recipientId];
        return new Message(recipient, data.getMessage());
    }

    public void populateInitialMessages() throws IOException, IllegalStateException {
        if (state != AppState.STARTING)
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
    }

    private Message getShareMessage(int recipientId) throws IOException {
        ShareMessage data = new ShareMessage(this, recipientId);
        Participant recipient = this.participants[recipientId];
        return new Message(recipient, data.getMessage());
    }

    public void populateShareMessages() throws IOException, IllegalStateException {
        if (state != AppState.SENDING_SHARE)
            throw new IllegalStateException("Forbidden action (populateShareMessage) at current state " + state);
        for (int i = 0; i < numParticipants; i++) {
            if (i != ownId)
                unsentMessages[i] = getShareMessage(i);
            else {
                for (Bin b : bins) {
                    b.transferSharesOutIn(ownId);
                }
            }
        }
    }

    public int getParticipantId(Participant p) throws IllegalArgumentException {
        for (int i = 0; i < participants.length; i++) {
            if (participants[i].equals(p))
                return i;
        }
        throw new IllegalArgumentException("Unknown participant " + p);
    }

    public void setShareFromMessage(Message msg, Participant sender)
            throws IllegalStateException, IllegalArgumentException, ClassNotFoundException, IOException {
        if (state != AppState.RECIEVING_SHARE)
            throw new IllegalStateException("Setting a share from a Message is not allowed at state " + state);
        if (Message.validateData(participants[ownId], msg.data)) {
            ShareMessage sm = ShareMessage.decodeAndVerify(Message.getMessageData(msg), sender, this);
            int senderId = getParticipantId(sender);
            for (int i = 0; i < bins.length; i++) {
                bins[i].setInShare(sm.bins[i].share, senderId);
            }
        } else
            throw new IllegalArgumentException("Message invalid");

    }

    public void setModelFromMessage(Message msg)
            throws IllegalStateException, IllegalArgumentException, ClassNotFoundException, IOException {
        if (state != AppState.PARTICIPATING)
            throw new IllegalStateException("Setting the Model from a Message is not allowed at state " + state);
        AppModel model = InitialMessage.getAppModel(InitialMessage.decodeMessage(Message.getMessageData(msg)));
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
                unsentMessages[i] = new Message(recipient, data.getMessage());
            } else {
                for (Bin b : bins) {
                    b.setInShare(b.getSumShare(), ownId);
                }
            }
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

    private AppModel createInitialStudy() {
        AppModel result = new AppModel();
        result.state = AppState.STARTING;
        // Get Stuff from GUI
        // reult.initializeStudy(name, participants, bins);
        return result;
    }
}

package de.tu_darmstadt.cbs.emailsmpc;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Base64;
import java.util.Base64.Decoder;
import java.util.Base64.Encoder;

/**
 * Initial message
 * @author Tobias Kussel
 */
public class InitialMessage implements Serializable {
    
    /** SVUID */
    private static final long serialVersionUID = 1631395617989735129L;
    
    /**
     * Decode message.
     *
     * @param msg the msg
     * @return the initial message
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws IllegalArgumentException the illegal argument exception
     * @throws ClassNotFoundException the class not found exception
     */
    public static InitialMessage decodeMessage(String msg)
            throws IOException, IllegalArgumentException, ClassNotFoundException {
        Decoder decoder = Base64.getDecoder();
        ByteArrayInputStream stream = new ByteArrayInputStream(decoder.decode(msg));
        ObjectInputStream ois = new ObjectInputStream(stream);
        Object o = ois.readObject();
        if (!(o instanceof InitialMessage))
            throw new IllegalArgumentException("Message not of type InitialMessage");
        return (InitialMessage) o;
    }
    
    /**
     * Gets the app model.
     *
     * @param msg the msg
     * @return the app model
     */
    public static AppModel getAppModel(InitialMessage msg) {
        AppModel model = new AppModel();
        model.name = msg.name;
        model.participants = msg.participants;
        model.numParticipants = msg.participants.length;
        model.ownId = msg.recipientId;
        model.state = AppState.PARTICIPATING;
        model.bins = new Bin[msg.bins.length];
        for (int i = 0; i < msg.bins.length; i++) {
            model.bins[i] = MessageBin.getBin(msg.bins[i], model.numParticipants);
        }
        return model;
    }
    
    /** The name. */
    public String name;
    
    /** The participants. */
    public Participant[] participants;

    /** The bins. */
    public MessageBin[] bins;

    /** The recipient id. */
    public int recipientId;

    /**
     * Instantiates a new initial message.
     *
     * @param model the model
     * @param recipientId the recipient id
     */
    public InitialMessage(AppModel model, int recipientId) {
        this.name = model.name;
        this.participants = model.participants;
        this.recipientId = recipientId;
        this.bins = new MessageBin[model.bins.length];
        for (int i = 0; i < model.bins.length; i++) {
            bins[i] = new MessageBin(model.bins[i], recipientId);
        }
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
        if (!(o instanceof InitialMessage))
            return false;
        InitialMessage msg = (InitialMessage) o;
        if (!(participants.length == msg.participants.length || bins.length == msg.bins.length))
            return false;
        boolean equal = this.name.equals(msg.name);
        equal = equal && (this.recipientId == msg.recipientId);

        for (int i = 0; i < participants.length; i++) {
            equal = equal && participants[i].equals(msg.participants[i]);
            if (equal == false) // Short circuit comparisons if false
                return false;
        }
        for (int i = 0; i < bins.length; i++) {
            equal = equal && bins[i].equals(msg.bins[i]);
            if (equal == false) // Short circuit comparisons if false
                return false;
        }
        return equal;
    }

    /**
     * Gets the message.
     *
     * @return the message
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public String getMessage() throws IOException {
        Encoder encoder = Base64.getEncoder();
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(stream);
        oos.writeObject(this);
        return encoder.encodeToString(stream.toByteArray());
    }

    /**
     * Hash code.
     *
     * @return the int
     */
    @Override
    public int hashCode() {
        int result = name.hashCode();
        for (Participant p : participants) {
            result = 31 * result + p.hashCode();
        }
        for (MessageBin b : bins) {
            result = 31 * result + b.hashCode();
        }
        return 31 * result + recipientId;
    }
}
